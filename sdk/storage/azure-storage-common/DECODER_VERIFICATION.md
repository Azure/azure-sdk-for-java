# Structured Message Decoder Verification Guide

## Overview

The Structured Message Decoder is a component that decodes structured messages with support for segmentation and CRC64 checksums. This guide explains how to verify that the decoder works correctly.

## What the Decoder Does

The `StructuredMessageDecoder` provides the following functionality:

1. **Message Format Validation**: Validates structured message format including headers and segments
2. **CRC64 Checksum Verification**: Validates data integrity using CRC64 checksums (when enabled)
3. **Segmented Decoding**: Handles messages split into multiple segments
4. **Partial Decoding**: Supports reading messages in chunks
5. **Error Detection**: Detects and reports various format and integrity errors

## Message Format

A structured message consists of:

```
[Message Header] [Segment 1] [Segment 2] ... [Segment N] [Message Footer]
```

### Message Header (13 bytes)
- Version (1 byte): Message format version (currently 1)
- Length (8 bytes): Total message length in little-endian format
- Flags (2 bytes): Message flags (0=none, 1=CRC64 enabled)
- Segment Count (2 bytes): Number of segments

### Segment Format
Each segment contains:
- **Segment Header (10 bytes)**:
  - Segment Number (2 bytes): Sequential segment identifier
  - Segment Size (8 bytes): Size of segment data
- **Segment Data**: The actual data content
- **Segment Footer (0-8 bytes)**: CRC64 checksum if enabled

### Message Footer (0-8 bytes)
- CRC64 checksum of all segment data (if CRC64 flag is enabled)

## How to Verify the Decoder Works

### 1. Run Unit Tests

Execute the comprehensive test suite to verify all functionality:

```bash
cd sdk/storage/azure-storage-common
mvn test -Dtest=MessageDecoderTests
```

This runs tests covering:
- Basic decoding functionality
- CRC64 validation
- Error detection
- Edge cases
- Various message sizes and formats

### 2. Run Verification Examples

Execute verification examples that demonstrate key functionality:

```bash
mvn test -Dtest=DecoderVerificationExamples
```

These examples show:
- Basic message decoding
- CRC64 checksum validation
- Corruption detection
- Partial decoding

### 3. Manual Verification Steps

#### Basic Functionality Test
```java
// Create test data
byte[] testData = "Hello, World!".getBytes();

// Create decoder
StructuredMessageDecoder decoder = new StructuredMessageDecoder(messageLength);

// Decode message
ByteBuffer result = decoder.decode(messageBuffer);

// Verify result matches original data
byte[] decodedData = new byte[result.remaining()];
result.get(decodedData);
assert Arrays.equals(testData, decodedData);

// Finalize to ensure complete processing
decoder.finalizeDecoding();
```

#### CRC64 Validation Test
```java
// Decode message with CRC64 enabled
StructuredMessageDecoder decoder = new StructuredMessageDecoder(messageLength);
ByteBuffer result = decoder.decode(messageBufferWithCrc64);

// If CRC64 is valid, decoding succeeds
// If CRC64 is invalid, IllegalArgumentException is thrown
```

#### Error Detection Test
```java
// Try to decode corrupted message
StructuredMessageDecoder decoder = new StructuredMessageDecoder(messageLength);

try {
    decoder.decode(corruptedMessage);
    // Should not reach here if corruption is detected
} catch (IllegalArgumentException e) {
    // Expected: decoder detected corruption
}
```

## Test Scenarios Covered

### Success Cases
1. **Simple Messages**: Single segment, no CRC
2. **CRC64 Messages**: Single segment with CRC64 validation
3. **Multi-Segment Messages**: Multiple segments with various sizes
4. **Large Messages**: Messages up to 50MB+ with different segment sizes
5. **Partial Decoding**: Reading messages in chunks
6. **Empty Segments**: Zero-length segments

### Error Cases
1. **Invalid Version**: Unsupported message version
2. **Incorrect Length**: Message length mismatch
3. **CRC64 Mismatch**: Segment or message CRC64 validation failure
4. **Incomplete Headers**: Truncated message or segment headers
5. **Invalid Segment Numbers**: Out-of-order or missing segments
6. **Size Mismatches**: Segment size doesn't match actual data

## Performance Verification

The decoder is tested with various message sizes:
- Small messages (10 bytes)
- Medium messages (1-10 KB)
- Large messages (1-50 MB)
- Various segment sizes (1 byte to full message)

Performance should be consistent regardless of message size or segmentation.

## Integration with Client Download Methods

The decoder integrates with Azure Storage client download methods by:

1. **Streaming Support**: Can decode messages as they are downloaded
2. **Partial Reading**: Supports reading partial content efficiently
3. **Error Handling**: Provides clear error messages for debugging
4. **Memory Efficiency**: Processes data without loading entire message in memory

## Troubleshooting

### Common Issues
1. **IllegalArgumentException**: Usually indicates corrupted data or format mismatch
2. **Buffer underflow**: Message is shorter than expected
3. **CRC mismatch**: Data corruption during transmission

### Debug Steps
1. Check message version (first byte should be 0x01)
2. Verify message length matches actual buffer size
3. Check CRC64 flag setting
4. Validate segment count and sizes

## Security Considerations

The decoder includes several security measures:
- Input validation to prevent buffer overflows
- CRC64 verification to detect tampering
- Bounds checking on all read operations
- Safe handling of malformed messages

This ensures the decoder can safely process untrusted input without security vulnerabilities.