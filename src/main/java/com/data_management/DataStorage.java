package com.data_management;

import com.alerts.AlertGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring system.
 */
public class DataStorage {
    private final Map<Integer, Patient> patientMap; // Stores patient objects indexed by their unique patient ID.
    private final ReentrantReadWriteLock lock; // Ensures thread-safe access to patientMap.
    private DataReader reader; // Add field to store the DataReader
    private WebSocketOutputStrategy outputStrategy; // Add field for WebSocketOutputStrategy
    private static DataStorage instance; // Singleton instance

    private DataStorage() {
        this.patientMap = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    private DataStorage(DataReader reader) {
        this();
        this.reader = reader;
    }

    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    public static synchronized DataStorage getInstance(DataReader reader) {
        if (instance == null) {
            instance = new DataStorage(reader);
        } else {
            instance.reader = reader;
        }
        return instance;
    }

    public void readData() throws IOException {
        if (reader != null) {
            reader.readData(this);
        } else {
            System.out.println("No DataReader configured for DataStorage.");
        }
    }

    public void startStreaming() {
        if (reader != null) {
            reader.startStreaming(this);
        } else {
            System.out.println("No DataReader configured for DataStorage.");
        }
    }

    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        lock.writeLock().lock();
        try {
            Patient patient = patientMap.computeIfAbsent(patientId, id -> new Patient(id));
            patient.addRecord(measurementValue, recordType, timestamp);

            // Send data via WebSocketOutputStrategy if initialized
            if (outputStrategy != null) {
                outputStrategy.output(patientId, timestamp, recordType, String.valueOf(measurementValue));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Patient getPatient(int patientId) {
        lock.readLock().lock();
        try {
            return patientMap.get(patientId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Patient> getAllPatients() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(patientMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            patientMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java DataStorage <websocket_uri>");
            System.exit(1);
        }

        URI serverUri = new URI(args[0]);
        DataReader reader = new WebSocketClientImpl(serverUri);
        DataStorage storage = DataStorage.getInstance(reader);

        // Initialize WebSocketOutputStrategy
        storage.outputStrategy = new WebSocketOutputStrategy(8080);

        storage.startStreaming();

        AlertGenerator alertGenerator = new AlertGenerator(storage, new ConsoleOutputStrategy());
        while (true) {
            for (Patient patient : storage.getAllPatients()) {
                alertGenerator.evaluateData(patient);
            }
            Thread.sleep(1000);
        }
    }    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        List<PatientRecord> records = new ArrayList<>();
        Patient patient = getPatient(patientId);

        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }

        return records;
    }

    // Utility method to check if a port is in use
    private static boolean isPortInUse(int port) {
        try (var socket = new java.net.ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}