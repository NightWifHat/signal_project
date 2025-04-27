package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PatientTest {
    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(1);
    }

    @Test
    void testGetRecordsWithinTimeRange() {
        patient.addRecord(new PatientRecord(1, 100.0, "BloodPressure", 1000L));
        patient.addRecord(new PatientRecord(1, 110.0, "BloodPressure", 2000L));
        patient.addRecord(new PatientRecord(1, 120.0, "BloodPressure", 3000L));

        List<PatientRecord> records = patient.getRecords(1500L, 2500L);
        assertEquals(1, records.size());
        assertEquals(2000L, records.get(0).getTimestamp());
    }

    @Test
    void testGetRecordsNoRecordsInRange() {
        patient.addRecord(new PatientRecord(1, 100.0, "BloodPressure", 1000L));

        List<PatientRecord> records = patient.getRecords(2000L, 3000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsInvalidTimeRange() {
        patient.addRecord(new PatientRecord(1, 100.0, "BloodPressure", 1000L));

        assertThrows(IllegalArgumentException.class, () -> {
            patient.getRecords(3000L, 2000L);
        }, "Expected IllegalArgumentException for invalid time range (start > end)");
    }
}