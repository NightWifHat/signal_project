package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpOutputStrategy implements OutputStrategy {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private ExecutorService executorService;
    private final int port;

    public TcpOutputStrategy(int port) throws IOException {
        this(port, Executors.newSingleThreadExecutor());
    }

    public TcpOutputStrategy(int port, ExecutorService executorService) throws IOException {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.port = port;
        this.executorService = executorService;
        this.serverSocket = createServerSocket(port);
        startServer();
    }

    protected ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    protected PrintWriter createPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

    protected ExecutorService createExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

    private void startServer() {
        executorService.submit(() -> {
            try {
                System.out.println("TCP Server started on port " + port);
                clientSocket = serverSocket.accept();
                out = createPrintWriter(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void output(int patientId, long timestamp, String label, String value) {
        if (patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be positive");
        }
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Label cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, value);
            out.println(message);
        } else {
            System.err.println("No client connected to send data");
        }
    }

    public void close() throws IOException {
        if (out != null) out.close();
        if (clientSocket != null) clientSocket.close();
        if (serverSocket != null) serverSocket.close();
        if (executorService != null) executorService.shutdown();
    }
}