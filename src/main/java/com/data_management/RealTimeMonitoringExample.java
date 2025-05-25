package com.data_management;

import com.alerts.AlertGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.data_management.DataStorage;
import com.data_management.WebSocketDataReader;
import com.data_management.Patient;

import java.net.URI;

/**
 * Example demonstrating real-time WebSocket data processing with alert generation.
 * This example shows how to set up real-time monitoring of patient data using WebSocket streams.
 */
public class RealTimeMonitoringExample {

    public static void main(String[] args) {
        try {
            // Step 1: Configure WebSocket connection
            String websocketUrl = "ws://localhost:8080"; // Signal generator WebSocket endpoint
            if (args.length > 0) {
                websocketUrl = args[0];
            }
            
            System.out.println("=== Real-Time Patient Monitoring System ===");
            System.out.println("Connecting to WebSocket server: " + websocketUrl);
            
            // Step 2: Create WebSocket data reader
            WebSocketDataReader websocketReader = new WebSocketDataReader(websocketUrl);
            
            // Step 3: Initialize data storage with WebSocket reader
            DataStorage dataStorage = DataStorage.getInstance(websocketReader);
            
            // Step 4: Set up alert generation with console output
            AlertGenerator alertGenerator = new AlertGenerator(dataStorage, new ConsoleOutputStrategy());
            
            // Step 5: Start real-time data streaming
            System.out.println("Starting real-time data streaming...");
            dataStorage.startStreaming();
            
            // Step 6: Monitor for connection status
            Thread.sleep(2000); // Give connection time to establish
            if (websocketReader.isConnected()) {
                System.out.println("✓ Successfully connected to WebSocket server");
            } else {
                System.out.println("⚠ Connection to WebSocket server is pending...");
            }
            
            // Step 7: Real-time monitoring loop
            System.out.println("Starting real-time patient monitoring...");
            System.out.println("Expected message format: patientId,measurementValue,recordType,timestamp");
            System.out.println("Example: 123,98.6,Temperature,1714376789050");
            System.out.println("\nMonitoring for alerts... (Press Ctrl+C to stop)\n");
            
            int monitoringCycles = 0;
            while (true) {
                // Process alerts for all patients every second
                for (Patient patient : dataStorage.getAllPatients()) {
                    alertGenerator.evaluateData(patient);
                }
                
                // Display monitoring status every 30 seconds
                if (monitoringCycles % 30 == 0) {
                    displayMonitoringStatus(dataStorage, websocketReader);
                }
                
                Thread.sleep(1000); // Check every second
                monitoringCycles++;
            }
            
        } catch (Exception e) {
            System.err.println("Error in real-time monitoring: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Display current monitoring status including patient count and connection status.
     */
    private static void displayMonitoringStatus(DataStorage dataStorage, WebSocketDataReader websocketReader) {
        System.out.println("\n--- Monitoring Status ---");
        System.out.println("Connection Status: " + (websocketReader.isConnected() ? "Connected" : "Disconnected"));
        System.out.println("Total Patients: " + dataStorage.getAllPatients().size());
        System.out.println("WebSocket Server: " + websocketReader.getServerUri());
        
        // Display recent patient data summary
        for (Patient patient : dataStorage.getAllPatients()) {
            var records = dataStorage.getRecords(patient.getPatientId(), 
                                                System.currentTimeMillis() - 60000, // Last minute
                                                System.currentTimeMillis());
            if (!records.isEmpty()) {
                System.out.println("Patient " + patient.getPatientId() + 
                                 ": " + records.size() + " readings in last minute");
            }
        }
        System.out.println("------------------------\n");
    }
}

/**
 * Example showing how to combine batch processing with real-time WebSocket data.
 * This demonstrates hybrid data processing capabilities.
 */
class HybridDataProcessingExample {
    
    public static void demonstrateHybridProcessing() {
        try {
            System.out.println("=== Hybrid Data Processing Example ===");
            
            // Step 1: Load historical data from files (batch processing)
            DataStorage dataStorage = DataStorage.getInstance();
            
            // Simulate loading historical data
            System.out.println("Loading historical patient data...");
            long baseTimestamp = System.currentTimeMillis() - 86400000; // 24 hours ago
            
            // Add some historical data for demonstration
            for (int i = 1; i <= 5; i++) {
                dataStorage.addPatientData(i, 120.0 + i, "SystolicPressure", baseTimestamp + (i * 3600000));
                dataStorage.addPatientData(i, 80.0 + i, "DiastolicPressure", baseTimestamp + (i * 3600000));
                dataStorage.addPatientData(i, 70.0 + i, "HeartRate", baseTimestamp + (i * 3600000));
            }
            
            System.out.println("Historical data loaded for " + dataStorage.getAllPatients().size() + " patients");
            
            // Step 2: Add real-time WebSocket streaming
            WebSocketDataReader websocketReader = new WebSocketDataReader("ws://localhost:8080");
            dataStorage = DataStorage.getInstance(websocketReader);
            
            System.out.println("Starting real-time data streaming on top of historical data...");
            dataStorage.startStreaming();
            
            // Step 3: Set up monitoring that works with both data sources
            AlertGenerator alertGenerator = new AlertGenerator(dataStorage, new ConsoleOutputStrategy());
            
            // Step 4: Monitor both historical and real-time data
            System.out.println("Monitoring both historical and real-time data...");
            
            for (int cycle = 0; cycle < 10; cycle++) {
                for (Patient patient : dataStorage.getAllPatients()) {
                    alertGenerator.evaluateData(patient);
                }
                Thread.sleep(2000);
            }
            
            System.out.println("Hybrid processing demonstration completed.");
            
        } catch (Exception e) {
            System.err.println("Error in hybrid processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
