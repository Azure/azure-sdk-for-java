// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.storage.common.implementation.StorageCrc64Calculator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A simple demonstration of the StructuredMessageDecoder in action.
 * This class can be run to see the decoder working with real data.
 */
public class DecoderDemo {

    /**
     * Test that runs the main demo functionality.
     */
    @Test
    public void runDemo() {
        try {
            System.out.println("=== Structured Message Decoder Demonstration ===\n");

            // Demo 1: Basic decoding
            demonstrateBasicDecoding();

            // Demo 2: CRC64 validation
            demonstrateCrc64Validation();

            // Demo 3: Multi-segment message
            demonstrateMultiSegmentMessage();

            // Demo 4: Error detection
            demonstrateErrorDetection();

            System.out.println("All demonstrations completed successfully!");

        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void demonstrateBasicDecoding() throws IOException {
        System.out.println("1. Basic Message Decoding:");
        System.out.println("-------------------------");

        String originalText = "Hello, Azure Storage!";
        byte[] originalData = originalText.getBytes();

        System.out.println("Original data: \"" + originalText + "\"");
        System.out.println("Data length: " + originalData.length + " bytes");

        // Create structured message without CRC
        ByteBuffer message = createSimpleMessage(originalData, StructuredMessageFlags.NONE);
        System.out.println("Structured message size: " + message.capacity() + " bytes");

        // Decode the message
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());
        ByteBuffer decoded = decoder.decode(message);
        decoder.finalizeDecoding();

        // Extract result
        byte[] decodedData = new byte[decoded.remaining()];
        decoded.get(decodedData);
        String decodedText = new String(decodedData);

        System.out.println("Decoded data: \"" + decodedText + "\"");
        System.out.println("Decode successful: " + originalText.equals(decodedText));
        System.out.println();
    }

    private static void demonstrateCrc64Validation() throws IOException {
        System.out.println("2. CRC64 Checksum Validation:");
        System.out.println("-----------------------------");

        String originalText = "Data integrity is important!";
        byte[] originalData = originalText.getBytes();

        System.out.println("Original data: \"" + originalText + "\"");

        // Create structured message with CRC64
        ByteBuffer message = createSimpleMessage(originalData, StructuredMessageFlags.STORAGE_CRC64);
        System.out.println("Message with CRC64 size: " + message.capacity() + " bytes");

        // Decode the message
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());
        ByteBuffer decoded = decoder.decode(message);
        decoder.finalizeDecoding();

        // Extract result
        byte[] decodedData = new byte[decoded.remaining()];
        decoded.get(decodedData);
        String decodedText = new String(decodedData);

        System.out.println("Decoded data: \"" + decodedText + "\"");
        System.out.println("CRC64 validation: PASSED");
        System.out.println("Decode successful: " + originalText.equals(decodedText));
        System.out.println();
    }

    private static void demonstrateMultiSegmentMessage() throws IOException {
        System.out.println("3. Multi-Segment Message:");
        System.out.println("-------------------------");

        // Create larger data that will be segmented
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("Segment data ").append(i).append(" ");
        }
        String originalText = sb.toString();
        byte[] originalData = originalText.getBytes();

        System.out.println("Original data length: " + originalData.length + " bytes");

        // Create multi-segment message (50-byte segments)
        ByteBuffer message = createMultiSegmentMessage(originalData, 50, StructuredMessageFlags.STORAGE_CRC64);
        System.out.println("Multi-segment message size: " + message.capacity() + " bytes");

        // Calculate expected number of segments
        int expectedSegments = (int) Math.ceil((double) originalData.length / 50);
        System.out.println("Expected segments: " + expectedSegments);

