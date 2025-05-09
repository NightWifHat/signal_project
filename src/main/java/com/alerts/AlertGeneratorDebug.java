package com.alerts;

import com.cardio_generator.outputs.OutputStrategy;
import com.data_management.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlertGeneratorDebug {
    
    public static void main(String[] args) throws IOException {
        // Debug the alerting system independently of test runner
        System.out.println("Starting Alert Generator Debug...");
        
        // Create test output strategy
        TestOutputStrategy outputStrategy = new TestOutputStrategy();
        
        // Initialize data
        DataStorage storage = DataStorage.getInstance();
        storage.clear();
        FileDataReader reader = new FileDataReader("src/test/resources/test_data.txt");
        storage = DataStorage.getInstance(reader);
        storage.readData();
        
        // Create alert generator
        AlertGenerator alertGenerator = new AlertGenerator(storage, outputStrategy);
        
        // Get patient and evaluate
        Patient patient = storage.getAllPatients().get(0);
        System.out.println("Evaluating alerts for patient ID: " + patient.getPatientId());
        
        // Dump records for debug purposes
        List<PatientRecord> records = storage.getRecords(patient.getPatientId(), 0, System.currentTimeMillis());
        System.out.println("Patient records:");
        for (PatientRecord record : records) {
            System.out.println("  " + record.getRecordType() + ": " + record.getMeasurementValue());
        }
        
        // Run evaluation
        alertGenerator.evaluateData(patient);
        
        // Print results
        System.out.println("\nAlert evaluation complete. Triggered alerts:");
        List<String> alerts = outputStrategy.getAlerts();
        if (alerts.isEmpty()) {
            System.out.println("  NO ALERTS TRIGGERED!");
        } else {
            for (String alert : alerts) {
                System.out.println("  " + alert);
            }
        }
        System.out.println("\nAlerts triggered: " + outputStrategy.isAlertTriggered());
    }
    
    private static class TestOutputStrategy implements OutputStrategy {
        private final List<String> alerts = new ArrayList<>();
        private boolean alertTriggered = false;

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            alerts.add(label);
            alertTriggered = true;
        }

        public List<String> getAlerts() {
            return alerts;
        }
        
        public boolean isAlertTriggered() {
            return alertTriggered;
        }
    }
}
