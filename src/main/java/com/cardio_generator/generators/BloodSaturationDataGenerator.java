
package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * A class that makes fake blood saturation data for patients.
 * This class makes pretend blood saturation numbers for patients in the health simulator. It
 * follows the PatientDataGenerator rule to create data and send it to an output.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {

    /** Makes random numbers to change saturation values. */
    private static final Random random = new Random();

    /** Stores the last saturation value for each patient. */
    private int[] lastSaturationValues;

    /**
     * Sets up the starting saturation values for all patients.
     * This method makes an array to store saturation values for each patient. It starts with
     * values between 95 and 100 for each patient.
     *
     * @param patientCount the number of patients; must be positive
     * @throws IllegalArgumentException if patientCount is not positive
     */
    public BloodSaturationDataGenerator(int patientCount) {
        if (patientCount <= 0) {
            throw new IllegalArgumentException("Patient count must be positive");
        }
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Makes a new blood saturation value for a patient and sends it to the output.
     * This method makes a new saturation value by changing the last one a little. It keeps the
     * value between 90 and 100 to be realistic. Then it sends the data to the output.

     * @param patientId the patient number; must be positive and in range
     * @param outputStrategy where to send the data; cannot be null
     * @throws IllegalArgumentException if patientId is not valid or outputStrategy is null
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        if (patientId <= 0 || patientId >= lastSaturationValues.length) {
            throw new IllegalArgumentException("Patient ID must be positive and within range");
        }
        if (outputStrategy == null) {
            throw new IllegalArgumentException("Output strategy must not be null");
        }
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}