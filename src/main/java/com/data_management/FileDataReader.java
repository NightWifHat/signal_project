package com.data_management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A DataReader implementation that reads patient data from a file.
 */
public class FileDataReader implements DataReader {
    @Override
    public void startStreaming(DataStorage dataStorage) {
        throw new UnsupportedOperationException("Streaming is not supported for FileDataReader.");
    }

    private final String filePath;

    /**
     * Constructs a FileDataReader to read data from the specified file.
     *
     * @param filePath the path to the file containing patient data
     */
    public FileDataReader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads data from the file and stores it in the provided DataStorage.
     * Expected file format per line: patientId,measurementValue,recordType,timestamp
     * Example: 1,100.0,WhiteBloodCells,1714376789050
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the file
     * @throws IllegalArgumentException if the data format is invalid
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path path = Paths.get(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Parse the line
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Invalid data format in line: " + line);
                }
                try {
                    int patientId = Integer.parseInt(parts[0].trim());
                    double measurementValue = Double.parseDouble(parts[1].trim());
                    String recordType = parts[2].trim();
                    long timestamp = Long.parseLong(parts[3].trim());
                    // Add the record to DataStorage
                    dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Error parsing line: " + line + " - " + e.getMessage(), e);
                }
            }
        }
    }
}