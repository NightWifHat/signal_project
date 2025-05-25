# WebSocket Real-Time Data Processing - Week 5 Implementation

 Overview

This document describes the Week 5 implementation that extends the signal processing system to support real-time data streaming via WebSocket APIs while preserving all existing batch processing functionality from Weeks 1-4.

New Features

### 1. WebSocket Data Reader (`WebSocketDataReader`)

A new `DataReader` implementation that connects to WebSocket servers for real-time patient data streaming.

Key Features:
- Real-time data processing from WebSocket servers
- Automatic reconnection on connection failures
- Thread-safe integration with existing `DataStorage`
- Error handling for malformed data and connection issues
- Support for both `ws://` and `wss://` protocols

**Usage Example:**
```java
// Create WebSocket data reader
URI serverUri = new URI("ws://localhost:8080");
WebSocketDataReader wsReader = new WebSocketDataReader(serverUri);

// Get DataStorage instance
DataStorage storage = DataStorage.getInstance(wsReader);

// Start real-time streaming
storage.startStreaming();

// The system will now continuously process incoming WebSocket messages
```

 2. Enhanced WebSocket Client (`WebSocketClientImpl`)

Improved WebSocket client implementation with:
- Robust error handling and logging
- Automatic reconnection attempts
- Support for the correct message format: `patientId,measurementValue,recordType,timestamp`
- Detailed connection state monitoring

Message Format:
```
patientId,measurementValue,recordType,timestamp
Example: 123,98.6,Temperature,1714376789050
```

 3. Thread-Safe Data Storage

The existing `DataStorage` class has been enhanced to support concurrent access from:
- WebSocket client threads (real-time data)
- File processing threads (batch data)
- Alert generation threads
- Main application threads

Concurrency Features:
- `ReentrantReadWriteLock` for thread-safe operations
- Concurrent patient data updates
- Safe access to patient records during real-time streaming

 Architecture Integration

 Preserved Functionality

All existing functionality from Weeks 1-4 remains fully operational:

1. **File-based Data Processing**: `FileDataReader` continues to work unchanged
2. **Batch Processing**: Existing batch processing workflows are unaffected
3. **Alert Generation**: Alert system works with both real-time and batch data
4. **Patient Management**: Patient data structure and management unchanged
5. **Output Strategies**: Console, TCP, and WebSocket output strategies preserved

New Real-Time Capabilities

1. **Real-Time Data Ingestion**: WebSocket messages are processed immediately
2. **Continuous Alert Monitoring**: Alerts are generated in real-time as data arrives
3. **Live Data Broadcasting**: Processed data can be broadcasted via WebSocket output
4. **Connection Management**: Automatic handling of connection failures and recovery

 Implementation Details

 Message Processing Flow

1. **WebSocket Connection**: Client connects to signal generator WebSocket server
2. **Message Reception**: Incoming messages are parsed in `onMessage()` callback
3. **Data Validation**: Message format and data types are validated
4. **Storage**: Valid data is stored in thread-safe `DataStorage`
5. **Alert Processing**: Alert system evaluates new data for threshold violations
6. **Output Broadcasting**: Data can be broadcasted to connected clients

 Error Handling

- **Malformed Messages**: Invalid format messages are logged and skipped
- **Connection Failures**: Automatic reconnection attempts with exponential backoff
- **Data Type Errors**: NumberFormatException handling for invalid numeric data
- **Network Issues**: Connection state monitoring and recovery

Performance Considerations

- **Non-blocking Operations**: WebSocket operations don't block main application
- **Memory Management**: Efficient patient data storage and retrieval
- **Concurrent Access**: Optimized read/write locks for high-throughput scenarios

Testing Coverage

Comprehensive test suite includes:

 Unit Tests
- `WebSocketClientImplTest`: Tests message parsing, error handling, connection management
- `WebSocketDataReaderTest`: Tests wrapper functionality and interface compliance
- `WebSocketIntegrationTest`: Tests end-to-end real-time data flow

Test Scenarios
- Valid message processing
- Invalid message format handling
- Connection failure recovery
- Concurrent data storage operations
- Thread safety verification
- Integration with alert generation
- Compatibility with existing file processing

Configuration and Usage

 Signal Generator Configuration

To use WebSocket data streaming, run the signal generator with:
```bash
java -jar signal_generator.jar -output websocket 8080
```

Application Configuration

```java
// Real-time WebSocket processing
URI serverUri = new URI("ws://localhost:8080");
DataReader reader = new WebSocketDataReader(serverUri);
DataStorage storage = DataStorage.getInstance(reader);
storage.startStreaming();

// Continue with alert generation
AlertGenerator alertGenerator = new AlertGenerator(storage, outputStrategy);
// Process alerts in real-time...
```

 Hybrid Processing (Batch + Real-time)

The system supports both batch and real-time processing simultaneously:

```java
// Process historical data from files
FileDataReader fileReader = new FileDataReader("historical_data.txt");
DataStorage storage = DataStorage.getInstance();
storage.readData(); // Batch processing

// Add real-time streaming
WebSocketDataReader wsReader = new WebSocketDataReader("ws://localhost:8080");
storage = DataStorage.getInstance(wsReader);
storage.startStreaming(); // Real-time processing

// Both data sources feed into the same storage and alert system
```

Dependencies

The implementation uses the Java-WebSocket library:

```xml
<dependency>
    <groupId>org.java-websocket</groupId>
    <artifactId>Java-WebSocket</artifactId>
    <version>1.5.2</version>
</dependency>
```

 
This implementation successfully bridges batch and real-time processing paradigms while maintaining the robustness and reliability of the existing system.
