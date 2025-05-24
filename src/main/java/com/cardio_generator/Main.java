package com.cardio_generator;

import com.cardio_generator.outputs.*;
import com.data_management.*;
import com.alerts.AlertGenerator; // missing  import
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java com.cardio_generator.Main <mode> [data-file-path]");
            System.err.println("Modes: DataStorage, HealthDataSimulator, ProcessFile");
            System.exit(1);
        }

        String mode = args[0];
        switch (mode) {
            case "DataStorage":
                try {
                    DataStorage.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                break;
            case "HealthDataSimulator":
                HealthDataSimulator.main(args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[]{});
                break;
            case "ProcessFile":
                if (args.length != 2) {
                    System.err.println("Usage for ProcessFile: java com.cardio_generator.Main ProcessFile <data-file-path>");
                    System.exit(1);
                }
                processFile(args[1]);
                break;
            default:
                System.err.println("Unknown mode: " + mode);
                System.err.println("Modes: DataStorage, HealthDataSimulator, ProcessFile");
                System.exit(1);
        }
    }

    private static void processFile(String filePath) throws IOException {
        DataStorage dataStorage = DataStorage.getInstance(); // Use Singleton
        FileDataReader reader = new FileDataReader(filePath); // Pass the file path to the constructor
        reader.readData(dataStorage); // Call readData with only DataStorage

        OutputStrategy outputStrategy = new ConsoleOutputStrategy();
        AlertGenerator alertGenerator = new AlertGenerator(dataStorage, outputStrategy);
        for (Patient patient : dataStorage.getAllPatients()) {
            System.out.println("Evaluating patient: " + patient.getPatientId());
            alertGenerator.evaluateData(patient);
        }
    }
}