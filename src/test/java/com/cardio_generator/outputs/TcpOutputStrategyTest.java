package com.cardio_generator.outputs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TcpOutputStrategyTest {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter printWriter;
    private TcpOutputStrategy tcpOutputStrategy;
    private ExecutorService executorService;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = mock(ServerSocket.class);
        clientSocket = mock(Socket.class);
        printWriter = mock(PrintWriter.class);
        executorService = mock(ExecutorService.class);

        // Mock ServerSocket.accept() to return the mocked clientSocket
        when(serverSocket.accept()).thenReturn(clientSocket);
        // Mock clientSocket.getOutputStream() to return a mocked OutputStream
        when(clientSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // Capture and execute the Runnable submitted to executorService
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(executorService.submit(runnableCaptor.capture())).thenAnswer(invocation -> {
            // Execute the captured Runnable immediately to simulate server startup
            runnableCaptor.getValue().run();
            return null;
        });

        // Set up System.err to capture error messages
        System.setErr(new PrintStream(errContent));

        // Create TcpOutputStrategy instance with mocked dependencies
        tcpOutputStrategy = new TcpOutputStrategy(12345, executorService) {
            @Override
            protected ServerSocket createServerSocket(int port) throws IOException {
                return serverSocket;
            }

            @Override
            protected PrintWriter createPrintWriter(Socket socket) throws IOException {
                return printWriter;
            }
        };
    }

    @Test
    void testOutputSuccess() throws IOException {
        tcpOutputStrategy.output(1, 1714376789050L, "TestLabel", "TestData");

        verify(printWriter).println("1,1714376789050,TestLabel,TestData");
    }

    @Test
    void testOutputNoClientConnected() throws IOException {
        // Create a new instance without executing the server Runnable
        TcpOutputStrategy strategy = new TcpOutputStrategy(12345, executorService) {
            @Override
            protected ServerSocket createServerSocket(int port) throws IOException {
                ServerSocket mockServer = mock(ServerSocket.class);
                when(mockServer.accept()).thenThrow(new IOException("Simulated failure"));
                return mockServer;
            }

            @Override
            protected PrintWriter createPrintWriter(Socket socket) throws IOException {
                return printWriter;
            }
        };

        strategy.output(1, 1714376789050L, "TestLabel", "TestData");

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("No client connected to send data"));
        errContent.reset();
    }

    @Test
    void testOutputWithInvalidPatientId() {
        assertThrows(IllegalArgumentException.class, () -> {
            tcpOutputStrategy.output(0, 1714376789050L, "TestLabel", "TestData");
        }, "Expected IllegalArgumentException for non-positive patient ID");
    }

    @Test
    void testOutputWithNullLabel() {
        assertThrows(IllegalArgumentException.class, () -> {
            tcpOutputStrategy.output(1, 1714376789050L, null, "TestData");
        }, "Expected IllegalArgumentException for null label");
    }

    @Test
    void testOutputWithEmptyLabel() {
        assertThrows(IllegalArgumentException.class, () -> {
            tcpOutputStrategy.output(1, 1714376789050L, " ", "TestData");
        }, "Expected IllegalArgumentException for empty label");
    }

    @Test
    void testOutputWithNullData() {
        assertThrows(IllegalArgumentException.class, () -> {
            tcpOutputStrategy.output(1, 1714376789050L, "TestLabel", null);
        }, "Expected IllegalArgumentException for null data");
    }

    @Test
    void testConstructorWithInvalidPort() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TcpOutputStrategy(0, Executors.newSingleThreadExecutor());
        }, "Expected IllegalArgumentException for port 0");

        assertThrows(IllegalArgumentException.class, () -> {
            new TcpOutputStrategy(65536, Executors.newSingleThreadExecutor());
        }, "Expected IllegalArgumentException for port 65536");
    }

    @AfterEach
    void restoreStreams() {
        System.setErr(originalErr);
    }
}