package com.data_management;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataStorageTest {
    @Test
    void testAddAndGetPatientData() {
        DataStorage storage = DataStorage.getInstance(); // Use Singleton
        storage.addPatientData(1, 120.0, "BloodPressure", System.currentTimeMillis());
        Patient patient = storage.getPatient(1);
        assertNotNull(patient);
        assertEquals(1, patient.getPatientId());
    }
}