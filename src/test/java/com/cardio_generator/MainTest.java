package com.cardio_generator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private SecurityManager originalSecurityManager;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        // Save the original SecurityManager
        originalSecurityManager = System.getSecurityManager();
        // Set a custom SecurityManager to prevent System.exit
        System.setSecurityManager(new NoExitSecurityManager());
    }

    @Test
    void testMainWithValidFile(@TempDir Path tempDir) throws IOException {
        // Create a temporary test file
        Path tempFile = tempDir.resolve("test_data.txt");
        Files.write(tempFile, List.of(
            "1,190.0,BloodPressure,1714376789050",
            "1,120.0,DiastolicBloodPressure,1714376789050"
        ));

        try {
            Main.main(new String[]{"ProcessFile", tempFile.toString()}); // Added "ProcessFile" mode
        } catch (ExitException e) {
            fail("Main should not call System.exit with valid arguments");
        }

        String output = outContent.toString();
        assertTrue(output.contains("Evaluating patient: 1"), "Expected patient evaluation message");
        assertTrue(output.contains("Critical: Systolic BP above 180 mmHg"), "Expected systolic BP alert");
        assertTrue(output.contains("Critical: Diastolic BP above 110 mmHg"), "Expected diastolic BP alert");
        outContent.reset();
    }

    @Test
    void testMainWithInvalidArgs() {
        // Redirect System.err to capture error messages
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try {
            Main.main(new String[]{"InvalidMode"}); // Use an invalid mode to trigger usage message
            fail("Expected ExitException due to invalid arguments");
        } catch (ExitException e) {
            assertEquals(1, e.status, "Expected exit code 1 for invalid arguments");
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Modes: DataStorage, HealthDataSimulator, ProcessFile"),
                   "Expected usage message with invalid args");

        // Restore System.err
        System.setErr(originalErr);
        outContent.reset();
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setSecurityManager(originalSecurityManager);
    }

    // Custom SecurityManager to prevent System.exit
    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(java.security.Permission perm) {
            // Allow all permissions except exit
        }

        @Override
        public void checkExit(int status) {
            throw new ExitException(status);
        }
    }

    // Custom exception to capture the exit status
    private static class ExitException extends SecurityException {
        public final int status;

        public ExitException(int status) {
            super("System.exit called with status: " + status);
            this.status = status;
        }
    }
}