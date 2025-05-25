package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketDataReader class.
 * Tests the wrapper functionality and interface compliance.
 */
@ExtendWith(MockitoExtension.class)
public class WebSocketDataReaderTest {

    @Mock
    private DataStorage mockDataStorage;

    private WebSocketDataReader webSocketDataReader;
    private URI testUri;

    @BeforeEach
    void setUp() throws Exception {
        testUri = new URI("ws://localhost:8080");
        webSocketDataReader = new WebSocketDataReader(testUri);
    }

    @Test
    void testConstructorWithURI() {
        assertNotNull(webSocketDataReader);
        assertEquals(testUri, webSocketDataReader.getServerUri());
        assertFalse(webSocketDataReader.isConnected());
    }

    @Test
    void testConstructorWithStringURI() {
        String uriString = "ws://localhost:9090";
        WebSocketDataReader reader = new WebSocketDataReader(uriString);
        
        assertNotNull(reader);
        assertEquals(URI.create(uriString), reader.getServerUri());
        assertFalse(reader.isConnected());
    }

    @Test
    void testConstructorWithInvalidURI() {
        String invalidUri = "invalid-uri";
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new WebSocketDataReader(invalidUri)
        );
        
        assertTrue(exception.getMessage().contains("Invalid WebSocket URI"));
    }

    @Test
    void testReadDataThrowsUnsupportedOperation() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> webSocketDataReader.readData(mockDataStorage)
        );
        
        assertTrue(exception.getMessage().contains("Batch reading is not supported"));
        assertTrue(exception.getMessage().contains("Use startStreaming()"));
    }

    @Test
    void testStartStreaming() {
        // Test that startStreaming doesn't throw exceptions
        assertDoesNotThrow(() -> webSocketDataReader.startStreaming(mockDataStorage));
        
        // In a real scenario, this would connect to a WebSocket server
        // Since there's no actual server running, the connection will fail gracefully
    }

    @Test
    void testIsConnectedInitiallyFalse() {
        assertFalse(webSocketDataReader.isConnected());
    }

    @Test
    void testClose() {
        // Test that close doesn't throw exceptions even when not connected
        assertDoesNotThrow(() -> webSocketDataReader.close());
    }

    @Test
    void testGetServerUri() {
        assertEquals(testUri, webSocketDataReader.getServerUri());
    }

    @Test
    void testMultipleStartStreamingCalls() {
        // Test that multiple calls to startStreaming don't cause issues
        assertDoesNotThrow(() -> {
            webSocketDataReader.startStreaming(mockDataStorage);
            webSocketDataReader.startStreaming(mockDataStorage);
        });
    }

    @Test
    void testCloseAfterStartStreaming() {
        // Test close after attempting to start streaming
        assertDoesNotThrow(() -> {
            webSocketDataReader.startStreaming(mockDataStorage);
            webSocketDataReader.close();
        });
    }

    @Test
    void testDifferentURISchemes() {
        // Test with wss:// (secure WebSocket)
        assertDoesNotThrow(() -> {
            WebSocketDataReader secureReader = new WebSocketDataReader("wss://secure-server:443");
            assertEquals(URI.create("wss://secure-server:443"), secureReader.getServerUri());
        });

        // Test with different ports
        assertDoesNotThrow(() -> {
            WebSocketDataReader portReader = new WebSocketDataReader("ws://localhost:3000");
            assertEquals(URI.create("ws://localhost:3000"), portReader.getServerUri());
        });
    }

    @Test
    void testURIWithPath() {
        String uriWithPath = "ws://localhost:8080/data-stream";
        WebSocketDataReader reader = new WebSocketDataReader(uriWithPath);
        
        assertEquals(URI.create(uriWithPath), reader.getServerUri());
    }

    @Test
    void testURIWithQueryParameters() {
        String uriWithQuery = "ws://localhost:8080/stream?patient=123&type=realtime";
        WebSocketDataReader reader = new WebSocketDataReader(uriWithQuery);
        
        assertEquals(URI.create(uriWithQuery), reader.getServerUri());
    }
}
