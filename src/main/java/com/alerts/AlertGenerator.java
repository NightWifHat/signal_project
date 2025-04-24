package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private static final long TEN_MINUTES = 10 * 60 * 1000; // 10 minutes in milliseconds
    private static final int SLIDING_WINDOW_SIZE = 5; // For ECG moving average

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert} method.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        int patientIdInt = patient.getPatientId();
        String patientId = String.valueOf(patientIdInt); // Convert int to String for Alert
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 3600_000; // 1 hour ago for most checks
        List<PatientRecord> records = dataStorage.getRecords(patientIdInt, 0, endTime);

        // 1. Blood Pressure Alerts
        checkBloodPressureAlerts(patientId, records);

        // 2. Blood Saturation Alerts
        checkBloodSaturationAlerts(patientId, records);

        // 3. Hypotensive Hypoxemia Alert
        checkHypotensiveHypoxemiaAlert(patientId, records);

        // 4. ECG Data Alerts
        checkECGAlerts(patientId, records);

        // 5. Triggered Alerts
        checkTriggeredAlerts(patientId, records);
    }

    private void checkBloodPressureAlerts(String patientId, List<PatientRecord> records) {
        // Critical Threshold Alerts (assuming measurementValue is systolic BP)
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("BloodPressure")) {
                try {
                    double systolic = record.getMeasurementValue();
                    if (systolic > 180) {
                        triggerAlert(new Alert(patientId, "Critical: Systolic BP above 180 mmHg", record.getTimestamp()));
                    } else if (systolic < 90) {
                        triggerAlert(new Alert(patientId, "Critical: Systolic BP below 90 mmHg", record.getTimestamp()));
                    }
                    // Diastolic not handled until PatientRecord.java clarifies format
                } catch (Exception e) {
                    System.err.println("Error parsing BloodPressure record: " + record.getMeasurementValue());
                }
            }
        }

        // Trend Alerts (3 consecutive increases/decreases > 10 mmHg)
        List<PatientRecord> bpRecords = new ArrayList<>();
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("BloodPressure")) {
                bpRecords.add(record);
            }
        }
        checkBloodPressureTrend(patientId, bpRecords, "systolic");
    }

    private void checkBloodPressureTrend(String patientId, List<PatientRecord> bpRecords, String type) {
        if (bpRecords.size() < 3) return;

        for (int i = 2; i < bpRecords.size(); i++) {
            try {
                PatientRecord r1 = bpRecords.get(i - 2);
                PatientRecord r2 = bpRecords.get(i - 1);
                PatientRecord r3 = bpRecords.get(i);
                double v1 = r1.getMeasurementValue();
                double v2 = r2.getMeasurementValue();
                double v3 = r3.getMeasurementValue();

                // Increasing trend
                if (v2 - v1 > 10 && v3 - v2 > 10) {
                    triggerAlert(new Alert(patientId, "Trend: Three consecutive systolic BP increases > 10 mmHg",
                            r3.getTimestamp()));
                }
                // Decreasing trend
                if (v1 - v2 > 10 && v2 - v3 > 10) {
                    triggerAlert(new Alert(patientId, "Trend: Three consecutive systolic BP decreases > 10 mmHg",
                            r3.getTimestamp()));
                }
            } catch (Exception e) {
                System.err.println("Error parsing BloodPressure trend at index " + i);
            }
        }
    }

    private void checkBloodSaturationAlerts(String patientId, List<PatientRecord> records) {
        // Low Saturation Alert
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("BloodSaturation")) {
                double saturation = record.getMeasurementValue();
                if (saturation < 92) {
                    triggerAlert(new Alert(patientId, "Low Blood Saturation: Below 92%", record.getTimestamp()));
                }
            }
        }

        // Rapid Drop Alert (5% drop within 10 minutes)
        for (int i = 1; i < records.size(); i++) {
            PatientRecord current = records.get(i);
            PatientRecord previous = records.get(i - 1);
            if (current.getRecordType().equals("BloodSaturation") &&
                    previous.getRecordType().equals("BloodSaturation")) {
                if (current.getTimestamp() - previous.getTimestamp() <= TEN_MINUTES &&
                        previous.getMeasurementValue() - current.getMeasurementValue() >= 5) {
                    triggerAlert(new Alert(patientId, "Rapid Blood Saturation Drop: 5% or more in 10 minutes",
                            current.getTimestamp()));
                }
            }
        }
    }

    private void checkHypotensiveHypoxemiaAlert(String patientId, List<PatientRecord> records) {
        for (PatientRecord bpRecord : records) {
            if (bpRecord.getRecordType().equals("BloodPressure")) {
                try {
                    double systolic = bpRecord.getMeasurementValue();
                    if (systolic < 90) {
                        // Check for low saturation within 1 minute
                        for (PatientRecord satRecord : records) {
                            if (satRecord.getRecordType().equals("BloodSaturation") &&
                                    Math.abs(satRecord.getTimestamp() - bpRecord.getTimestamp()) < 60_000) {
                                if (satRecord.getMeasurementValue() < 92) {
                                    triggerAlert(new Alert(patientId, "Hypotensive Hypoxemia: Low BP and Low Saturation",
                                            bpRecord.getTimestamp()));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing BloodPressure for Hypotensive Hypoxemia: " + bpRecord.getMeasurementValue());
                }
            }
        }
    }

    private void checkECGAlerts(String patientId, List<PatientRecord> records) {
        List<Double> ecgValues = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>();
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("ECG")) {
                ecgValues.add(record.getMeasurementValue());
                timestamps.add(record.getTimestamp());
            }
        }

        // Sliding window for moving average
        for (int i = SLIDING_WINDOW_SIZE - 1; i < ecgValues.size(); i++) {
            double sum = 0;
            for (int j = i - SLIDING_WINDOW_SIZE + 1; j <= i; j++) {
                sum += ecgValues.get(j);
            }
            double average = sum / SLIDING_WINDOW_SIZE;
            double current = ecgValues.get(i);
            // Threshold: 2x the average for a peak
            if (current > average * 2) {
                triggerAlert(new Alert(patientId, "Abnormal ECG Peak: Value " + current + " exceeds 2x average " + average,
                        timestamps.get(i)));
            }
        }
    }

    private void checkTriggeredAlerts(String patientId, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("Alert")) {
                String condition = record.getMeasurementValue() == 1.0 ? "Triggered" : "Untriggered";
                triggerAlert(new Alert(patientId, "Manual Alert: " + condition, record.getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        // Log to console for now; could notify staff or log to a file
        System.out.println("ALERT: Patient ID: " + alert.getPatientId() + ", Condition: " + alert.getCondition() +
                ", Timestamp: " + alert.getTimestamp());
    }
}