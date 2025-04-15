
package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * An interface for making pretend health data for patients in our project.

 */
public interface PatientDataGenerator {

    /**
     * Makes fake health data for a patient and sends it to an output.
     *
     * @param patientId the number of the patient we're making data for; should be a positive number
     * @param outputStrategy where we send the data, like a file or console; can't be null
     * @throws IllegalArgumentException if patientId isn't positive or if outputStrategy is null
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}