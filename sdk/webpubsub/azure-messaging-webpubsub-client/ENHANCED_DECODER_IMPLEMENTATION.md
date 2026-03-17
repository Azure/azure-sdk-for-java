# Enhanced WebPubSub Client Decoder Implementation

## Overview

This implementation provides enhanced structured message decoding for the Azure WebPubSub client with robust validation, comprehensive error handling, and seamless integration into client download methods.

## Key Features

### 1. Enhanced MessageDecoder

The `MessageDecoder` class has been significantly enhanced with:

- **Robust Input Validation**: Null and empty string checks with meaningful error messages
- **Comprehensive Error Handling**: Detailed error messages and proper exception types
- **Data Type Conversion**: Static utility method for robust data type conversion
- **Base64 Validation**: Proper validation before decoding binary/protobuf data
- **JSON Validation**: Format validation for JSON data types
- **Enhanced Logging**: Detailed logging with ClientLogger for debugging

### 2. Message Validation

The new `MessageValidator` utility class provides:

- **Field Validation**: Validates required fields for different message types
- **Data Consistency**: Ensures data type and content consistency
- **JSON Format Validation**: Validates JSON structure for JSON data types
- **Detailed Error Messages**: Clear error messages for validation failures

### 3. Integration with Client Download Methods

Enhanced integration into the WebSocket message handling:

- **WebSocketClientHandler**: Enhanced with robust decoder error handling
- **Graceful Error Handling**: Malformed messages are logged and skipped gracefully
- **Null Safety**: Proper null checks for decoded messages
- **Backwards Compatibility**: All existing functionality preserved

## Data Type Handling

The enhanced decoder supports all WebPubSub data formats:

### TEXT
- Direct string conversion from JSON
- No additional validation required
- Maintains original string content

### BINARY and PROTOBUF
- Base64 format validation before decoding
- Proper error handling for invalid Base64
- Decoded to byte array via BinaryData

### JSON
- JSON format validation
- Support for objects, arrays, primitives, and null
- Proper error handling for malformed JSON

## Error Handling

Comprehensive error handling for all failure scenarios:

### Input Validation Errors
- `NullPointerException` for null input
- `IllegalArgumentException` for empty/whitespace input

### JSON Parsing Errors
- `UncheckedIOException` wrapped as `IllegalArgumentException`
- Detailed error messages with context

### Data Format Errors
- Invalid Base64 format detection
- Invalid JSON structure detection
- Unsupported data type handling

## Testing

Comprehensive test coverage with two test suites:

### DecoderTests
- Extended existing tests with 20+ additional test cases
- Error handling scenarios
- Data type conversion edge cases
- Boundary condition testing

### MessageValidatorTests
- New comprehensive validation testing
- Message field validation
- Data type consistency testing
- JSON format validation

## Usage Examples

### Basic Decoding
```java
MessageDecoder decoder = new MessageDecoder();
WebPubSubMessage message = decoder.decode(jsonString);
```

### Data Type Conversion
```java
BinaryData textData = MessageDecoder.convertDataForType("text", WebPubSubDataFormat.TEXT);
BinaryData binaryData = MessageDecoder.convertDataForType("SGVsbG8=", WebPubSubDataFormat.BINARY);
```

### Message Validation
```java
MessageValidator.validateMessage(decodedMessage);
```

## Performance Considerations

- **Minimal Overhead**: Enhanced functionality adds minimal performance overhead
- **Lazy Validation**: Validation only occurs when messages are processed
- **Efficient Base64 Handling**: Validation reuses decoding for efficiency
- **Memory Efficient**: No additional memory allocation for successful cases

## Backwards Compatibility

✅ **Full backwards compatibility maintained**:
- All existing public APIs unchanged
- Existing tests continue to pass
- No breaking changes to message formats
- Enhanced functionality is purely additive

## Integration Points

The enhanced decoder is integrated at key points in the client:

1. **WebSocket Message Reception**: Direct integration in `WebSocketClientHandler.publishBuffer()`
2. **Message Model Decoding**: Used in `GroupDataMessage.fromJson()` and `ServerDataMessage.fromJson()`
3. **Error Handling**: Graceful error handling with logging throughout the message processing pipeline

## Benefits

1. **Improved Reliability**: Robust error handling prevents client crashes from malformed messages
2. **Better Debugging**: Enhanced logging provides clear insight into decoding failures
3. **Data Integrity**: Validation ensures message consistency and proper data formats
4. **Maintainability**: Clear separation of concerns with dedicated validation utilities
5. **Future-Proof**: Extensible design allows for easy addition of new data types or validation rules

This implementation successfully provides the enhanced structured message decoding capabilities required while maintaining full backwards compatibility and following Azure SDK for Java best practices.