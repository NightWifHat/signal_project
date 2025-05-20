package com.cardio_generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.BloodPressureDataGenerator;

import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;


import com.cardio_generator.generators.PatientDataGenerator; // Added missing import
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

/**
 * This class makes fake health data for patients and shows it in different ways.
 * <pre>
 * java HealthDataSimulator --patient-count 100 --output file:./output
 * </pre>
 * That makes data for 100 patients and puts it in files in the "output" folder.
 */
public class HealthDataSimulator {

    /** How many patients we are making data for; it starts at 50 unless we change it. */
    private static int patientCount = 50;

    /** This thing runs our tasks on a schedule. */
    private static ScheduledExecutorService scheduler;

    /** Where the data goes-like to the console or a file; starts with console. */
    private static OutputStrategy outputStrategy = new ConsoleOutputStrategy();

    /** A random number randomizer to mix things up when we start tasks. */
    private static final Random random = new Random();

    private static HealthDataSimulator instance;
    private PatientDataGenerator[] generators;

    private HealthDataSimulator() {
        // Initialize generators
        generators = new PatientDataGenerator[] {
            new ECGDataGenerator(patientCount),
            new BloodSaturationDataGenerator(patientCount),


            new BloodPressureDataGenerator(patientCount),
            new BloodLevelsDataGenerator(patientCount),

            new AlertGenerator(patientCount)
        };
    }

    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    /**
     * Starts the whole program and sets everything up.
     * This is the main method we run to start making fake health data It looks at what we type
     * in the command line (like how many patients) and gets everything ready. I had to look up
     * what "args" means-it is the stuff we type when we run the program. It can make files if we
     * pick file output, which might cause an error, so I added that in the notes!
     *
     * @param args stuff we type in the command line, like "--patient-count 100"
     * @throws IOException if something goes wrong making folders for file output
     */
    public static void main(String[] args) throws IOException {
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        simulator.start(args);
    }

    public void start(String[] args) throws IOException {
        parseArguments(args);

        scheduler = Executors.newScheduledThreadPool(patientCount * 4);

        List<Integer> patientIds = initializePatientIds(patientCount);

        Collections.shuffle(patientIds); // Randomize the order of patient IDs

        scheduleTasksForPatients(patientIds);
    }

    /**
     * Looks at what we typed in the command line and sets up the program.
     * This method checks what we typed, like how many patients or where to send the data.
     *
     * @param args the words we typed in the command line
     * @throws IOException if it cant make a folder for file output
     */
    private void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {

                        try {
                            patientCount = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid number of patients. Using default value: " + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                outputStrategy = new WebSocketOutputStrategy(port);
                                System.out.println("WebSocket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for WebSocket output. Please specify a valid port number.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                outputStrategy = new TcpOutputStrategy(port);
                                System.out.println("TCP socket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for TCP output. Please specify a valid port number.");
                            }
                        } else {
                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }

    /**
     * Shows how to use the program if we need help.
     * This method prints a big help message to the screen! It tells us how to run the program
     * and what options we can use, like how many patients or where the data goes. It prints stuff
     * to the screen, which is the only thing it does besides that.
     */
    private void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("Options:");
        System.out.println("  -h                       Show help and exit.");
        System.out.println(
                "  --patient-count <count>  Specify the number of patients to simulate data for (default: 50).");
        System.out.println("  --output <type>          Define the output method. Options are:");
        System.out.println("                             'console' for console output,");
        System.out.println("                             'file:<directory>' for file output,");
        System.out.println("                             'websocket:<port>' for WebSocket output,");
        System.out.println("                             'tcp:<port>' for TCP socket output.");
        System.out.println("Example:");
        System.out.println("  java HealthDataSimulator --patient-count 100 --output websocket:8080");
        System.out.println(
                "  This command simulates data for 100 patients and sends the output to WebSocket clients connected to port 8080.");
    }

    /**
     * Makes a list of patient IDs to use in the simulation.
     *
     * @param patientCount how many patients we want; gotta be a positive number
     * @return a list of numbers from 1 to patientCount for the patients
     * @throws IllegalArgumentException if patientCount is 0 or less 
     */
    private List<Integer> initializePatientIds(int patientCount) {

        if (patientCount <= 0) {
            throw new IllegalArgumentException("Patient count must be positive");
        }
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }

    /**
     * Sets up all the tasks to make data for each patient.
     *
     * @param patientIds the list of patient numbers we're making data for; can't be null or empty
     * @throws IllegalArgumentException if the patientIds list is null or empty
     */
    private void scheduleTasksForPatients(List<Integer> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            throw new IllegalArgumentException("Patient IDs list must not be null or empty");
        }
        for (int patientId : patientIds) {
            scheduleTask(() -> generators[0].generate(patientId, outputStrategy), 1, TimeUnit.SECONDS); // ECG
            scheduleTask(() -> generators[1].generate(patientId, outputStrategy), 1, TimeUnit.SECONDS); // Blood Saturation
            scheduleTask(() -> generators[2].generate(patientId, outputStrategy), 1, TimeUnit.MINUTES); // Blood Pressure
            scheduleTask(() -> generators[3].generate(patientId, outputStrategy), 2, TimeUnit.MINUTES); // Blood Levels
            scheduleTask(() -> generators[4].generate(patientId, outputStrategy), 20, TimeUnit.SECONDS); // Alerts
        }
    }

    /**
     * Plans a task to run over and over with a little random wait at the start.
     *
     * @param task the job we want to do, like making data cant be null
     * @param period how often to do the task, like every 1 second; has to be a positive number
     * @param timeUnit what kind of time we're using, like seconds or minutes; can't be null
     * @throws IllegalArgumentException if task or timeUnit is null, or if period isn't positive
     */
    private void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        if (task == null) {

            throw new IllegalArgumentException("Task must not be null");
        }
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be positive");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit must not be null");
        }
        scheduler.scheduleAtFixedRate(task, random.nextInt(5), period, timeUnit);
    }
}