package com.alerts;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class AlertGeneratorTest {
    private DataStorage storage;
    private AlertGenerator alertGenerator;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws IOException {
        storage = new DataStorage(new FileDataReader("src/test/resources/test_data.txt"));
        storage.readData();
        alertGenerator = new AlertGenerator(storage);
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testBloodPressureCriticalHighAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Critical: Systolic BP above 180 mmHg"),
                   "Expected critical high systolic blood pressure alert");
        outContent.reset();
    }

    @Test
    void testBloodPressureCriticalLowAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Critical: Systolic BP below 90 mmHg"),
                   "Expected critical low systolic blood pressure alert");
        outContent.reset();
    }

    @Test
    void testDiastolicBloodPressureCriticalHighAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Critical: Diastolic BP above 110 mmHg"),
                   "Expected critical high diastolic blood pressure alert");
        outContent.reset();
    }

    @Test
    void testDiastolicBloodPressureCriticalLowAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Critical: Diastolic BP below 60 mmHg"),
                   "Expected critical low diastolic blood pressure alert");
        outContent.reset();
    }

    @Test
    void testBloodPressureTrendAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Trend: Three consecutive systolic BP increases > 10 mmHg"),
                   "Expected systolic blood pressure trend alert");
        outContent.reset();
    }

    @Test
    void testDiastolicBloodPressureTrendAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Trend: Three consecutive diastolic BP increases > 10 mmHg"),
                   "Expected diastolic blood pressure trend alert");
        outContent.reset();
    }

    @Test
    void testLowBloodSaturationAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Low Blood Saturation: Below 92%"),
                   "Expected low blood saturation alert");
        outContent.reset();
    }

    @Test
    void testRapidSaturationDropAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Rapid Blood Saturation Drop: 5% or more in 10 minutes"),
                   "Expected rapid saturation drop alert");
        outContent.reset();
    }

    @Test
    void testHypotensiveHypoxemiaAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Hypotensive Hypoxemia: Low BP and Low Saturation"),
                   "Expected hypotensive hypoxemia alert");
        outContent.reset();
    }

    @Test
    void testECGAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Abnormal ECG Peak"),
                   "Expected ECG peak alert");
        outContent.reset();
    }

    @Test
    void testTriggeredAlert() {
        Patient patient = storage.getAllPatients().get(0);
        alertGenerator.evaluateData(patient);
        String output = outContent.toString();
        assertTrue(output.contains("Manual Alert: Triggered"),
                   "Expected triggered alert");
        outContent.reset();
    }

    @org.junit.jupiter.api.AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }
}