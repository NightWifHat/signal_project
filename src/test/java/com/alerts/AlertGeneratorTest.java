package com.alerts;

import com.cardio_generator.outputs.OutputStrategy; // Added import
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlertGeneratorTest {
    private DataStorage storage;
    private AlertGenerator alertGenerator;
    private TestOutputStrategy outputStrategy; // Custom OutputStrategy to capture alerts

    // Custom OutputStrategy to capture alerts for testing
    private static class TestOutputStrategy implements OutputStrategy {
        private final List<String> alerts = new ArrayList<>();

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            alerts.add(label); // Capture the label (which includes the alert message)
        }

        public List<String> getAlerts() {
            return alerts;
        }

        public void clear() {
            alerts.clear();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        storage = new DataStorage(new FileDataReader("src/test/resources/test_data.txt"));
        storage.readData();
        outputStrategy = new TestOutputStrategy(); // Initialize the custom OutputStrategy
        alertGenerator = new AlertGenerator(storage, outputStrategy); // Provide OutputStrategy
    }

    @Test
    void testBloodPressureCriticalHighAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Critical: Systolic BP above 180 mmHg")),
                   "Expected critical high systolic blood pressure alert");
        outputStrategy.clear();
    }

    @Test
    void testBloodPressureCriticalLowAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Critical: Systolic BP below 90 mmHg")),
                   "Expected critical low systolic blood pressure alert");
        outputStrategy.clear();
    }

    @Test
    void testDiastolicBloodPressureCriticalHighAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Critical: Diastolic BP above 110 mmHg")),
                   "Expected critical high diastolic blood pressure alert");
        outputStrategy.clear();
    }

    @Test
    void testDiastolicBloodPressureCriticalLowAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Critical: Diastolic BP below 60 mmHg")),
                   "Expected critical low diastolic blood pressure alert");
        outputStrategy.clear();
    }

    @Test
    void testBloodPressureTrendAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Trend: Three consecutive systolic BP increases > 10 mmHg")),
                   "Expected systolic blood pressure trend alert");
        outputStrategy.clear();
    }

    @Test
    void testDiastolicBloodPressureTrendAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Trend: Three consecutive diastolic BP increases > 10 mmHg")),
                   "Expected diastolic blood pressure trend alert");
        outputStrategy.clear();
    }

    @Test
    void testLowBloodSaturationAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Low Blood Saturation: Below 92%")),
                   "Expected low blood saturation alert");
        outputStrategy.clear();
    }

    @Test
    void testRapidSaturationDropAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Rapid Blood Saturation Drop: 5% or more in 10 minutes")),
                   "Expected rapid saturation drop alert");
        outputStrategy.clear();
    }

    @Test
    void testHypotensiveHypoxemiaAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Hypotensive Hypoxemia: Low BP and Low Saturation")),
                   "Expected hypotensive hypoxemia alert");
        outputStrategy.clear();
    }

    @Test
    void testECGAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Abnormal ECG Peak")),
                   "Expected ECG peak alert");
        outputStrategy.clear();
    }

    @Test
    void testTriggeredAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("Manual Alert: Triggered")),
                   "Expected triggered alert");
        outputStrategy.clear();
    }
}