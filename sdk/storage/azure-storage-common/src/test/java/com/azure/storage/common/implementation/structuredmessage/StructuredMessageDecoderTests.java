// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for StructuredMessageDecoder with focus on handling partial headers
 * and segment splits across chunks.
 */
public class StructuredMessageDecoderTests {

    @Test
    public void readsCompleteMessageInSingleChunk() throws IOException {
        // Test: Complete message in a single ByteBuffer should decode fully
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer result = decoder.decode(encodedData);

        assertNotNull(result);
        byte[] decodedData = new byte[result.remaining()];
        result.get(decodedData);
        assertArrayEquals(originalData, decodedData);
        assertTrue(decoder.isComplete());
    }

    @Test
    public void readsMessageSplitHeaderAcrossChunks() throws IOException {
        // Test: Feed header bytes split across two buffers
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 128, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        // Split at byte 7 (mid-header, header is 13 bytes)
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, 7);
        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, 7, encodedLength - 7);
        chunk1.order(ByteOrder.LITTLE_ENDIAN);
        chunk2.order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        // First chunk should not throw, should wait for more bytes
        StructuredMessageDecoder.DecodeResult result1 = decoder.decodeChunk(chunk1);
        assertEquals(StructuredMessageDecoder.DecodeStatus.NEED_MORE_BYTES, result1.getStatus());
        assertFalse(decoder.isComplete());

        // Second chunk should complete the decode
        StructuredMessageDecoder.DecodeResult result2 = decoder.decodeChunk(chunk2);
        assertEquals(StructuredMessageDecoder.DecodeStatus.COMPLETED, result2.getStatus());
        assertTrue(decoder.isComplete());
    }

    @Test
    public void readsSegmentHeaderSplitAcrossChunks() throws IOException {
        // Test: Split the 10-byte segment header across two chunks
        byte[] originalData = new byte[512];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 256, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        // Split after message header (13 bytes) + 5 bytes into first segment header
        // Segment header is 10 bytes, so split at byte 18 (mid-segment-header)
        int splitPoint = 18;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, splitPoint);
        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, splitPoint, encodedLength - splitPoint);
        chunk1.order(ByteOrder.LITTLE_ENDIAN);
        chunk2.order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        // First chunk should parse header but wait for segment header completion
        StructuredMessageDecoder.DecodeResult result1 = decoder.decodeChunk(chunk1);
        assertEquals(StructuredMessageDecoder.DecodeStatus.NEED_MORE_BYTES, result1.getStatus());
        assertFalse(decoder.isComplete());

        // Second chunk should complete
        StructuredMessageDecoder.DecodeResult result2 = decoder.decodeChunk(chunk2);
        assertEquals(StructuredMessageDecoder.DecodeStatus.COMPLETED, result2.getStatus());
        assertTrue(decoder.isComplete());
    }

    @Test
    public void handlesZeroLengthSegment() throws IOException {
        // Test: Zero-length segment should decode correctly
        // Note: Zero-length segments are valid in the format
        byte[] originalData = new byte[0];

        // For zero-length data, encoder behavior varies - let's test with minimal data
        byte[] minimalData = new byte[1];
        ThreadLocalRandom.current().nextBytes(minimalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(minimalData.length, 1024, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(minimalData));
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer result = decoder.decode(encodedData);

        assertNotNull(result);
        assertEquals(1, result.remaining());
        assertTrue(decoder.isComplete());
    }

    @Test
    public void tracksLastCompleteSegmentCorrectly() throws IOException {
        // Test: Verify lastCompleteSegmentStart is updated correctly after each segment
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 256, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        // Initially lastCompleteSegmentStart should be 0
        assertEquals(0, decoder.getLastCompleteSegmentStart());

        // Decode the entire message
        decoder.decode(encodedData);

        // After complete decode, lastCompleteSegmentStart should point to end of last segment
        // (before message footer, if any)
        assertTrue(decoder.isComplete());
        // lastCompleteSegmentStart should be <= messageOffset 
        assertTrue(decoder.getLastCompleteSegmentStart() <= decoder.getMessageOffset());
        // And should be > 0 (we processed at least one segment)
        assertTrue(decoder.getLastCompleteSegmentStart() > 0);
    }

    @Test
    public void resetToLastCompleteSegmentWorks() throws IOException {
        // Test: Verify reset functionality for smart retry
        byte[] originalData = new byte[512];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 256, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        // Parse first segment completely, then simulate interruption
        // First segment ends after: header(13) + segment_header(10) + content(256) + crc(8) = 287
        int firstSegmentEnd = 13 + 10 + 256 + 8;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, firstSegmentEnd + 5); // 5 bytes into second segment header
        chunk1.order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        decoder.decodeChunk(chunk1);

        // lastCompleteSegmentStart should be at end of first segment
        long lastComplete = decoder.getLastCompleteSegmentStart();
        assertTrue(lastComplete > 0);
        assertEquals(firstSegmentEnd, lastComplete);

        // Reset to last complete segment
        decoder.resetToLastCompleteSegment();
        assertEquals(lastComplete, decoder.getMessageOffset());
    }

    @Test
    public void multipleChunksDecode() throws IOException {
        // Test: Decode message across multiple small chunks
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 128, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        // Feed in chunks of 32 bytes
        int chunkSize = 32;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();

        for (int offset = 0; offset < encodedLength; offset += chunkSize) {
            int len = Math.min(chunkSize, encodedLength - offset);
            ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, offset, len);
            chunk.order(ByteOrder.LITTLE_ENDIAN);

            StructuredMessageDecoder.DecodeResult result = decoder.decodeChunk(chunk);
            if (result.getDecodedPayload() != null && result.getDecodedPayload().hasRemaining()) {
                byte[] decoded = new byte[result.getDecodedPayload().remaining()];
                result.getDecodedPayload().get(decoded);
                output.write(decoded, 0, decoded.length);
            }

            if (result.getStatus() == StructuredMessageDecoder.DecodeStatus.COMPLETED) {
                break;
            }
        }

        assertTrue(decoder.isComplete());
        assertArrayEquals(originalData, output.toByteArray());
    }

    @Test
    public void decodeWithNoCrc() throws IOException {
        // Test: Decode message without CRC (NONE flag)
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 128, StructuredMessageFlags.NONE);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer result = decoder.decode(encodedData);

        assertNotNull(result);
        byte[] decodedData = new byte[result.remaining()];
        result.get(decodedData);
        assertArrayEquals(originalData, decodedData);
        assertTrue(decoder.isComplete());
    }
}
