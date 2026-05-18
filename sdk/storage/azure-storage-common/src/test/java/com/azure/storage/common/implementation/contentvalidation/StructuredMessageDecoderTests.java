// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    public void readsCompleteMessageInSingleChunk() {
        byte[] originalData = getRandomData(1024);
        ByteBuffer encodedData = encodeToByteBuffer(originalData, 512, StructuredMessageFlags.STORAGE_CRC64);
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(encodedData);

        assertTrue(decoder.isComplete());
        assertFalse(decodedPayload.isEmpty());
        assertArrayEquals(originalData, collectDecodedBytes(decodedPayload));
    }

    @Test
    public void readsTopLevelMessageHeaderSplitAcrossChunks() {
        byte[] originalData = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(originalData, 128);
        int encodedLength = encodedBytes.length;

        // Split before the 13-byte message header is complete.
        int messageHeaderSplitPoint = 7;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, messageHeaderSplitPoint);
        ByteBuffer chunk2
            = ByteBuffer.wrap(encodedBytes, messageHeaderSplitPoint, encodedLength - messageHeaderSplitPoint);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        List<ByteBuffer> firstDecodedPayload = decoder.decodeChunk(chunk1);
        assertTrue(firstDecodedPayload.isEmpty());
        assertFalse(decoder.isComplete());

        List<ByteBuffer> secondDecodedPayload = decoder.decodeChunk(chunk2);
        assertFalse(secondDecodedPayload.isEmpty());
        assertTrue(decoder.isComplete());
    }

    @Test
    public void readsPerSegmentHeaderSplitAcrossChunks() {
        byte[] originalData = getRandomData(512);
        byte[] encodedBytes = encodeToBytes(originalData, 256);
        int encodedLength = encodedBytes.length;

        // Split after the full message header but before the 10-byte segment header is complete.
        int segmentHeaderSplitPoint = MESSAGE_HEADER_LENGTH + 5;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, segmentHeaderSplitPoint);
        ByteBuffer chunk2
            = ByteBuffer.wrap(encodedBytes, segmentHeaderSplitPoint, encodedLength - segmentHeaderSplitPoint);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        List<ByteBuffer> firstDecodedPayload = decoder.decodeChunk(chunk1);
        // Segment header is incomplete, so nothing is emitted yet.
        assertTrue(firstDecodedPayload.isEmpty());
        assertFalse(decoder.isComplete());

        List<ByteBuffer> secondDecodedPayload = decoder.decodeChunk(chunk2);
        assertFalse(secondDecodedPayload.isEmpty());
        assertTrue(decoder.isComplete());
    }

    @Test
    public void multipleChunksDecode() {
        byte[] originalData = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(originalData, 128);
        int encodedLength = encodedBytes.length;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);

        int chunkSize = 32;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int offset = 0; offset < encodedLength; offset += chunkSize) {
            int len = Math.min(chunkSize, encodedLength - offset);
            ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, offset, len);

            List<ByteBuffer> decodedPayload = decoder.decodeChunk(chunk);
            writeDecodedPayload(output, decodedPayload);
            if (decoder.isComplete()) {
                break;
            }
        }

        assertTrue(decoder.isComplete());
        assertArrayEquals(originalData, output.toByteArray());
    }

    @Test
    public void decodeWithNoCrc() {
        byte[] originalData = getRandomData(256);
        ByteBuffer encodedData = encodeToByteBuffer(originalData, 128, StructuredMessageFlags.NONE);
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(encodedData);

        assertTrue(decoder.isComplete());
        assertFalse(decodedPayload.isEmpty());
        assertArrayEquals(originalData, collectDecodedBytes(decodedPayload));
    }

    @Test
    public void handlesZeroLengthBuffer() {
        byte[] originalData = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(originalData, 128);
        int encodedLength = encodedBytes.length;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        List<ByteBuffer> firstDecodedPayload = decoder.decodeChunk(emptyBuffer);
        assertTrue(firstDecodedPayload.isEmpty());
        ByteBuffer dataBuffer = ByteBuffer.wrap(encodedBytes);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(dataBuffer);
        assertFalse(decodedPayload.isEmpty());
        assertTrue(decoder.isComplete());
    }

    /**
     * Payload bytes for a segment must not be emitted until the segment's CRC footer has been read and
     * validated. While the footer is incomplete, decodeChunk must return null.
     */
    @Test
    public void withholdsPayloadUntilSegmentFooterValidated() {
        byte[] originalData = getRandomData(1024);
        byte[] encodedBytes = encodeToBytes(originalData, 1024);
        int encodedLength = encodedBytes.length;

        // The segment payload cannot be emitted until the final segment CRC byte arrives in chunk2.
        int segCrcAllButLast
            = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + 1024 + StructuredMessageConstants.CRC64_LENGTH - 1;
        ByteBuffer chunk1 = ByteBuffer.wrap(encodedBytes, 0, segCrcAllButLast);
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        List<ByteBuffer> firstDecodedPayload = decoder.decodeChunk(chunk1);
        assertTrue(firstDecodedPayload.isEmpty(), "Decoder must not emit payload before segment CRC is validated");
        assertFalse(decoder.isComplete());

        ByteBuffer chunk2 = ByteBuffer.wrap(encodedBytes, segCrcAllButLast, encodedLength - segCrcAllButLast);
        List<ByteBuffer> emittedPayload = decoder.decodeChunk(chunk2);
        assertFalse(emittedPayload.isEmpty());
        assertTrue(decoder.isComplete());
        assertArrayEquals(originalData, collectDecodedBytes(emittedPayload));
    }

    @Test
    public void throwsOnUnsupportedStructuredMessageVersion() {
        byte[] data = getRandomData(64);
        byte[] encodedBytes = encodeToBytes(data, 64);

        // Byte 0 of the message header is reserved for the version. Changing to an incompatible version (2)
        encodedBytes[0] = (byte) (StructuredMessageConstants.DEFAULT_MESSAGE_VERSION + 1);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Unsupported structured message version"));
    }

    @Test
    public void throwsOnMessageLengthMismatch() {
        byte[] data = getRandomData(128);
        byte[] encodedBytes = encodeToBytes(data, 128);

        // Construct decoder with wrong expected encoded length.
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length + 1);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("did not match content length"));
    }

    @Test
    public void throwsOnUnexpectedSegmentNumber() {
        byte[] data = getRandomData(300);
        byte[] encodedBytes = encodeToBytes(data, 128);

        // Corrupt first segment number from 1 to 2 (offset 13).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(MESSAGE_HEADER_LENGTH, (short) 2);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Unexpected segment number"));
    }

    @Test
    public void throwsOnInvalidSegmentSize() {
        byte[] data = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(data, 128);

        // Corrupt first segment size to an impossible value (8 bytes at offset 15).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(MESSAGE_HEADER_LENGTH + 2, Long.MAX_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Invalid segment size detected"));
    }

    @Test
    public void throwsOnSegmentCrcMismatch() {
        byte[] data = getRandomData(512);
        byte[] encodedBytes = encodeToBytes(data, 512);

        // msgHdr(13) + segHdr(10) + payload(512) + segCrc(8) + msgCrc(8). Flip one bit of the segment CRC.
        int segmentCrcOffset = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + data.length;
        encodedBytes[segmentCrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in segment"));
    }

    @Test
    public void throwsOnSegmentCrcMismatchInLaterSegment() {
        // Multi-segment message where segment 1 is intact but segment 2's CRC is corrupted; verifies
        // CRC validation runs on every segment, not just the first.
        byte[] data = getRandomData(300);
        byte[] encodedBytes = encodeToBytes(data, 100);

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
    public void throwsOnMessageCrcMismatch() {
        byte[] data = getRandomData(512);
        byte[] encodedBytes = encodeToBytes(data, 512);

        int messageCrcOffset = encodedBytes.length - CRC64_LENGTH;
        byte[] corrupted = Arrays.copyOf(encodedBytes, encodedBytes.length);
        corrupted[messageCrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(corrupted.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(corrupted)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in message footer"));
    }

    @Test
    public void throwsOnUnsupportedFlags() {
        // Flags value 2 is not in the StructuredMessageFlags enum (NONE=0, STORAGE_CRC64=1) and must be rejected.
        byte[] data = getRandomData(64);
        byte[] encodedBytes = encodeToBytes(data, 64);

        // Flags live at offset 9 (2 bytes, little-endian).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(9, (short) 2);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Invalid value for StructuredMessageFlags"));
    }

    @Test
    public void throwsOnZeroSegments() {
        byte[] data = getRandomData(64);
        byte[] encodedBytes = encodeToBytes(data, 64);

        // numSegments lives at offset 11 (2 bytes, little-endian). Force it to zero.
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(11, (short) 0);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("at least one segment"));
    }

    @Test
    public void throwsOnSkippedSegmentNumber() {
        // 3 segments of 100 bytes each. Layout per segment: segHdr(10) + payload(100) + segCrc(8) = 118.
        // Rewrite segment 2's number to 3 to simulate a stream that skips segment 2.
        byte[] data = getRandomData(300);
        byte[] encodedBytes = encodeToBytes(data, 100);

        int seg2NumberOffset = MESSAGE_HEADER_LENGTH + (SEGMENT_HEADER_LENGTH + 100 + CRC64_LENGTH);
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putShort(seg2NumberOffset, (short) 3);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Unexpected segment number"));
    }

    @Test
    public void truncatedMessageHeaderLeavesDecoderIncomplete() {
        byte[] data = getRandomData(64);
        byte[] encodedBytes = encodeToBytes(data, 64);

        // Feed only the first 5 bytes of the 13-byte message header.
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, 5);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(chunk);
        assertTrue(decodedPayload.isEmpty());
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedSegmentHeaderLeavesDecoderIncomplete() {
        byte[] data = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(data, 256);

        // Feed full message header + 5 of the 10 segment-header bytes.
        int truncated = MESSAGE_HEADER_LENGTH + 5;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(chunk);
        assertTrue(decodedPayload.isEmpty());
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedSegmentFooterLeavesDecoderIncomplete() {
        byte[] data = getRandomData(128);
        byte[] encodedBytes = encodeToBytes(data, 128);

        // Layout: msgHdr(13) + segHdr(10) + payload(128) + segCrc(8) + msgCrc(8). Truncate mid-segCrc.
        int truncated = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + 128 + 4;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(chunk);
        assertTrue(decodedPayload.isEmpty());
        assertFalse(decoder.isComplete());
    }

    @Test
    public void truncatedMessageFooterLeavesDecoderIncomplete() {
        byte[] data = getRandomData(128);
        byte[] encodedBytes = encodeToBytes(data, 128);

        // Feed everything except the last 4 bytes of the 8-byte message CRC footer.
        int truncated = encodedBytes.length - 4;
        ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, 0, truncated);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(chunk);
        // Segment payload has been released, but the message footer is still incomplete.
        assertFalse(decodedPayload.isEmpty());
        assertFalse(decoder.isComplete());
    }

    @Test
    public void extraBytesAfterMessageFooterAreNotConsumed() {
        byte[] data = getRandomData(128);
        byte[] encodedBytes = encodeToBytes(data, 128);

        // Append garbage bytes after the message footer; the decoder must stop at the declared message length.
        int extras = 16;
        byte[] padded = new byte[encodedBytes.length + extras];
        System.arraycopy(encodedBytes, 0, padded, 0, encodedBytes.length);
        byte[] noise = getRandomData(extras);
        System.arraycopy(noise, 0, padded, encodedBytes.length, extras);
        ByteBuffer buffer = ByteBuffer.wrap(padded);
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(buffer);

        assertTrue(decoder.isComplete());
        assertFalse(decodedPayload.isEmpty());
        assertArrayEquals(data, collectDecodedBytes(decodedPayload));
        // Trailing bytes must not be consumed; buffer position stops at the declared message length.
        assertEquals(extras, buffer.remaining());
    }

    @Test
    public void throwsOnEncodedPayloadLargerThanExpectedSize() {
        // Wire payload is larger than the expectedEncodedMessageLength supplied to the decoder. Must be rejected by the msgLen check.
        byte[] data = getRandomData(128);
        byte[] encodedBytes = encodeToBytes(data, 128);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length - 8);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("did not match content length"));
    }

    @Test
    public void throwsOnNegativeMessageLength() {
        // msgLen lives at offset 1 (8 bytes, little-endian). A negative value must be rejected before
        // any further bounds math runs.
        byte[] data = getRandomData(64);
        byte[] encodedBytes = encodeToBytes(data, 64);

        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(1, Long.MIN_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Message length too small"));
    }

    @Test
    public void throwsOnNegativeSegmentSize() {
        // Companion to throwsOnInvalidSegmentSize covering the negative-value branch of the segment-size check.
        byte[] data = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(data, 256);

        // Segment size lives at offset MESSAGE_HEADER_LENGTH + 2 (after the 2-byte segment number).
        ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(MESSAGE_HEADER_LENGTH + 2, Long.MIN_VALUE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes)));
        assertTrue(exception.getMessage().contains("Invalid segment size detected"));
    }

    @Test
    public void throwsOnInjectedRandomByte() {
        // Insert a single random byte at a random offset in the encoded wire bytes. The msgLen field still
        // declares the original size, so any insertion must be rejected by validation.
        byte[] data = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(data, 128);

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
    public void throwsOnRemovedRandomBytes() {
        // Remove a random run of bytes from a random offset in the encoded wire. The msgLen field still
        // declares the original size, so any deletion must be rejected by validation.
        byte[] data = getRandomData(256);
        byte[] encodedBytes = encodeToBytes(data, 128);

        int removeCount = 1 + ThreadLocalRandom.current().nextInt(8);
        int removeAt = ThreadLocalRandom.current().nextInt(encodedBytes.length - removeCount);

        byte[] tampered = new byte[encodedBytes.length - removeCount];
        System.arraycopy(encodedBytes, 0, tampered, 0, removeAt);
        System.arraycopy(encodedBytes, removeAt + removeCount, tampered, removeAt,
            encodedBytes.length - removeAt - removeCount);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(tampered.length);
        assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(tampered)));
    }

    /**
     * Multi-segment round-trip with CRC enabled. Exercises the per-segment CRC validation followed by the
     * message-wide CRC concat fold: if the concat math is wrong the trailing message footer check would fail
     * even though every individual segment CRC matched, so this test directly guards the concat optimization.
     */
    @Test
    public void multipleSegmentsRoundTripWithCrc() {
        // 16 segments of 1 KiB each. Small enough to feed in one chunk; large enough that there is meaningful
        // CRC accumulation across segments.
        int segmentSize = 1024;
        int numSegments = 16;
        byte[] originalData = getRandomData(segmentSize * numSegments);
        ByteBuffer encoded = encodeToByteBuffer(originalData, segmentSize, StructuredMessageFlags.STORAGE_CRC64);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encoded.remaining());
        List<ByteBuffer> decodedPayload = decoder.decodeChunk(encoded);

        assertTrue(decoder.isComplete());
        assertFalse(decodedPayload.isEmpty());
        assertArrayEquals(originalData, collectDecodedBytes(decodedPayload));
    }

    /**
     * Multi-segment round-trip with CRC where the decoder is fed many small chunks rather than the whole encoded
     * blob at once. This ensures the per-segment CRC computation is correct when payload bytes for a single
     * segment arrive split across many decodeChunk calls (the typical production wire pattern) and that the
     * O(1) concat fold at each segment boundary still produces a matching message CRC at the end.
     */
    @Test
    public void multipleSegmentsRoundTripWithCrcAcrossManyChunks() {
        int segmentSize = 4 * 1024;
        int numSegments = 8;
        byte[] originalData = getRandomData(segmentSize * numSegments);
        byte[] encodedBytes = encodeToBytes(originalData, segmentSize);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        ByteArrayOutputStream collected = new ByteArrayOutputStream();
        int chunkSize = 137; // deliberately non-power-of-two and smaller than the segment so footers split

        for (int offset = 0; offset < encodedBytes.length; offset += chunkSize) {
            int len = Math.min(chunkSize, encodedBytes.length - offset);
            ByteBuffer chunk = ByteBuffer.wrap(encodedBytes, offset, len);
            writeDecodedPayload(collected, decoder.decodeChunk(chunk));
        }

        assertTrue(decoder.isComplete());
        assertArrayEquals(originalData, collected.toByteArray());
    }

    /**
     * Zero-copy emission: the decoder's per-segment backing array is sized to the exact segment payload length so
     * the wrapped ByteBuffer's underlying array length matches segment size. Guards against accidental
     * reintroduction of intermediate buffering (e.g. a growing {@code ByteArrayOutputStream} that hands off an
     * oversized buffer).
     */
    @Test
    public void singleSegmentEmissionIsExactSized() {
        int segmentSize = 2 * 1024;
        byte[] originalData = getRandomData(segmentSize);
        ByteBuffer encoded = encodeToByteBuffer(originalData, segmentSize, StructuredMessageFlags.STORAGE_CRC64);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encoded.remaining());
        List<ByteBuffer> decoded = decoder.decodeChunk(encoded);

        assertEquals(1, decoded.size());
        assertTrue(decoder.isComplete());
        ByteBuffer payload = decoded.get(0);
        assertTrue(payload.hasArray());
        assertEquals(segmentSize, payload.remaining());
        assertEquals(segmentSize, payload.array().length);
        assertArrayEquals(originalData, collectDecodedBytes(decoded));
    }

    /**
     * Segments larger than {@code Integer.MAX_VALUE} cannot be backed by a single Java array, so the decoder must
     * reject them up front rather than blowing up at allocation time. The check lives in the segment-header parse;
     * we forge a header that declares a too-large segment size and confirm the failure.
     */
    @Test
    public void throwsOnSegmentSizeExceedingArrayLimit() {
        // Build a forged 13-byte message header + 10-byte segment header where the segment claims a payload size
        // larger than what a Java array can hold. Total message length is set so the segment-size check fires
        // before any other validation.
        long forgedSegmentSize = (long) Integer.MAX_VALUE; // > Integer.MAX_VALUE - 8 triggers the guard
        int messageHeaderLength = MESSAGE_HEADER_LENGTH;
        int segmentHeaderLength = SEGMENT_HEADER_LENGTH;
        long forgedMessageLength = messageHeaderLength + segmentHeaderLength + forgedSegmentSize;

        byte[] forged = new byte[messageHeaderLength + segmentHeaderLength];
        ByteBuffer hdr = ByteBuffer.wrap(forged).order(ByteOrder.LITTLE_ENDIAN);
        hdr.put((byte) StructuredMessageConstants.DEFAULT_MESSAGE_VERSION); // version
        hdr.putLong(forgedMessageLength); // total message length
        hdr.putShort((short) StructuredMessageFlags.NONE.getValue()); // flags
        hdr.putShort((short) 1); // numSegments
        hdr.putShort((short) 1); // segment number
        hdr.putLong(forgedSegmentSize); // segment size

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(forgedMessageLength);
        IllegalArgumentException ex
            = assertThrows(IllegalArgumentException.class, () -> decoder.decodeChunk(ByteBuffer.wrap(forged)));
        assertTrue(ex.getMessage().contains("exceeds the maximum supported segment size"));
    }

    /**
     * Multi-megabyte segment size  exercises per-segment length from the wire header,
     * not a fixed 4 MiB assumption.
     */
    @ParameterizedTest
    @MethodSource("segmentPayloadSizeAndTotalPayloadSizeSupplier")
    public void decodesMultiMegabyteSegments(int segmentPayloadSize, int totalPayloadSize) {
        byte[] originalData = getRandomData(totalPayloadSize);
        ByteBuffer encodedData = encodeToByteBuffer(originalData, segmentPayloadSize, StructuredMessageFlags.NONE);
        int encodedLength = encodedData.remaining();

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedLength);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        writeDecodedPayload(output, decoder.decodeChunk(encodedData));

        assertTrue(decoder.isComplete());
        assertArrayEquals(originalData, output.toByteArray());
    }

    private static Stream<Arguments> segmentPayloadSizeAndTotalPayloadSizeSupplier() {
        return Stream.of(Arguments.of(10 * 1024 * 1024, 10 * 1024 * 1024 + 1),
            Arguments.of(3 * 1024 * 1024, 3 * 1024 * 1024 + 1), Arguments.of(5 * 1024 * 1024 + 1, 15 * 1024 * 1024));
    }

    // For tests that pass the whole encoded message to decodeChunk.
    private static ByteBuffer encodeToByteBuffer(byte[] originalData, int segmentLength, StructuredMessageFlags flags) {
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, segmentLength, flags);
        Flux<ByteBuffer> flux = encoder.encode(ByteBuffer.wrap(originalData));

        return ByteBuffer.wrap(Objects.requireNonNull(FluxUtil.collectBytesInByteBufferStream(flux).block()));
    }

    // For tests that need random access/mutation/splitting of encoded bytes.
    private static byte[] encodeToBytes(byte[] originalData, int segmentLength) {
        ByteBuffer encoded = encodeToByteBuffer(originalData, segmentLength, StructuredMessageFlags.STORAGE_CRC64);
        byte[] encodedBytes = new byte[encoded.remaining()];
        encoded.get(encodedBytes);
        return encodedBytes;
    }

    private static byte[] getRandomData(int size) {
        byte[] result = new byte[size];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    private static byte[] collectDecodedBytes(List<ByteBuffer> decoded) {
        if (decoded.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (ByteBuffer buffer : decoded) {
            if (buffer != null && buffer.hasRemaining()) {
                byte[] decodedBytes = new byte[buffer.remaining()];
                buffer.get(decodedBytes);
                output.write(decodedBytes, 0, decodedBytes.length);
            }
        }
        return output.toByteArray();
    }

    private static void writeDecodedPayload(ByteArrayOutputStream output, List<ByteBuffer> decoded) {
        byte[] bytes = collectDecodedBytes(decoded);
        if (bytes != null) {
            output.write(bytes, 0, bytes.length);
        }
    }

}
