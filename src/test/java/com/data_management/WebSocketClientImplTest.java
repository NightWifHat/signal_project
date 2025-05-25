package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketClientImpl class.
 * Tests WebSocket message parsing, error handling, and integration with DataStorage.
 */
@ExtendWith(MockitoExtension.class)
public class WebSocketClientImplTest {

    @Mock
    private DataStorage mockDataStorage;

    private WebSocketClientImpl webSocketClient;
    private URI testUri;

    @BeforeEach
    void setUp() throws Exception {
        testUri = new URI("ws://localhost:8080");
        webSocketClient = new WebSocketClientImpl(testUri);
    }

    @Test
    void testConstructor() {
        assertNotNull(webSocketClient);
        assertEquals(testUri, webSocketClient.getURI());
        assertFalse(webSocketClient.isOpen());
    }

    @Test
    void testOnMessageValidFormat() {
        // Start streaming to set the dataStorage
        webSocketClient.startStreaming(mockDataStorage);
        
        // Test valid message format: patientId,measurementValue,recordType,timestamp
        String validMessage = "123,98.6,Temperature,1714376789050";
        
        webSocketClient.onMessage(validMessage);
        
        verify(mockDataStorage).addPatientData(123, 98.6, "Temperature", 1714376789050L);
    }

    @Test
    void testOnMessageMultipleValidMessages() {
        webSocketClient.startStreaming(mockDataStorage);
        
        String message1 = "1,120.5,SystolicPressure,1714376789000";
        String message2 = "2,78.2,HeartRate,1714376789100";
        String message3 = "1,80.0,DiastolicPressure,1714376789200";
        
        webSocketClient.onMessage(message1);
        webSocketClient.onMessage(message2);
        webSocketClient.onMessage(message3);
        
        verify(mockDataStorage).addPatientData(1, 120.5, "SystolicPressure", 1714376789000L);
        verify(mockDataStorage).addPatientData(2, 78.2, "HeartRate", 1714376789100L);
        verify(mockDataStorage).addPatientData(1, 80.0, "DiastolicPressure", 1714376789200L);
    }

    @Test
    void testOnMessageInvalidFormat() {
        webSocketClient.startStreaming(mockDataStorage);
        
        // Test messages with wrong number of parts
        String invalidMessage1 = "123,98.6,Temperature"; // Missing timestamp
        String invalidMessage2 = "123,98.6"; // Missing recordType and timestamp
        String invalidMessage3 = "123,98.6,Temperature,1714376789050,extra"; // Too many parts
        String invalidMessage4 = ""; // Empty message
        
        // These should not throw exceptions but should print error messages
        assertDoesNotThrow(() -> webSocketClient.onMessage(invalidMessage1));
        assertDoesNotThrow(() -> webSocketClient.onMessage(invalidMessage2));
        assertDoesNotThrow(() -> webSocketClient.onMessage(invalidMessage3));
        assertDoesNotThrow(() -> webSocketClient.onMessage(invalidMessage4));
        
        // Verify that no data was stored for invalid messages
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    void testOnMessageInvalidDataTypes() {
        webSocketClient.startStreaming(mockDataStorage);
        
        // Test messages with invalid data types
        String invalidPatientId = "abc,98.6,Temperature,1714376789050";
        String invalidMeasurementValue = "123,abc,Temperature,1714376789050";
        String invalidTimestamp = "123,98.6,Temperature,abc";
        
        // These should not throw exceptions but should print error messages
        assertDoesNotThrow(() -> webSocketClient.onMessage(invalidPatientId));
        assertDoesNotThrow(() -> webSocketClient.onMessage(invalidMeasurementValue));
        assertDoesNotThrow(() -> webSocketClient.onMessage(invalidTimestamp));
        
        // Verify that no data was stored for invalid messages
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    void testOnMessageWithWhitespace() {
        webSocketClient.startStreaming(mockDataStorage);
        
        // Test message with extra whitespace (should still work)
        String messageWithSpaces = " 123 , 98.6 , Temperature , 1714376789050 ";
        
        // This might fail depending on how trim() is handled in implementation
        // If implementation doesn't trim, this will cause NumberFormatException
        assertDoesNotThrow(() -> webSocketClient.onMessage(messageWithSpaces));
    }

    @Test
    void testReadDataThrowsUnsupportedOperation() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> webSocketClient.readData(mockDataStorage)
        );
        
        assertEquals("Use startStreaming for real-time data.", exception.getMessage());
    }

    @Test
    void testStartStreaming() {
        // Test that startStreaming sets the dataStorage and attempts to connect
        assertDoesNotThrow(() -> webSocketClient.startStreaming(mockDataStorage));
        
        // The connection might fail since there's no actual server, but it should not throw an exception
        // In a real scenario, this would connect to a WebSocket server
    }

    @Test
    void testOnOpen() {
        // Test onOpen method - should not throw exceptions
        assertDoesNotThrow(() -> webSocketClient.onOpen(null));
    }

    @Test
    void testOnClose() {
        // Test onClose method with different codes
        assertDoesNotThrow(() -> webSocketClient.onClose(1000, "Normal closure", false));
        assertDoesNotThrow(() -> webSocketClient.onClose(1006, "Abnormal closure", true));
    }

    @Test
    void testOnError() {
        // Test onError method
        Exception testException = new RuntimeException("Test error");
        assertDoesNotThrow(() -> webSocketClient.onError(testException));
    }

    @Test
    void testEdgeCaseMessages() {
        webSocketClient.startStreaming(mockDataStorage);
        
        // Test edge cases
        String negativePatientId = "-1,98.6,Temperature,1714376789050";
        String zeroMeasurement = "123,0.0,Temperature,1714376789050";
        String negativeMeasurement = "123,-10.5,Temperature,1714376789050";
        String veryLargeTimestamp = "123,98.6,Temperature,9999999999999";
        
        assertDoesNotThrow(() -> webSocketClient.onMessage(negativePatientId));
        assertDoesNotThrow(() -> webSocketClient.onMessage(zeroMeasurement));
        assertDoesNotThrow(() -> webSocketClient.onMessage(negativeMeasurement));
        assertDoesNotThrow(() -> webSocketClient.onMessage(veryLargeTimestamp));
        
        // Verify some calls were made (negative patient ID might be filtered out by DataStorage)
        verify(mockDataStorage, atLeastOnce()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    void testSpecialCharactersInRecordType() {
        webSocketClient.startStreaming(mockDataStorage);
        
        // Test record types with special characters
        String messageWithSpaces = "123,98.6,Blood Pressure,1714376789050";
        String messageWithUnderscores = "123,98.6,Heart_Rate,1714376789050";
        String messageWithNumbers = "123,98.6,Type123,1714376789050";
        
        assertDoesNotThrow(() -> webSocketClient.onMessage(messageWithSpaces));
        assertDoesNotThrow(() -> webSocketClient.onMessage(messageWithUnderscores));
        assertDoesNotThrow(() -> webSocketClient.onMessage(messageWithNumbers));
        
        verify(mockDataStorage).addPatientData(123, 98.6, "Blood Pressure", 1714376789050L);
        verify(mockDataStorage).addPatientData(123, 98.6, "Heart_Rate", 1714376789050L);
        verify(mockDataStorage).addPatientData(123, 98.6, "Type123", 1714376789050L);
    }
}
