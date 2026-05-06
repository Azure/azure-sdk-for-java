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
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for StructuredMessageDecoder.
 */
public class StructuredMessageDecoderTests {
    private static final int MESSAGE_HEADER_LENGTH = 13;
    private static final int SEGMENT_HEADER_LENGTH = 10;
    private static final int CRC64_LENGTH = 8;

    private static ByteBuffer collectFlux(Flux<ByteBuffer> flux) {
        return ByteBuffer.wrap(FluxUtil.collectBytesInByteBufferStream(flux).block()).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static byte[] encode(byte[] originalData, int segmentLength, StructuredMessageFlags flags)
        throws IOException {
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, segmentLength, flags);
        ByteBuffer encoded = collectFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        byte[] encodedBytes = new byte[encoded.remaining()];
        encoded.get(encodedBytes);
        return encodedBytes;
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

        // Split mid-message-header (header is 13 bytes).
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

        // Split mid-segment-header: msgHdr(13) + 5 of segHdr(10).
        int splitPoint = 18;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, splitPoint);
        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, splitPoint, encodedLength - splitPoint);
        chunk1.order(ByteOrder.LITTLE_ENDIAN);
        chunk2.order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        ByteBuffer result1 = decoder.decodeChunk(chunk1);
        // Segment header is incomplete, so nothing is emitted yet.
        assertNull(result1);
        assertFalse(decoder.isComplete());

        ByteBuffer result2 = decoder.decodeChunk(chunk2);
        assertNotNull(result2);
        assertTrue(decoder.isComplete());
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
     * Payload bytes for a segment must not be emitted until the segment's CRC footer has been read and
     * validated. While the footer is incomplete, decodeChunk must return null.
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

        // msgHdr(13) + segHdr(10) + payload(1024) + segCrc(8) + msgCrc(8) = 1063. Stop 1 byte short of segCrc.
        int segCrcAllButLast = 13 + 10 + 1024 + 7;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, segCrcAllButLast);
        chunk1.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer partial = decoder.decodeChunk(chunk1);
        assertNull(partial, "Decoder must not emit payload before segment CRC is validated");
        assertFalse(decoder.isComplete());

        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, segCrcAllButLast, encodedLength - segCrcAllButLast);
        chunk2.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer emitted = decoder.decodeChunk(chunk2);
        assertNotNull(emitted);
        assertTrue(decoder.isComplete());

        byte[] decodedData = new byte[emitted.remaining()];
        emitted.get(decodedData);
        assertArrayEquals(originalData, decodedData);
    }

    @Test
    public void throwsOnUnsupportedStructuredMessageVersion() throws IOException {
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // Corrupt message version (byte 0 of the message header).
        encodedBytes[0] = (byte) (StructuredMessageConstants.DEFAULT_MESSAGE_VERSION + 1);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("Unsupported structured message version"));
    }

    @Test
    public void throwsOnMessageLengthMismatch() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Construct decoder with wrong expected encoded length.
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length + 1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("did not match content length"));
    }

    @Test
    public void throwsOnUnexpectedSegmentNumber() throws IOException {
        byte[] data = new byte[300];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Corrupt first segment number from 1 to 2 (offset 13).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(MESSAGE_HEADER_LENGTH, (short) 2);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("Unexpected segment number"));
    }

    @Test
    public void throwsOnInvalidSegmentSize() throws IOException {
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Corrupt first segment size to an impossible value (8 bytes at offset 15).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(MESSAGE_HEADER_LENGTH + 2, Long.MAX_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("Invalid segment size detected"));
    }

    @Test
    public void throwsOnSegmentCrcMismatch() throws IOException {
        byte[] data = new byte[512];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 512, StructuredMessageFlags.STORAGE_CRC64);

        // msgHdr(13) + segHdr(10) + payload(512) + segCrc(8) + msgCrc(8). Flip one bit of the segment CRC.
        int segmentCrcOffset = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + data.length;
        encodedBytes[segmentCrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in segment"));
    }

    @Test
    public void throwsOnSegmentCrcMismatchInLaterSegment() throws IOException {
        // Multi-segment message where segment 1 is intact but segment 2's CRC is corrupted; verifies
        // CRC validation runs on every segment, not just the first.
        byte[] data = new byte[300];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 100, StructuredMessageFlags.STORAGE_CRC64);

        // Per-segment block: segHdr(10) + payload(100) + segCrc(8) = 118.
        // Segment 2's CRC starts at: msgHdr(13) + segBlock(118) + segHdr(10) + payload(100).
        int seg2CrcOffset
            = MESSAGE_HEADER_LENGTH + (SEGMENT_HEADER_LENGTH + 100 + CRC64_LENGTH) + SEGMENT_HEADER_LENGTH + 100;
        encodedBytes[seg2CrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in segment 2"));
    }

    @Test
    public void throwsOnMessageCrcMismatch() throws IOException {
        byte[] data = new byte[512];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 512, StructuredMessageFlags.STORAGE_CRC64);

        int messageCrcOffset = encodedBytes.length - CRC64_LENGTH;
        byte[] corrupted = Arrays.copyOf(encodedBytes, encodedBytes.length);
        corrupted[messageCrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(corrupted.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(corrupted).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in message footer"));
    }

    @Test
    public void throwsOnUnsupportedFlags() throws IOException {
        // Flags value 2 is not in the StructuredMessageFlags enum (NONE=0, STORAGE_CRC64=1) and must be rejected.
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // Flags live at offset 9 (2 bytes, little-endian).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(9, (short) 2);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("Invalid value for StructuredMessageFlags"));
    }

    @Test
    public void throwsOnZeroSegments() throws IOException {
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // numSegments lives at offset 11 (2 bytes, little-endian). Force it to zero.
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(11, (short) 0);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("at least one segment"));
    }

    @Test
    public void throwsOnSkippedSegmentNumber() throws IOException {
        // 3 segments of 100 bytes each. Layout per segment: segHdr(10) + payload(100) + segCrc(8) = 118.
        // Rewrite segment 2's number to 3 to simulate a stream that skips segment 2.
        byte[] data = new byte[300];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 100, StructuredMessageFlags.STORAGE_CRC64);

        int seg2NumberOffset = MESSAGE_HEADER_LENGTH + (SEGMENT_HEADER_LENGTH + 100 + CRC64_LENGTH);
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(seg2NumberOffset, (short) 3);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("Unexpected segment number"));
    }

    @Test
    public void truncatedMessageHeaderLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // Feed only the first 5 bytes of the 13-byte message header.
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, 5).order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer result = decoder.decodeChunk(chunk);
        assertNull(result);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedSegmentHeaderLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 256, StructuredMessageFlags.STORAGE_CRC64);

        // Feed full message header + 5 of the 10 segment-header bytes.
        int truncated = MESSAGE_HEADER_LENGTH + 5;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated).order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer result = decoder.decodeChunk(chunk);
        assertNull(result);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedSegmentFooterLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Layout: msgHdr(13) + segHdr(10) + payload(128) + segCrc(8) + msgCrc(8). Truncate mid-segCrc.
        int truncated = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + 128 + 4;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated).order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer result = decoder.decodeChunk(chunk);
        assertNull(result);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedMessageFooterLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Feed everything except the last 4 bytes of the 8-byte message CRC footer.
        int truncated = encodedBytes.length - 4;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated).order(ByteOrder.LITTLE_ENDIAN);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer result = decoder.decodeChunk(chunk);
        // Segment payload has been released, but the message footer is still incomplete.
        assertNotNull(result);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void extraBytesAfterMessageFooterAreNotConsumed() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Append garbage bytes after the message footer; the decoder must stop at the declared message length.
        int extras = 16;
        byte[] padded = new byte[encodedBytes.length + extras];
        System.arraycopy(encodedBytes, 0, padded, 0, encodedBytes.length);
        byte[] noise = new byte[extras];
        ThreadLocalRandom.current().nextBytes(noise);
        System.arraycopy(noise, 0, padded, encodedBytes.length, extras);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer buffer = ByteBuffer.wrap(padded).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer result = decoder.decodeChunk(buffer);

        assertTrue(decoder.isComplete());
        assertNotNull(result);
        byte[] decoded = new byte[result.remaining()];
        result.get(decoded);
        assertArrayEquals(data, decoded);
        // Trailing bytes must not be consumed; buffer position stops at the declared message length.
        assertEquals(extras, buffer.remaining());
    }

    @Test
    public void throwsOnEncodedPayloadLargerThanExpectedSize() throws IOException {
        // Wire payload is larger than the expectedEncodedMessageLength supplied to the decoder. Must be rejected by the msgLen check.
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length - 8);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("did not match content length"));
    }

    @Test
    public void throwsOnNegativeMessageLength() throws IOException {
        // msgLen lives at offset 1 (8 bytes, little-endian). A negative value must be rejected before
        // any further bounds math runs.
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(1, Long.MIN_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("Message length too small"));
    }

    @Test
    public void throwsOnNegativeSegmentSize() throws IOException {
        // Companion to throwsOnInvalidSegmentSize covering the negative-value branch of the segment-size check.
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 256, StructuredMessageFlags.STORAGE_CRC64);

        // Segment size lives at offset MESSAGE_HEADER_LENGTH + 2 (after the 2-byte segment number).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(MESSAGE_HEADER_LENGTH + 2, Long.MIN_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("Invalid segment size detected"));
    }

    @Test
    public void throwsOnInjectedRandomByte() throws IOException {
        // Insert a single random byte at a random offset in the encoded wire bytes. The msgLen field still
        // declares the original size, so any insertion must be rejected by validation.
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        int insertAt = ThreadLocalRandom.current().nextInt(encodedBytes.length + 1);
        byte injected = (byte) ThreadLocalRandom.current().nextInt(256);

        byte[] tampered = new byte[encodedBytes.length + 1];
        System.arraycopy(encodedBytes, 0, tampered, 0, insertAt);
        tampered[insertAt] = injected;
        System.arraycopy(encodedBytes, insertAt, tampered, insertAt + 1, encodedBytes.length - insertAt);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(tampered.length);
        assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(tampered).order(ByteOrder.LITTLE_ENDIAN)));
    }

    @Test
    public void throwsOnRemovedRandomBytes() throws IOException {
        // Remove a random run of bytes from a random offset in the encoded wire. The msgLen field still
        // declares the original size, so any deletion must be rejected by validation.
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encode(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        int removeCount = 1 + ThreadLocalRandom.current().nextInt(8);
        int removeAt = ThreadLocalRandom.current().nextInt(encodedBytes.length - removeCount);

        byte[] tampered = new byte[encodedBytes.length - removeCount];
        System.arraycopy(encodedBytes, 0, tampered, 0, removeAt);
        System.arraycopy(encodedBytes, removeAt + removeCount, tampered, removeAt,
            encodedBytes.length - removeAt - removeCount);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(tampered.length);
        assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(tampered).order(ByteOrder.LITTLE_ENDIAN)));
    }

}
