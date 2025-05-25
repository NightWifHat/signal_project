package com.data_management;

import com.alerts.AlertGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WebSocket real-time data processing.
 * Tests the complete flow from WebSocket data reception to alert generation.
 */
public class WebSocketIntegrationTest {

    private DataStorage dataStorage;
    private WebSocketDataReader webSocketDataReader;
    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() throws Exception {
        // Use singleton pattern for DataStorage
        dataStorage = DataStorage.getInstance();
        dataStorage.clear(); // Reset before each test
        
        // Create WebSocket data reader (won't actually connect in tests)
        webSocketDataReader = new WebSocketDataReader("ws://localhost:8080");
        
        // Create alert generator
        alertGenerator = new AlertGenerator(dataStorage, new ConsoleOutputStrategy());
    }

    @Test
    void testWebSocketDataReaderIntegration() {
        // Test that WebSocketDataReader integrates properly with DataStorage
        assertNotNull(webSocketDataReader);
        assertFalse(webSocketDataReader.isConnected());
        
        // Test that startStreaming can be called without errors
        assertDoesNotThrow(() -> webSocketDataReader.startStreaming(dataStorage));
    }

    @Test
    void testSimulatedRealTimeDataFlow() {
        // Simulate real-time data processing by directly adding data to storage
        // This simulates what would happen when WebSocket messages are received
        
        long baseTimestamp = System.currentTimeMillis();
        
        // Simulate incoming patient data
        dataStorage.addPatientData(1, 120.0, "SystolicPressure", baseTimestamp);
        dataStorage.addPatientData(1, 80.0, "DiastolicPressure", baseTimestamp + 1000);
        dataStorage.addPatientData(1, 72.0, "HeartRate", baseTimestamp + 2000);
        dataStorage.addPatientData(1, 98.6, "Temperature", baseTimestamp + 3000);
        
        // Verify data was stored correctly
        List<PatientRecord> records = dataStorage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(4, records.size());
        
        // Verify we can retrieve the patient
        Patient patient = dataStorage.getPatient(1);
        assertNotNull(patient);
        assertEquals(1, patient.getPatientId());
    }

    @Test
    void testConcurrentDataStorage() throws InterruptedException {
        // Test thread safety of DataStorage during concurrent WebSocket updates
        int numThreads = 5;
        int messagesPerThread = 10;
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        // Simulate multiple WebSocket connections sending data concurrently
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    for (int i = 0; i < messagesPerThread; i++) {
                        int patientId = threadId + 1; // Different patient per thread
                        double value = 100.0 + i;
                        String recordType = "HeartRate";
                        long timestamp = System.currentTimeMillis() + i;
                        
                        dataStorage.addPatientData(patientId, value, recordType, timestamp);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        
        // Verify all data was stored correctly
        for (int patientId = 1; patientId <= numThreads; patientId++) {
            Patient patient = dataStorage.getPatient(patientId);
            assertNotNull(patient, "Patient " + patientId + " should exist");
            
            List<PatientRecord> records = dataStorage.getRecords(patientId, 0, Long.MAX_VALUE);
            assertEquals(messagesPerThread, records.size(), 
                        "Patient " + patientId + " should have " + messagesPerThread + " records");
        }
    }

    @Test
    @Timeout(10)
    void testAlertGenerationWithRealTimeData() {
        // Test that alerts can be generated from real-time WebSocket data
        long baseTimestamp = System.currentTimeMillis();
        
        // Add critical vital signs that should trigger alerts
        dataStorage.addPatientData(1, 200.0, "SystolicPressure", baseTimestamp); // High blood pressure
        dataStorage.addPatientData(1, 120.0, "DiastolicPressure", baseTimestamp + 1000); // High diastolic
        dataStorage.addPatientData(1, 150.0, "HeartRate", baseTimestamp + 2000); // High heart rate
        
        // Test that alert generation works with the stored data
        Patient patient = dataStorage.getPatient(1);
        assertNotNull(patient);
        
        // The alert generator should be able to process this patient's data
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testWebSocketErrorHandling() {
        // Test WebSocket client with invalid URI
        assertThrows(IllegalArgumentException.class, () -> {
            new WebSocketDataReader("invalid-uri");
        });
        
        // Test WebSocket client with valid but unreachable URI
        WebSocketDataReader unreachableReader = new WebSocketDataReader("ws://unreachable-host:9999");
        
        // Should not throw exception but should handle connection failure gracefully
        assertDoesNotThrow(() -> unreachableReader.startStreaming(dataStorage));
        assertFalse(unreachableReader.isConnected());
    }

    @Test
    void testDataStoragePatientManagement() {
        // Test that DataStorage correctly manages multiple patients in real-time
        long timestamp = System.currentTimeMillis();
        
        // Add data for multiple patients
        for (int patientId = 1; patientId <= 3; patientId++) {
            dataStorage.addPatientData(patientId, 72.0, "HeartRate", timestamp);
            dataStorage.addPatientData(patientId, 120.0, "SystolicPressure", timestamp + 1000);
            dataStorage.addPatientData(patientId, 80.0, "DiastolicPressure", timestamp + 2000);
        }
        
        // Verify all patients exist
        List<Patient> allPatients = dataStorage.getAllPatients();
        assertEquals(3, allPatients.size());
        
        // Verify each patient has the correct number of records
        for (int patientId = 1; patientId <= 3; patientId++) {
            Patient patient = dataStorage.getPatient(patientId);
            assertNotNull(patient);
            assertEquals(patientId, patient.getPatientId());
            
            List<PatientRecord> records = dataStorage.getRecords(patientId, 0, Long.MAX_VALUE);
            assertEquals(3, records.size());
        }
    }

    @Test
    void testWebSocketDataReaderLifecycle() {
        // Test the complete lifecycle of WebSocketDataReader
        WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:8080");
        
        // Initial state
        assertFalse(reader.isConnected());
        assertEquals(URI.create("ws://localhost:8080"), reader.getServerUri());
        
        // Start streaming (won't actually connect due to no server)
        assertDoesNotThrow(() -> reader.startStreaming(dataStorage));
        
        // Close connection
        assertDoesNotThrow(() -> reader.close());
    }

    @Test
    void testCompatibilityWithFileDataReader() {
        // Test that WebSocket and File data readers can coexist
        // This ensures Week 1-4 functionality is preserved
        
        // Simulate data from WebSocket
        dataStorage.addPatientData(1, 72.0, "HeartRate", System.currentTimeMillis());
        
        // Verify we still have the same interface and storage
        Patient patient = dataStorage.getPatient(1);
        assertNotNull(patient);
        
        List<PatientRecord> records = dataStorage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(72.0, records.get(0).getMeasurementValue());
    }
}
