
package com.cardio_generator.outputs;

/**
 * An interface for sending patient data in the health simulator project.
 */
public interface OutputStrategy {

    /**
     * Sends patient health data to an output location.
     *
     * @param patientId the patient number; must be a positive number
     * @param timestamp the time the data was made in milliseconds; must be a valid time
     * @param label the type of data like "HeartRate"; cannot be null or empty
     * @param data the data value like "72"; cannot be null
     * @throws IllegalArgumentException if patientId is not positive or if label or data is not valid
     * @throws IOException if there is a problem sending the data like a file error
     */
    void output(int patientId, long timestamp, String label, String data);
}