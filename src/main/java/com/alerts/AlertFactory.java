package com.alerts;

public abstract class AlertFactory {
    public abstract Alert createAlert(String patientId, String condition, long timestamp);

	//sava pls explain
    public static AlertFactory getFactory(String alertType) {

        switch (alertType.toLowerCase()) {
            case "bloodpressure":
                return new BloodPressureAlertFactory();
            case "bloodoxygen":
                return new BloodOxygenAlertFactory();
            case "ecg":
                return new ECGAlertFactory();
            default:

                throw new IllegalArgumentException("Unknown alert type: " + alertType);
        }
    }
}