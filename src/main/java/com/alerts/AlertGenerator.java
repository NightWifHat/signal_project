package com.alerts;

import com.cardio_generator.outputs.OutputStrategy; // Added import
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

public class AlertGenerator {
    private DataStorage dataStorage;
    private OutputStrategy outputStrategy; // Added field
    private static final long TEN_MINUTES = 10 * 60 * 1000;
    private static final int SLIDING_WINDOW_SIZE = 5;

    public AlertGenerator(DataStorage dataStorage, OutputStrategy outputStrategy) {
        this.dataStorage = dataStorage;
        this.outputStrategy = outputStrategy; // Store the OutputStrategy
    }

    public void evaluateData(Patient patient) {
        int patientIdInt = patient.getPatientId();
        String patientId = String.valueOf(patientIdInt);
        long endTime = System.currentTimeMillis();
        List<PatientRecord> records = dataStorage.getRecords(patientIdInt, 0, endTime);

        checkBloodPressureAlerts(patientId, records);
        checkBloodSaturationAlerts(patientId, records);
        checkHypotensiveHypoxemiaAlert(patientId, records);
        checkECGAlerts(patientId, records);
        checkTriggeredAlerts(patientId, records);
    }

    private void checkBloodPressureAlerts(String patientId, List<PatientRecord> records) {
        // Critical Threshold Alerts for Systolic BP
        List<PatientRecord> systolicRecords = new ArrayList<>();
        List<PatientRecord> diastolicRecords = new ArrayList<>();
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("BloodPressure")) {
                systolicRecords.add(record);
                double systolic = record.getMeasurementValue();
                if (systolic > 180) {
                    triggerAlert(new Alert(patientId, "Critical: Systolic BP above 180 mmHg", record.getTimestamp()));
                } else if (systolic < 90) {
                    triggerAlert(new Alert(patientId, "Critical: Systolic BP below 90 mmHg", record.getTimestamp()));
                }
            } else if (record.getRecordType().equals("DiastolicBloodPressure")) {
                diastolicRecords.add(record);
                double diastolic = record.getMeasurementValue();
                if (diastolic > 110) {
                    triggerAlert(new Alert(patientId, "Critical: Diastolic BP above 110 mmHg", record.getTimestamp()));
                } else if (diastolic < 60) {
                    triggerAlert(new Alert(patientId, "Critical: Diastolic BP below 60 mmHg", record.getTimestamp()));
                }
            }
        }

        // Trend Alerts for Systolic BP
        checkBloodPressureTrend(patientId, systolicRecords, "systolic");
        // Trend Alerts for Diastolic BP
        checkBloodPressureTrend(patientId, diastolicRecords, "diastolic");
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
                    triggerAlert(new Alert(patientId, "Trend: Three consecutive " + type + " BP increases > 10 mmHg",
                            r3.getTimestamp()));
                }
                // Decreasing trend
                if (v1 - v2 > 10 && v2 - v3 > 10) {
                    triggerAlert(new Alert(patientId, "Trend: Three consecutive " + type + " BP decreases > 10 mmHg",
                            r3.getTimestamp()));
                }
            } catch (Exception e) {
                System.err.println("Error parsing " + type + " BP trend at index " + i);
            }
        }
    }

    private void checkBloodSaturationAlerts(String patientId, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("BloodSaturation")) {
                double saturation = record.getMeasurementValue();
                if (saturation < 92) {
                    triggerAlert(new Alert(patientId, "Low Blood Saturation: Below 92%", record.getTimestamp()));
                }
            }
        }

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
                double systolic = bpRecord.getMeasurementValue();
                if (systolic < 90) {
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

        for (int i = SLIDING_WINDOW_SIZE - 1; i < ecgValues.size(); i++) {
            double sum = 0;
            for (int j = i - SLIDING_WINDOW_SIZE + 1; j <= i; j++) {
                sum += ecgValues.get(j);
            }
            double average = sum / SLIDING_WINDOW_SIZE;
            double current = ecgValues.get(i);
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

    private void triggerAlert(Alert alert) {
        outputStrategy.output(Integer.parseInt(alert.getPatientId()), alert.getTimestamp(),
                "ALERT: Patient ID: " + alert.getPatientId() + ", Condition: " + alert.getCondition(),
                alert.getCondition());
    }
}