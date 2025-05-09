package com.alerts;

import com.cardio_generator.outputs.OutputStrategy;
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
    private TestOutputStrategy outputStrategy;    private static class TestOutputStrategy implements OutputStrategy {
        private final List<String> alerts = new ArrayList<>();
        private boolean alertTriggered = false;

        @Override
        public void output(int patientId, long timestamp, String label, String data) {
            System.out.println("Alert triggered: " + label);  // Debug output
            alerts.add(label);
            alertTriggered = true;  // Set flag indicating alert was triggered
        }

        public List<String> getAlerts() {
            return alerts;
        }

        public void clear() {
            alerts.clear();
            alertTriggered = false;  // Reset flag when clearing
        }
        
        public boolean isAlertTriggered() {
            return alertTriggered;
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        storage = DataStorage.getInstance(); // Use fresh instance
        storage.clear(); // Clear previous data
        FileDataReader reader = new FileDataReader("src/test/resources/test_data.txt");
        storage = DataStorage.getInstance(reader); // Update with reader
        storage.readData();
        outputStrategy = new TestOutputStrategy();
        alertGenerator = new AlertGenerator(storage, outputStrategy);
    }

    @Test
    void testBloodPressureCriticalHighAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Critical: Systolic BP above 180 mmHg")),
                   "Expected critical high systolic blood pressure alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testBloodPressureCriticalLowAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Critical: Systolic BP below 90 mmHg")),
                   "Expected critical low systolic blood pressure alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testDiastolicBloodPressureCriticalHighAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Critical: Diastolic BP above 110 mmHg")),
                   "Expected critical high diastolic blood pressure alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testDiastolicBloodPressureCriticalLowAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Critical: Diastolic BP below 60 mmHg")),
                   "Expected critical low diastolic blood pressure alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testBloodPressureTrendAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Trend: Three consecutive systolic BP increases > 10 mmHg")),
                   "Expected systolic blood pressure trend alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testDiastolicBloodPressureTrendAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Trend: Three consecutive diastolic BP increases > 10 mmHg")),
                   "Expected diastolic blood pressure trend alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testLowBloodSaturationAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Low Blood Saturation: Below 92%")),
                   "Expected low blood saturation alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testRapidSaturationDropAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Rapid Blood Saturation Drop: 5% or more in 10 minutes")),
                   "Expected rapid saturation drop alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testHypotensiveHypoxemiaAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Hypotensive Hypoxemia: Low BP and Low Saturation")),
                   "Expected hypotensive hypoxemia alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testECGAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Abnormal ECG Peak")),
                   "Expected ECG peak alert with priority and repeat");
        outputStrategy.clear();
    }

    @Test
    void testTriggeredAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        List<String> alerts = outputStrategy.getAlerts();
        assertTrue(alerts.stream().anyMatch(alert -> alert.contains("[Priority 2] [Repeated 1 times] Manual Alert: Triggered")),
                   "Expected triggered alert with priority and repeat");
        outputStrategy.clear();
    }
}