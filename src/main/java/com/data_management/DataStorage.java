package com.data_management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alerts.AlertGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring system.
 * This class serves as a repository for all patient records, organized by patient IDs.
 */
public class DataStorage {
    private Map<Integer, Patient> patientMap; // Stores patient objects indexed by their unique patient ID.
    private DataReader reader; // Add field to store the DataReader
    private static DataStorage instance; // Singleton instance

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the underlying storage structure.
     */
    private DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Constructs a new instance of DataStorage with a specified DataReader.
     * Initializes the underlying storage structure and sets the DataReader for reading data.
     *
     * @param reader the DataReader used to read patient data into this storage
     */
    private DataStorage(DataReader reader) {
        this.patientMap = new HashMap<>();
        this.reader = reader;
    }

    /**
     * Provides access to the singleton instance of DataStorage.
     * If no instance exists, it creates one with no DataReader.
     *
     * @return the singleton instance of DataStorage
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    /**
     * Provides access to the singleton instance of DataStorage with a specified DataReader.
     * If no instance exists, it creates one with the given DataReader.
     *
     * @param reader the DataReader used to read patient data into this storage
     * @return the singleton instance of DataStorage
     */
    public static synchronized DataStorage getInstance(DataReader reader) {
        if (instance == null) {
            instance = new DataStorage(reader);
        } else {
            instance.reader = reader; // Update reader if instance already exists
        }
        return instance;
    }

    /**
     * Reads data into this storage using the configured DataReader.
     *
     * @throws IOException if there is an error reading the data
     */
    public void readData() throws IOException {
        if (reader != null) {
            reader.readData(this);
        } else {
            System.out.println("No DataReader configured for DataStorage.");
        }
    }

    /**
     * Adds or updates patient data in the storage.
     * If the patient does not exist, a new Patient object is created and added to the storage.
     * Otherwise, the new data is added to the existing patient's records.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g., "HeartRate", "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            patient = new Patient(patientId);
            patientMap.put(patientId, patient);
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a Patient object for a specific patient ID.
     *
     * @param patientId the unique identifier of the patient
     * @return the Patient object, or null if not found
     */
    public Patient getPatient(int patientId) {
        return patientMap.get(patientId);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered by a time range.
     *
     * @param patientId the unique identifier of the patient whose records are to be retrieved
     * @param startTime the start of the time range, in milliseconds since the Unix epoch
     * @param endTime   the end of the time range, in milliseconds since the Unix epoch
     * @return a list of PatientRecord objects that fall within the specified time range
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>(); // return an empty list if no patient is found
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * Clears all patient data from the storage.
     */
    public void clear() {
        patientMap.clear();
    }

    /**
     * The main method for the DataStorage class.
     * Initializes the system, reads data into storage using a FileDataReader, and continuously monitors
     * and evaluates patient data.
     *
     * @param args command line arguments, expects the file path as the first argument
     * @throws IOException if there is an error reading the data
     */
    public static void main(String[] args) throws IOException {
        // Check if a file path is provided as an argument
        if (args.length < 1) {
            System.err.println("Usage: java DataStorage <file_path>");
            System.exit(1);
        }

        // Initialize the DataReader with the provided file path
        String filePath = args[0];
        DataReader reader = new FileDataReader(filePath);
        DataStorage storage = DataStorage.getInstance(reader);

        // Read data from the file into storage
        storage.readData();

        // Example of using DataStorage to retrieve and print records for a patient
        List<PatientRecord> records = storage.getRecords(1, 1700000000000L, 1800000000000L);
        for (PatientRecord record : records) {
            System.out.println("Record for Patient ID: " + record.getPatientId() +
                    ", Type: " + record.getRecordType() +
                    ", Data: " + record.getMeasurementValue() +
                    ", Timestamp: " + record.getTimestamp());
        }

        // Initialize the AlertGenerator with the storage and an OutputStrategy
        OutputStrategy outputStrategy = new ConsoleOutputStrategy();
        AlertGenerator alertGenerator = new AlertGenerator(storage, outputStrategy);

        // Evaluate all patients' data to check for conditions that may trigger alerts
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }
    }
}