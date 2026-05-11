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
import java.util.Objects;
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
    private static final int MESSAGE_HEADER_LENGTH = StructuredMessageConstants.V1_HEADER_LENGTH;
    private static final int SEGMENT_HEADER_LENGTH = StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH;
    private static final int CRC64_LENGTH = 8;

    @Test
    public void readsCompleteMessageInSingleChunk() throws IOException {
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);

        ByteBuffer encodedData = encodeToByteBuffer(originalData, 512, StructuredMessageFlags.STORAGE_CRC64);
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer decodedPayload = decoder.decodeChunk(encodedData);

        assertTrue(decoder.isComplete());
        assertNotNull(decodedPayload);
        byte[] decodedData = new byte[decodedPayload.remaining()];
        decodedPayload.get(decodedData);
        assertArrayEquals(originalData, decodedData);
    }

    @Test
    public void readsTopLevelMessageHeaderSplitAcrossChunks() throws IOException {
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        byte[] encodedBytes = encodeToBytes(originalData, 128, StructuredMessageFlags.STORAGE_CRC64);
        int encodedLength = encodedBytes.length;

        // Split before the 13-byte message header is complete.
        int messageHeaderSplitPoint = 7;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, messageHeaderSplitPoint);
        ByteBuffer chunk2
            = ByteBuffer.wrap(encodedBytes, messageHeaderSplitPoint, encodedLength - messageHeaderSplitPoint);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer firstDecodedPayload = decoder.decodeChunk(chunk1);
        assertNull(firstDecodedPayload);
        assertFalse(decoder.isComplete());

        ByteBuffer secondDecodedPayload = decoder.decodeChunk(chunk2);
        assertNotNull(secondDecodedPayload);
        assertTrue(decoder.isComplete());
    }

    @Test
    public void readsPerSegmentHeaderSplitAcrossChunks() throws IOException {
        byte[] originalData = new byte[512];
        ThreadLocalRandom.current().nextBytes(originalData);

        byte[] encodedBytes = encodeToBytes(originalData, 256, StructuredMessageFlags.STORAGE_CRC64);
        int encodedLength = encodedBytes.length;

        // Split after the full message header but before the 10-byte segment header is complete.
        int segmentHeaderSplitPoint = MESSAGE_HEADER_LENGTH + 5;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, segmentHeaderSplitPoint);
        ByteBuffer chunk2
            = ByteBuffer.wrap(encodedBytes, segmentHeaderSplitPoint, encodedLength - segmentHeaderSplitPoint);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer firstDecodedPayload = decoder.decodeChunk(chunk1);
        // Segment header is incomplete, so nothing is emitted yet.
        assertNull(firstDecodedPayload);
        assertFalse(decoder.isComplete());

        ByteBuffer secondDecodedPayload = decoder.decodeChunk(chunk2);
        assertNotNull(secondDecodedPayload);
        assertTrue(decoder.isComplete());
    }

    @Test
    public void multipleChunksDecode() throws IOException {
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        byte[] encodedBytes = encodeToBytes(originalData, 128, StructuredMessageFlags.STORAGE_CRC64);
        int encodedLength = encodedBytes.length;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        int chunkSize = 32;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int offset = 0; offset < encodedLength; offset += chunkSize) {
            int len = Math.min(chunkSize, encodedLength - offset);
            ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, offset, len);

            ByteBuffer decodedPayload = decoder.decodeChunk(chunk);
            if (decodedPayload != null && decodedPayload.hasRemaining()) {
                byte[] decodedBytes = new byte[decodedPayload.remaining()];
                decodedPayload.get(decodedBytes);
                output.write(decodedBytes, 0, decodedBytes.length);
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

        ByteBuffer encodedData = encodeToByteBuffer(originalData, 128, StructuredMessageFlags.NONE);
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer decodedPayload = decoder.decodeChunk(encodedData);

        assertTrue(decoder.isComplete());
        assertNotNull(decodedPayload);
        byte[] decodedData = new byte[decodedPayload.remaining()];
        decodedPayload.get(decodedData);
        assertArrayEquals(originalData, decodedData);
    }

    @Test
    public void handlesZeroLengthBuffer() throws IOException {
        byte[] originalData = new byte[256];
        ThreadLocalRandom.current().nextBytes(originalData);

        byte[] encodedBytes = encodeToBytes(originalData, 128, StructuredMessageFlags.STORAGE_CRC64);
        int encodedLength = encodedBytes.length;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        ByteBuffer firstDecodedPayload = decoder.decodeChunk(emptyBuffer);
        assertNull(firstDecodedPayload);
        ByteBuffer dataBuffer = ByteBuffer.wrap(encodedBytes);
        ByteBuffer decodedPayload = decoder.decodeChunk(dataBuffer);
        assertNotNull(decodedPayload);
        assertTrue(decoder.isComplete());
    }

    /**
     * Payload bytes for a segment must not be emitted until the segment's CRC footer has been read and
     * validated. While the footer is incomplete, decodeChunk must return null.
     */
    @Test
    public void withholdsPayloadUntilSegmentFooterValidated() throws IOException {
        int payloadSize = 1024;
        byte[] originalData = new byte[payloadSize];
        ThreadLocalRandom.current().nextBytes(originalData);

        byte[] encodedBytes = encodeToBytes(originalData, 1024, StructuredMessageFlags.STORAGE_CRC64);
        int encodedLength = encodedBytes.length;

        // The segment payload cannot be emitted until the final segment CRC byte arrives in chunk2.
        int segCrcAllButLast
            = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + payloadSize + StructuredMessageConstants.CRC64_LENGTH - 1;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, segCrcAllButLast);
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer firstDecodedPayload = decoder.decodeChunk(chunk1);
        assertNull(firstDecodedPayload, "Decoder must not emit payload before segment CRC is validated");
        assertFalse(decoder.isComplete());

        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, segCrcAllButLast, encodedLength - segCrcAllButLast);
        ByteBuffer emittedPayload = decoder.decodeChunk(chunk2);
        assertNotNull(emittedPayload);
        assertTrue(decoder.isComplete());

        byte[] decodedData = new byte[emittedPayload.remaining()];
        emittedPayload.get(decodedData);
        assertArrayEquals(originalData, decodedData);
    }

    @Test
    public void throwsOnUnsupportedStructuredMessageVersion() throws IOException {
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // Byte 0 of the message header is reserved for the version. Changing to an incompatible version (2)
        encodedBytes[0] = (byte) (StructuredMessageConstants.DEFAULT_MESSAGE_VERSION + 1);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Unsupported structured message version"));
    }

    @Test
    public void throwsOnMessageLengthMismatch() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Construct decoder with wrong expected encoded length.
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length + 1);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("did not match content length"));
    }

    @Test
    public void throwsOnUnexpectedSegmentNumber() throws IOException {
        byte[] data = new byte[300];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Corrupt first segment number from 1 to 2 (offset 13).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(MESSAGE_HEADER_LENGTH, (short) 2);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Unexpected segment number"));
    }

    @Test
    public void throwsOnInvalidSegmentSize() throws IOException {
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Corrupt first segment size to an impossible value (8 bytes at offset 15).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(MESSAGE_HEADER_LENGTH + 2, Long.MAX_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Invalid segment size detected"));
    }

    @Test
    public void throwsOnSegmentCrcMismatch() throws IOException {
        byte[] data = new byte[512];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 512, StructuredMessageFlags.STORAGE_CRC64);

        // msgHdr(13) + segHdr(10) + payload(512) + segCrc(8) + msgCrc(8). Flip one bit of the segment CRC.
        int segmentCrcOffset = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + data.length;
        encodedBytes[segmentCrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in segment"));
    }

    @Test
    public void throwsOnSegmentCrcMismatchInLaterSegment() throws IOException {
        // Multi-segment message where segment 1 is intact but segment 2's CRC is corrupted; verifies
        // CRC validation runs on every segment, not just the first.
        byte[] data = new byte[300];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 100, StructuredMessageFlags.STORAGE_CRC64);

        // Per-segment block: segHdr(10) + payload(100) + segCrc(8) = 118.
        // Segment 2's CRC starts at: msgHdr(13) + segBlock(118) + segHdr(10) + payload(100).
        int seg2CrcOffset
            = MESSAGE_HEADER_LENGTH + (SEGMENT_HEADER_LENGTH + 100 + CRC64_LENGTH) + SEGMENT_HEADER_LENGTH + 100;
        encodedBytes[seg2CrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in segment 2"));
    }

    @Test
    public void throwsOnMessageCrcMismatch() throws IOException {
        byte[] data = new byte[512];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 512, StructuredMessageFlags.STORAGE_CRC64);

        int messageCrcOffset = encodedBytes.length - CRC64_LENGTH;
        byte[] corrupted = Arrays.copyOf(encodedBytes, encodedBytes.length);
        corrupted[messageCrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(corrupted.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(corrupted)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in message footer"));
    }

    @Test
    public void throwsOnUnsupportedFlags() throws IOException {
        // Flags value 2 is not in the StructuredMessageFlags enum (NONE=0, STORAGE_CRC64=1) and must be rejected.
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // Flags live at offset 9 (2 bytes, little-endian).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(9, (short) 2);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Invalid value for StructuredMessageFlags"));
    }

    @Test
    public void throwsOnZeroSegments() throws IOException {
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // numSegments lives at offset 11 (2 bytes, little-endian). Force it to zero.
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(11, (short) 0);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("at least one segment"));
    }

    @Test
    public void throwsOnSkippedSegmentNumber() throws IOException {
        // 3 segments of 100 bytes each. Layout per segment: segHdr(10) + payload(100) + segCrc(8) = 118.
        // Rewrite segment 2's number to 3 to simulate a stream that skips segment 2.
        byte[] data = new byte[300];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 100, StructuredMessageFlags.STORAGE_CRC64);

        int seg2NumberOffset = MESSAGE_HEADER_LENGTH + (SEGMENT_HEADER_LENGTH + 100 + CRC64_LENGTH);
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(seg2NumberOffset, (short) 3);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Unexpected segment number"));
    }

    @Test
    public void truncatedMessageHeaderLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        // Feed only the first 5 bytes of the 13-byte message header.
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, 5);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer decodedPayload = decoder.decodeChunk(chunk);
        assertNull(decodedPayload);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedSegmentHeaderLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 256, StructuredMessageFlags.STORAGE_CRC64);

        // Feed full message header + 5 of the 10 segment-header bytes.
        int truncated = MESSAGE_HEADER_LENGTH + 5;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer decodedPayload = decoder.decodeChunk(chunk);
        assertNull(decodedPayload);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedSegmentFooterLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Layout: msgHdr(13) + segHdr(10) + payload(128) + segCrc(8) + msgCrc(8). Truncate mid-segCrc.
        int truncated = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + 128 + 4;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer decodedPayload = decoder.decodeChunk(chunk);
        assertNull(decodedPayload);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedMessageFooterLeavesDecoderIncomplete() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Feed everything except the last 4 bytes of the 8-byte message CRC footer.
        int truncated = encodedBytes.length - 4;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer decodedPayload = decoder.decodeChunk(chunk);
        // Segment payload has been released, but the message footer is still incomplete.
        assertNotNull(decodedPayload);
        assertFalse(decoder.isComplete());
    }

    @Test
    public void extraBytesAfterMessageFooterAreNotConsumed() throws IOException {
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        // Append garbage bytes after the message footer; the decoder must stop at the declared message length.
        int extras = 16;
        byte[] padded = new byte[encodedBytes.length + extras];
        System.arraycopy(encodedBytes, 0, padded, 0, encodedBytes.length);
        byte[] noise = new byte[extras];
        ThreadLocalRandom.current().nextBytes(noise);
        System.arraycopy(noise, 0, padded, encodedBytes.length, extras);
        ByteBuffer buffer = ByteBuffer.wrap(padded);
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteBuffer decodedPayload = decoder.decodeChunk(buffer);

        assertTrue(decoder.isComplete());
        assertNotNull(decodedPayload);
        byte[] decodedData = new byte[decodedPayload.remaining()];
        decodedPayload.get(decodedData);
        assertArrayEquals(data, decodedData);
        // Trailing bytes must not be consumed; buffer position stops at the declared message length.
        assertEquals(extras, buffer.remaining());
    }

    @Test
    public void throwsOnEncodedPayloadLargerThanExpectedSize() throws IOException {
        // Wire payload is larger than the expectedEncodedMessageLength supplied to the decoder. Must be rejected by the msgLen check.
        byte[] data = new byte[128];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length - 8);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("did not match content length"));
    }

    @Test
    public void throwsOnNegativeMessageLength() throws IOException {
        // msgLen lives at offset 1 (8 bytes, little-endian). A negative value must be rejected before
        // any further bounds math runs.
        byte[] data = new byte[64];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 64, StructuredMessageFlags.STORAGE_CRC64);

        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(1, Long.MIN_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Message length too small"));
    }

    @Test
    public void throwsOnNegativeSegmentSize() throws IOException {
        // Companion to throwsOnInvalidSegmentSize covering the negative-value branch of the segment-size check.
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 256, StructuredMessageFlags.STORAGE_CRC64);

        // Segment size lives at offset MESSAGE_HEADER_LENGTH + 2 (after the 2-byte segment number).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(MESSAGE_HEADER_LENGTH + 2, Long.MIN_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Invalid segment size detected"));
    }

    @Test
    public void throwsOnInjectedRandomByte() throws IOException {
        // Insert a single random byte at a random offset in the encoded wire bytes. The msgLen field still
        // declares the original size, so any insertion must be rejected by validation.
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        int insertAt = ThreadLocalRandom.current().nextInt(encodedBytes.length + 1);
        byte injected = (byte) ThreadLocalRandom.current().nextInt(256);

        byte[] tampered = new byte[encodedBytes.length + 1];
        System.arraycopy(encodedBytes, 0, tampered, 0, insertAt);
        tampered[insertAt] = injected;
        System.arraycopy(encodedBytes, insertAt, tampered, insertAt + 1, encodedBytes.length - insertAt);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(tampered.length);
        assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(tampered)));
    }

    @Test
    public void throwsOnRemovedRandomBytes() throws IOException {
        // Remove a random run of bytes from a random offset in the encoded wire. The msgLen field still
        // declares the original size, so any deletion must be rejected by validation.
        byte[] data = new byte[256];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] encodedBytes = encodeToBytes(data, 128, StructuredMessageFlags.STORAGE_CRC64);

        int removeCount = 1 + ThreadLocalRandom.current().nextInt(8);
        int removeAt = ThreadLocalRandom.current().nextInt(encodedBytes.length - removeCount);

        byte[] tampered = new byte[encodedBytes.length - removeCount];
        System.arraycopy(encodedBytes, 0, tampered, 0, removeAt);
        System.arraycopy(encodedBytes, removeAt + removeCount, tampered, removeAt,
            encodedBytes.length - removeAt - removeCount);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(tampered.length);
        assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(tampered)));
    }

    // For tests that pass the whole encoded message to decodeChunk.
    private static ByteBuffer encodeToByteBuffer(byte[] originalData, int segmentLength, StructuredMessageFlags flags)
        throws IOException {
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, segmentLength, flags);
        Flux<ByteBuffer> flux = encoder.encode(ByteBuffer.wrap(originalData));

        return ByteBuffer.wrap(Objects.requireNonNull(FluxUtil.collectBytesInByteBufferStream(flux).block()));
    }

    // For tests that need random access/mutation/splitting of encoded bytes.
    private static byte[] encodeToBytes(byte[] originalData, int segmentLength, StructuredMessageFlags flags)
        throws IOException {
        ByteBuffer encoded = encodeToByteBuffer(originalData, segmentLength, flags);
        byte[] encodedBytes = new byte[encoded.remaining()];
        encoded.get(encodedBytes);
        return encodedBytes;
    }

}
