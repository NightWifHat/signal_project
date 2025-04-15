
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
    
    // Added Javadoc for field (Section 7.3).
    private String baseDirectory;

    // Renamed from file_map to fileMap to follow lowerCamelCase .
    
    // Added Javadoc for public field per Section 7.3.
    /** A map that keeps track of file paths for each data type. */
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

     /**
     * Makes a new FileOutputStrategy with a folder to save files.
     * This method sets the folder where we save the data files. It checks if the folder name is valid.
     *
     * @param baseDirectory the folder to save files in; cannot be null or empty
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    
     /**
     * Writes patient data to a file for the data type.
     * This method saves the data in a file named after the data type. It makes the folder if it does
     * not exist. The data is added to the file with the patient ID, time, label, and data value.
     *
     * @param patientId the patient number; must be positive
     * @param timestamp the time the data was made; must be a valid time
     * @param label the type of data like "HeartRate"; cannot be null or empty
     * @param data the data value; cannot be null
     * @throws IllegalArgumentException if label or data is not valid
     * @throws IOException if there is a problem making the folder or writing to the file
     */
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