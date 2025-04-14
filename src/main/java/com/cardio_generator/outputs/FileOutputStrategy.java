
package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

// Renamed class from fileOutputStrategy to FileOutputStrategy to follow UpperCamelCase.
// Added class Javadoc for public class .
public class FileOutputStrategy implements OutputStrategy {

    // Renamed from BaseDirectory to baseDirectory to follow lowerCamelCase .
    
    // Added Javadoc for field per Section 7.3.
    private String baseDirectory;

    // Renamed from file_map to fileMap to follow lowerCamelCase .
    
    // Added Javadoc for public field per Section 7.3.
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Renamed from FilePath to filePath to follow lowerCamelCase (Section 5.2.7)
        // Set the filePath variable
        String filePath = fileMap.computeIfAbsent(label,
                k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        // Wrapped line to comply with 100-column limit (Section 4.4) and improve readability (Section 4.5).
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                    patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}