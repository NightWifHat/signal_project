package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileDataReaderTest {
    private DataStorage dataStorage;
    private FileDataReader fileDataReader;

    @BeforeEach
    void setUp() {
        dataStorage = new DataStorage();
        fileDataReader = new FileDataReader("dummy_path.txt"); // Fixed constructor call
    }

    @Test
    void testReadDataFromValidFile(@TempDir Path tempDir) throws IOException {
        // Create a temporary test file
        Path tempFile = tempDir.resolve("test_data.txt");
        Files.write(tempFile, List.of(
            "1,190.0,BloodPressure,1714376789050",
            "1,120.0,DiastolicBloodPressure,1714376789050"
        ));

        fileDataReader = new FileDataReader(tempFile.toString());
        fileDataReader.readData(dataStorage); // Fixed readData call

        List<PatientRecord> records = dataStorage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals(190.0, records.get(0).getMeasurementValue());
        assertEquals("BloodPressure", records.get(0).getRecordType());
        assertEquals(120.0, records.get(1).getMeasurementValue());
        assertEquals("DiastolicBloodPressure", records.get(1).getRecordType());
    }

    @Test
    void testReadDataWithInvalidFile() {
        fileDataReader = new FileDataReader("nonexistent_file.txt");
        assertThrows(IOException.class, () -> {
            fileDataReader.readData(dataStorage); // Fixed readData call
        }, "Expected IOException for nonexistent file");
    }

    @Test
    void testReadDataWithMalformedData(@TempDir Path tempDir) throws IOException {
        // Create a temporary test file with malformed data
        Path tempFile = tempDir.resolve("malformed_data.txt");
        Files.write(tempFile, List.of(
            "1,abc,BloodPressure,1714376789050" // Invalid measurement value
        ));

        fileDataReader = new FileDataReader(tempFile.toString());
        assertThrows(IllegalArgumentException.class, () -> {
            fileDataReader.readData(dataStorage); // Fixed readData call
        }, "Expected IllegalArgumentException for malformed data");
    }
}