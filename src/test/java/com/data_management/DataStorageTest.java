package com.data_management;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataStorageTest {

    @Test
    void testAddAndGetRecords() throws IOException {
        // Use a direct path to src/test/resources/test_data.txt
        String filePath = "src/test/resources/test_data.txt";
        DataStorage storage = new DataStorage(new FileDataReader(filePath));
        storage.readData();

        List<Patient> patients = storage.getAllPatients();
        assertEquals(1, patients.size(), "Expected one patient");

        Patient patient = patients.get(0);
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);
        assertEquals(13, records.size(), "Expected 13 records from test_data.txt");
    }
}