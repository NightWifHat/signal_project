# Cardio Data Simulator

The Cardio Data Simulator is a Java-based application designed to simulate real-time cardiovascular data for multiple patients. This tool is particularly useful for educational purposes, enabling students to interact with real-time data streams of ECG, blood pressure, blood saturation, and other cardiovascular signals.

## Features

- Simulate real-time ECG, blood pressure, blood saturation, and blood levels data.
- Supports multiple output strategies:
  - Console output for direct observation.
  - File output for data persistence.
  - WebSocket and TCP output for networked data streaming.
- Configurable patient count and data generation rate.
- Randomized patient ID assignment for simulated data diversity.

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven for managing dependencies and compiling the application.

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/tpepels/signal_project.git
   ```

2. Navigate to the project directory:

   ```sh
   cd signal_project
   ```

3. Compile and package the application using Maven:
   ```sh
   mvn clean package
   ```
   This step compiles the source code and packages the application into an executable JAR file located in the `target/` directory.

### Running the Simulator

After packaging, you can run the simulator directly from the executable JAR:

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar
```

To run with specific options (e.g., to set the patient count and choose an output strategy):

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output
```

### Supported Output Options

- `console`: Directly prints the simulated data to the console.
- `file:<directory>`: Saves the simulated data to files within the specified directory.
- `websocket:<port>`: Streams the simulated data to WebSocket clients connected to the specified port.
- `tcp:<port>`: Streams the simulated data to TCP clients connected to the specified port.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Project Members
- Student ID: I6395767
- Student ID: I6385825

## UML Models (Week 2)

For Week 2, we created UML class diagrams for four subsystems of the Cardio Data Simulator:

- **Alert Generation System**: Handles real-time alert generation based on patient data thresholds.
- **Data Storage System**: Manages secure storage, retrieval, and deletion of patient data.
- **Patient Identification System**: Links incoming data to the correct patient records.
- **Data Access Layer**: Retrieves and parses data from external sources (TCP, WebSocket, files).

The diagrams and their explanations are in the [uml_models](uml_models/) directory.

## Week 3 Updates
For Week 3, we implemented the core functionalities of the Cardio Generator Project, including:
- **Alert Generation**: Implemented `AlertGenerator` to detect critical conditions (e.g., high/low blood pressure, low saturation, ECG peaks) and generate alerts using the `OutputStrategy` pattern.
- **Data Processing**: Enhanced `FileDataReader` and `Patient` classes to handle patient data parsing and validation, including throwing exceptions for malformed data and invalid time ranges.
- **Testing**: Added and fixed unit tests across all components (`AlertGeneratorTest`, `MainTest`, `FileDataReaderTest`, `PatientTest`, etc.), achieving a total of 27 passing tests.
- **Code Coverage**: Generated a JaCoCo report showing 41% instruction coverage and 48% branch coverage (see `coverage_report.txt` for details).
- **Verification**: Documented test results in `test_verification.txt`, confirming all tests pass with no failures.
