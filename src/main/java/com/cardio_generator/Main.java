package com.cardio_generator;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // Check for file path argument
        if (args.length != 1) {
            System.err.println("Usage: java com.cardio_generator.Main <data-file-path>");
            System.exit(1);
        }

        String filePath = args[0];
        // Initialize DataStorage with FileDataReader
        DataStorage storage = new DataStorage(new FileDataReader(filePath));
        storage.readData();

        // Initialize AlertGenerator
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // Evaluate data for all patients
        for (Patient patient : storage.getAllPatients()) {
            System.out.println("Evaluating patient: " + patient.getPatientId());
            alertGenerator.evaluateData(patient);
        }
    }
}