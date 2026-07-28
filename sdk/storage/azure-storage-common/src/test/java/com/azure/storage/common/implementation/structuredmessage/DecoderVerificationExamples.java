// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.storage.common.implementation.StorageCrc64Calculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Example tests demonstrating how to verify the StructuredMessageDecoder works correctly.
 */
public class DecoderVerificationExamples {

    /**
     * Example: Basic decoder functionality test.
     * This test demonstrates how to verify that the decoder can decode simple messages.
     */
    @Test
    public void basicDecoderTest() throws IOException {
        // Test data
        String testData = "Hello, World!";
        byte[] data = testData.getBytes();

        // Build a structured message manually
        ByteBuffer message = buildSimpleStructuredMessage(data, StructuredMessageFlags.NONE);

        // Decode the message
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());
        ByteBuffer decodedBuffer = decoder.decode(message);

        // Verify the result
        byte[] decodedData = new byte[decodedBuffer.remaining()];
        decodedBuffer.get(decodedData);

        Assertions.assertArrayEquals(data, decodedData);
        String decodedString = new String(decodedData);
        Assertions.assertEquals(testData, decodedString);

        // Finalize to ensure complete processing
        decoder.finalizeDecoding();
    }

    /**
     * Example: Decoder with CRC64 checksum verification.
     * This test demonstrates how to verify that the decoder correctly validates CRC64 checksums.
     */
    @Test
    public void crc64ValidationTest() throws IOException {
        // Test data
        byte[] data = "This is test data for CRC64 validation.".getBytes();

        // Build a structured message with CRC64
        ByteBuffer message = buildSimpleStructuredMessage(data, StructuredMessageFlags.STORAGE_CRC64);

        // Decode the message
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());
        ByteBuffer decodedBuffer = decoder.decode(message);

        // Verify the result
        byte[] decodedData = new byte[decodedBuffer.remaining()];
        decodedBuffer.get(decodedData);

        Assertions.assertArrayEquals(data, decodedData);

        // Finalize to ensure complete processing including CRC validation
        decoder.finalizeDecoding();
    }

    /**
     * Example: Verify that CRC64 mismatch is detected.
     * This test demonstrates how to verify that the decoder correctly detects corrupted data.
     */
    @Test
    public void corruptedDataDetectionTest() throws IOException {
        // Test data
        byte[] data = "Test data that will be corrupted.".getBytes();

        // Build a structured message with CRC64
        ByteBuffer message = buildSimpleStructuredMessage(data, StructuredMessageFlags.STORAGE_CRC64);

        // Corrupt the CRC64 value at the end of the message
        int crcPosition = message.capacity() - 8; // Last 8 bytes are the CRC64
        message.putLong(crcPosition, 0xDEADBEEFCAFEBABEL); // Invalid CRC

        // Try to decode - should throw exception
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            decoder.decode(message);
        });
    }

    /**
     * Example: Partial decoding test.
     * This test demonstrates how to verify partial decoding functionality.
     */
    @Test
    public void partialDecodingTest() throws IOException {
        // Test data
        byte[] data = "This is a longer test string for partial decoding verification.".getBytes();

        // Build a structured message
        ByteBuffer message = buildSimpleStructuredMessage(data, StructuredMessageFlags.STORAGE_CRC64);

        // Decode in chunks
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        // Read in 10-byte chunks
        while (result.size() < data.length) {
            ByteBuffer chunk = decoder.decode(message, 10);
            byte[] chunkData = new byte[chunk.remaining()];
            chunk.get(chunkData);
            result.write(chunkData);
        }

        // Verify the result
        Assertions.assertArrayEquals(data, result.toByteArray());

        // Finalize decoding
        decoder.finalizeDecoding();
    }

    /**
     * Helper method to build a simple structured message for testing.
     */
    private ByteBuffer buildSimpleStructuredMessage(byte[] data, StructuredMessageFlags flags) throws IOException {
        boolean hasCrc = flags == StructuredMessageFlags.STORAGE_CRC64;
        int crcLength = hasCrc ? 8 : 0;

        // Calculate message length: header(13) + segment_header(10) + data + segment_footer(0-8) + message_footer(0-8)
        int messageLength = 13 + 10 + data.length + crcLength + crcLength;

        ByteArrayOutputStream message = new ByteArrayOutputStream();

        // Message header: version(1) + length(8) + flags(2) + segments(2)
        ByteBuffer header = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
        header.put((byte) 1); // version
        header.putLong(messageLength); // message length
        header.putShort((short) flags.getValue()); // flags
        header.putShort((short) 1); // segment count
        message.write(header.array());

        // Segment header: number(2) + size(8)
        ByteBuffer segmentHeader = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
        segmentHeader.putShort((short) 1); // segment number
        segmentHeader.putLong(data.length); // segment size
        message.write(segmentHeader.array());

        // Segment data
        message.write(data);

        // Segment CRC64 (if enabled)
        if (hasCrc) {
            long segmentCrc = StorageCrc64Calculator.compute(data, 0);
            ByteBuffer segmentFooter = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            segmentFooter.putLong(segmentCrc);
            message.write(segmentFooter.array());
        }

        // Message CRC64 (if enabled)
        if (hasCrc) {
            long messageCrc = StorageCrc64Calculator.compute(data, 0);
            ByteBuffer messageFooter = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            messageFooter.putLong(messageCrc);
            message.write(messageFooter.array());
        }

        return ByteBuffer.wrap(message.toByteArray());
    }
}
