package com.data_management;

import java.io.IOException;
import java.net.URI;

/**
 * A DataReader implementation that reads patient data from a WebSocket server in real-time.
 * This class serves as a wrapper around WebSocketClientImpl to provide a clean interface
 * for real-time data reading while maintaining compatibility with batch processing systems.
 */
public class WebSocketDataReader implements DataReader {
    
    private final WebSocketClientImpl webSocketClient;
    private final URI serverUri;
    private boolean isStreamingStarted = false;
    
    /**
     * Constructs a WebSocketDataReader to connect to the specified WebSocket server.
     *
     * @param serverUri the URI of the WebSocket server (e.g., "ws://localhost:8080")
     */
    public WebSocketDataReader(URI serverUri) {
        this.serverUri = serverUri;
        this.webSocketClient = new WebSocketClientImpl(serverUri);
    }
      /**
     * Constructs a WebSocketDataReader from a URI string.
     *
     * @param serverUriString the URI string of the WebSocket server
     * @throws IllegalArgumentException if the URI string is invalid
     */
    public WebSocketDataReader(String serverUriString) {
        try {
            this.serverUri = URI.create(serverUriString);
            // Validate that it's a proper WebSocket URI
            if (!serverUri.getScheme().equals("ws") && !serverUri.getScheme().equals("wss")) {
                throw new IllegalArgumentException("URI must use ws:// or wss:// scheme");
            }
            this.webSocketClient = new WebSocketClientImpl(this.serverUri);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid WebSocket URI: " + serverUriString, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Note: This method is not supported for WebSocket-based real-time data reading.
     * Use startStreaming() instead for continuous data processing.
     * 
     * @throws UnsupportedOperationException always, as WebSocket data reading is streaming-based
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        throw new UnsupportedOperationException(
            "Batch reading is not supported for WebSocket data source. " +
            "Use startStreaming() for real-time data processing.");
    }

    /**
     * {@inheritDoc}
     * 
     * Starts real-time data streaming from the WebSocket server.
     * This method establishes a connection to the WebSocket server and begins
     * processing incoming messages continuously.
     * 
     * Expected message format: patientId,measurementValue,recordType,timestamp
     * Example: 1,98.6,Temperature,1714376789050
     */    @Override
    public void startStreaming(DataStorage dataStorage) {
        if (isStreamingStarted) {
            System.out.println("WebSocket streaming already started. Ignoring duplicate call.");
            return;
        }
        
        System.out.println("Starting WebSocket data streaming from: " + serverUri);
        webSocketClient.startStreaming(dataStorage);
        isStreamingStarted = true;
    }
    
    /**
     * Checks if the WebSocket connection is currently open.
     *
     * @return true if the connection is open, false otherwise
     */
    public boolean isConnected() {
        return webSocketClient.isOpen();
    }
      /**
     * Closes the WebSocket connection.
     */
    public void close() {
        if (webSocketClient.isOpen()) {
            webSocketClient.close();
            System.out.println("WebSocket connection closed.");
        }
        isStreamingStarted = false;
    }
    
    /**
     * Gets the URI of the WebSocket server this reader is connected to.
     *
     * @return the server URI
     */
    public URI getServerUri() {
        return serverUri;
    }
}