        // Decode the message
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());
        ByteBuffer decoded = decoder.decode(message);
        decoder.finalizeDecoding();

        // Extract result
        byte[] decodedData = new byte[decoded.remaining()];
        decoded.get(decodedData);
        String decodedText = new String(decodedData);

        System.out.println("Decoded data length: " + decodedData.length + " bytes");
        System.out.println("Multi-segment decode successful: " + originalText.equals(decodedText));
        System.out.println();
    }

    private static void demonstrateErrorDetection() throws IOException {
        System.out.println("4. Error Detection:");
        System.out.println("------------------");

        String originalText = "This message will be corrupted";
        byte[] originalData = originalText.getBytes();

        System.out.println("Original data: \"" + originalText + "\"");

        // Create structured message with CRC64
        ByteBuffer message = createSimpleMessage(originalData, StructuredMessageFlags.STORAGE_CRC64);

        // Corrupt the CRC64 at the end
        int crcPosition = message.capacity() - 8;
        long originalCrc = message.getLong(crcPosition);
        message.putLong(crcPosition, originalCrc + 1); // Corrupt it

        System.out.println("Message corrupted (CRC64 modified)");

        // Try to decode - should fail
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.capacity());

        try {
            decoder.decode(message);
            System.out.println("ERROR: Decoder should have detected corruption!");
        } catch (IllegalArgumentException e) {
            System.out.println("Corruption detected successfully: " + e.getMessage());
        }

        System.out.println();
    }

    // Helper method to create a simple structured message
    private static ByteBuffer createSimpleMessage(byte[] data, StructuredMessageFlags flags) throws IOException {
        boolean hasCrc = flags == StructuredMessageFlags.STORAGE_CRC64;
        int crcLength = hasCrc ? 8 : 0;

        // Calculate message length
        int messageLength = 13 + 10 + data.length + crcLength + crcLength;

        ByteArrayOutputStream message = new ByteArrayOutputStream();

        // Message header
        ByteBuffer header = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
        header.put((byte) 1);
        header.putLong(messageLength);
        header.putShort((short) flags.getValue());
        header.putShort((short) 1);
        message.write(header.array());

        // Segment header
        ByteBuffer segmentHeader = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
        segmentHeader.putShort((short) 1);
        segmentHeader.putLong(data.length);
        message.write(segmentHeader.array());

        // Segment data
        message.write(data);

        // Segment CRC64
        if (hasCrc) {
            long segmentCrc = StorageCrc64Calculator.compute(data, 0);
            ByteBuffer segmentFooter = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            segmentFooter.putLong(segmentCrc);
            message.write(segmentFooter.array());
        }

        // Message CRC64
        if (hasCrc) {
            long messageCrc = StorageCrc64Calculator.compute(data, 0);
            ByteBuffer messageFooter = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            messageFooter.putLong(messageCrc);
            message.write(messageFooter.array());
        }

        return ByteBuffer.wrap(message.toByteArray());
    }

    // Helper method to create a multi-segment message
    private static ByteBuffer createMultiSegmentMessage(byte[] data, int segmentSize, StructuredMessageFlags flags)
        throws IOException {
        boolean hasCrc = flags == StructuredMessageFlags.STORAGE_CRC64;
        int crcLength = hasCrc ? 8 : 0;

        int segmentCount = (int) Math.ceil((double) data.length / segmentSize);
        int messageLength = 13 + ((10 + crcLength) * segmentCount) + data.length + crcLength;

        ByteArrayOutputStream message = new ByteArrayOutputStream();

        // Message header
        ByteBuffer header = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
        header.put((byte) 1);
        header.putLong(messageLength);
        header.putShort((short) flags.getValue());
        header.putShort((short) segmentCount);
        message.write(header.array());

        long messageCrc = 0;

        // Write segments
        int offset = 0;
        for (int i = 1; i <= segmentCount; i++) {
            int currentSegmentSize = Math.min(segmentSize, data.length - offset);
            byte[] segmentData = new byte[currentSegmentSize];
            System.arraycopy(data, offset, segmentData, 0, currentSegmentSize);

            // Segment header
            ByteBuffer segmentHeader = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
            segmentHeader.putShort((short) i);
            segmentHeader.putLong(currentSegmentSize);
            message.write(segmentHeader.array());

            // Segment data
            message.write(segmentData);

            // Segment CRC64
            if (hasCrc) {
                long segmentCrc = StorageCrc64Calculator.compute(segmentData, 0);
                ByteBuffer segmentFooter = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                segmentFooter.putLong(segmentCrc);
                message.write(segmentFooter.array());

                messageCrc = StorageCrc64Calculator.compute(segmentData, messageCrc);
            }

            offset += currentSegmentSize;
        }

        // Message CRC64
        if (hasCrc) {
            ByteBuffer messageFooter = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            messageFooter.putLong(messageCrc);
            message.write(messageFooter.array());
        }

        return ByteBuffer.wrap(message.toByteArray());
    }
}
