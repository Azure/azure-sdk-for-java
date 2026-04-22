// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for StructuredMessageDecoder with focus on the validated-emission guarantee:
 * payload bytes for a segment are only returned after the segment's CRC has been verified.
 */
public class StructuredMessageDecoderTests {

    private static ByteBuffer collectFlux(Flux<ByteBuffer> flux) {
        return ByteBuffer.wrap(FluxUtil.collectBytesInByteBufferStream(flux).block()).order(ByteOrder.LITTLE_ENDIAN);
    }

    @Test
    public void readsCompleteMessageInSingleChunk() throws IOException {
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer result = decoder.decodeChunk(encodedData);

        assertTrue(decoder.isComplete());
        assertNotNull(result);
        byte[] decodedData = new byte[result.remaining()];
        result.get(decodedData);
        assertArrayEquals(originalData, decodedData);
    }

    @Test
    public void readsMessageSplitHeaderAcrossChunks() throws IOException {
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 128, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        // Split at byte 7 (mid-header, header is 13 bytes)
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, 7);
        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, 7, encodedLength - 7);
        chunk1.order(ByteOrder.LITTLE_ENDIAN);
        chunk2.order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        ByteBuffer result1 = decoder.decodeChunk(chunk1);
        assertNull(result1);
        assertFalse(decoder.isComplete());

        ByteBuffer result2 = decoder.decodeChunk(chunk2);
        assertNotNull(result2);
        assertTrue(decoder.isComplete());
    }

    @Test
    public void readsSegmentHeaderSplitAcrossChunks() throws IOException {
        byte[] originalData = new byte[512];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 256, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        // Split after message header (13 bytes) + 5 bytes into first segment header.
        int splitPoint = 18;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, splitPoint);
        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, splitPoint, encodedLength - splitPoint);
        chunk1.order(ByteOrder.LITTLE_ENDIAN);
        chunk2.order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        ByteBuffer result1 = decoder.decodeChunk(chunk1);
        // Only the message header is consumed; segment header is incomplete so nothing validated yet.
        assertNull(result1);
        assertFalse(decoder.isComplete());

        ByteBuffer result2 = decoder.decodeChunk(chunk2);
        assertNotNull(result2);
        assertTrue(decoder.isComplete());
    }

    @Test
    public void handlesZeroLengthSegment() throws IOException {
        byte[] minimalData = new byte[1];
        ThreadLocalRandom.current().nextBytes(minimalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(minimalData.length, 1024, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(minimalData)));
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer result = decoder.decodeChunk(encodedData);

        assertTrue(decoder.isComplete());
        assertNotNull(result);
        assertEquals(1, result.remaining());
    }

    @Test
    public void multipleChunksDecode() throws IOException {
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 128, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        int chunkSize = 32;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int offset = 0; offset < encodedLength; offset += chunkSize) {
            int len = Math.min(chunkSize, encodedLength - offset);
            ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, offset, len);
            chunk.order(ByteOrder.LITTLE_ENDIAN);

            ByteBuffer result = decoder.decodeChunk(chunk);
            if (result != null && result.hasRemaining()) {
                byte[] decoded = new byte[result.remaining()];
                result.get(decoded);
                output.write(decoded, 0, decoded.length);
            }
            if (decoder.isComplete()) {
                break;
            }
        }

        assertTrue(decoder.isComplete());
        assertArrayEquals(originalData, output.toByteArray());
    }

    @Test
    public void decodeWithNoCrc() throws IOException {
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 128, StructuredMessageFlags.NONE);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer result = decoder.decodeChunk(encodedData);

        assertTrue(decoder.isComplete());
        assertNotNull(result);
        byte[] decodedData = new byte[result.remaining()];
        result.get(decodedData);
        assertArrayEquals(originalData, decodedData);
    }

    @Test
    public void handlesZeroLengthBuffer() throws IOException {
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 128, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        ByteBuffer result1 = decoder.decodeChunk(emptyBuffer);
        assertNull(result1);

        ByteBuffer dataBuffer = ByteBuffer.wrap(encodedBytes);
        dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer result2 = decoder.decodeChunk(dataBuffer);
        assertNotNull(result2);
        assertTrue(decoder.isComplete());
    }

    /**
     * Verifies Kyle's emission guarantee (r3120267493): payload bytes for a segment are
     * not emitted until the segment's CRC footer is read and validated. When the decoder
     * has received the full segment payload but the CRC footer is still incomplete,
     * {@code decodeChunk} must return {@code null}, never the in-progress payload bytes.
     */
    @Test
    public void withholdsPayloadUntilSegmentFooterValidated() throws IOException {
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);

        StructuredMessageEncoder encoder
            = new StructuredMessageEncoder(originalData.length, 1024, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        int encodedLength = encodedData.remaining();
        byte[] encodedBytes = new byte[encodedLength];
        encodedData.get(encodedBytes);

        // Layout: msgHeader(13) + segHeader(10) + payload(1024) + segCrc(8) + msgCrc(8) = 1063.
        // Feed the full payload but stop 1 byte short of completing the SEGMENT CRC footer.
        int segCrcAllButLast = 13 + 10 + 1024 + 7;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, segCrcAllButLast);
        chunk1.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer partial = decoder.decodeChunk(chunk1);
        assertNull(partial, "Decoder must not emit payload before segment CRC is validated");
        assertFalse(decoder.isComplete());

        // Feed the remainder; segment CRC completes, payload is released, and message CRC completes.
        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, segCrcAllButLast, encodedLength - segCrcAllButLast);
        chunk2.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer emitted = decoder.decodeChunk(chunk2);
        assertNotNull(emitted);
        assertTrue(decoder.isComplete());

        byte[] decodedData = new byte[emitted.remaining()];
        emitted.get(decodedData);
        assertArrayEquals(originalData, decodedData);
    }

}
