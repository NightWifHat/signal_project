package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * WebSocketClient class that connects to a WebSocket server and processes incoming messages.
 * It utilizes the DataStorage class to store parsed information.
 */
public class WebSocketClientImpl extends WebSocketClient implements DataReader {

    private DataStorage dataStorage;

    /**
     * Constructor to initialize the WebSocket client with the server URI.
     *
     * @param serverUri the URI of the WebSocket server
     */
    public WebSocketClientImpl(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to WebSocket server.");
    }    @Override
    public void onMessage(String message) {
        try {
            // Parse the incoming message
            // Expected format: patientId,measurementValue,recordType,timestamp
            String[] parts = message.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid message format: " + message);
            }

            int patientId = Integer.parseInt(parts[0]);
            double measurementValue = Double.parseDouble(parts[1]);
            String recordType = parts[2];
            long timestamp = Long.parseLong(parts[3]);

            // Store the parsed data in DataStorage
            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);

            System.out.println("Processed WebSocket message: Patient " + patientId +
                    ", " + recordType + " = " + measurementValue +
                    " at " + timestamp);
        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            e.printStackTrace();
        }
    }    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed: " + reason + " (Code: " + code + ")");

        // Attempt to reconnect after a delay for certain close codes
        if (code != 1000 && code != 1001) { // Not normal closure or going away
            System.out.println("Attempting to reconnect in 5 seconds...");
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    if (!isOpen()) {
                        reconnect();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Reconnection attempt interrupted");
                } catch (Exception e) {
                    System.err.println("Failed to reconnect: " + e.getMessage());
                }
            }).start();
        }
    }    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error occurred: " + ex.getMessage());
        ex.printStackTrace();

        // Log connection state for debugging
        System.err.println("Connection state - Open: " + isOpen() +
                ", Closing: " + isClosing() +
                ", Closed: " + isClosed());
    }

    @Override
    public void readData(DataStorage dataStorage) {
        throw new UnsupportedOperationException("Use startStreaming for real-time data.");
    }

    @Override
    public void startStreaming(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.connect();
    }
}