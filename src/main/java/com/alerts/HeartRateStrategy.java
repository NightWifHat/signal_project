package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the AlertStrategy to monitor heart rate data and generate alerts if the heart rate is outside normal bounds.
 * 
 */
public class HeartRateStrategy implements AlertStrategy {
    /**
     * Checks the patient's heart rate data and generates alerts if the heart rate is below 50 or above 100 beats per minute.
     *
     * @param patient the patient whose data is being evaluated
     * @param dataStorage the storage system containing patient records
     * @return a list of alerts generated based on heart rate anomalies
     */
    @Override
    public List<Alert> checkAlert(Patient patient, DataStorage dataStorage) {
        List<Alert> alerts = new ArrayList<>();
        int patientIdInt = patient.getPatientId();
        String patientId = String.valueOf(patientIdInt);
        long endTime = System.currentTimeMillis();
        List<PatientRecord> records = dataStorage.getRecords(patientIdInt, 0, endTime);

        for (PatientRecord record : records) {
            if (record.getRecordType().equals("HeartRate")) {
                double heartRate = record.getMeasurementValue();
                if (heartRate < 50) {
                    alerts.add(AlertFactory.getFactory("ecg").createAlert(patientId, "Abnormal Heart Rate: Low heart rate " + heartRate + " bpm", record.getTimestamp()));
                } else if (heartRate > 100) {
                    alerts.add(AlertFactory.getFactory("ecg").createAlert(patientId, "Abnormal Heart Rate: High heart rate " + heartRate + " bpm", record.getTimestamp()));
                }
            }
        }

        return alerts;
    }
}