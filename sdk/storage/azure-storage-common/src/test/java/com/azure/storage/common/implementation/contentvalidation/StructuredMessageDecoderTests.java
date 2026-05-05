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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for StructuredMessageDecoder with focus on the validated-emission guarantee:
 * payload bytes for a segment are only returned after the segment's CRC has been verified.
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

        // Corrupt first segment number from 1 to 2 (offset 13 in v1 format).
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

        // Corrupt first segment size to an impossible value (offsets 15..22 in v1 format).
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

        // Layout for one-segment message:
        // messageHeader(13) + segmentHeader(10) + payload(512) + segmentCrc(8) + messageCrc(8)
        int segmentCrcOffset = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + data.length;
        encodedBytes[segmentCrcOffset] ^= 0x01;

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBytes.length);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> decoder.decodeChunk(ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN)));
        assertTrue(exception.getMessage().contains("CRC64 mismatch in segment"));
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

    // ===========================================================================================
    // Fuzzy encoder + decoder roundtrip
    //
    // Deterministic (payloadBytes, segmentBytes) grid that drives encode -> decode and verifies
    // byte-for-byte equality. Covers awkward boundaries (segmentSize - 1 / +1, many tiny segments,
    // single-segment large payloads, non-power-of-two sizes) so size-specific regressions surface
    // over time without random nondeterminism.
    // ===========================================================================================

    private static byte[] deterministicPayload(int size, int seedBase) {
        byte[] data = new byte[size];
        long state
            = 0x9E3779B97F4A7C15L ^ ((long) seedBase * 0xBF58476D1CE4E5B9L) ^ ((long) size * 0x94D049BB133111EBL);
        for (int i = 0; i < size; i++) {
            state ^= state >>> 30;
            state *= 0xBF58476D1CE4E5B9L;
            state ^= state >>> 27;
            state *= 0x94D049BB133111EBL;
            state ^= state >>> 31;
            data[i] = (byte) state;
        }
        return data;
    }

    static Stream<Arguments> fuzzyStructuredMessageRoundTripCases() {
        return Stream.of(
            // 1 byte payloads (encoder rejects 0; minimum supported is 1).
            Arguments.of(1, 1), Arguments.of(1, 64),
            // many tiny segments.
            Arguments.of(64, 1), Arguments.of(127, 1), Arguments.of(257, 7),
            // segment-size boundaries: -1 / exact / +1.
            Arguments.of(63, 64), Arguments.of(64, 64), Arguments.of(65, 64), Arguments.of(255, 256),
            Arguments.of(256, 256), Arguments.of(257, 256),
            // single segment, awkward sizes (non-power-of-two).
            Arguments.of(7 * 1024 + 3, 16 * 1024), Arguments.of(41 * 1024 + 17, 128 * 1024),
            // multiple segments with awkward last-segment lengths.
            Arguments.of(199 * 1024 + 5, 32 * 1024), Arguments.of(7 * 1024 + 3, 1024),
            Arguments.of(199 * 1024 + 5, 4 * 1024 + 17), Arguments.of(512 * 1024 - 31, 8 * 1024),
            // large segment sizes / large payloads (kept under a few MiB to keep unit tests fast).
            Arguments.of(1 * 1024 * 1024, 1 * 1024 * 1024), Arguments.of(1 * 1024 * 1024 + 1, 1 * 1024 * 1024),
            Arguments.of(1 * 1024 * 1024 - 1, 1 * 1024 * 1024), Arguments.of(2 * 1024 * 1024 + 333, 256 * 1024),
            Arguments.of(4 * 1024 * 1024 + 1, 1024 * 1024 + 17));
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageRoundTripCases")
    public void fuzzyStructuredMessageRoundTrip(int payloadBytes, int segmentBytes) throws IOException {
        String assertionMessage
            = "Fuzzy structured-message round trip payloadBytes=" + payloadBytes + ", segmentBytes=" + segmentBytes;

        byte[] data = deterministicPayload(payloadBytes, segmentBytes);
        byte[] encoded = encode(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encoded.length);
        ByteBuffer result = decoder.decodeChunk(ByteBuffer.wrap(encoded).order(ByteOrder.LITTLE_ENDIAN));

        assertTrue(decoder.isComplete(), assertionMessage);
        assertNotNull(result, assertionMessage);
        byte[] decoded = new byte[result.remaining()];
        result.get(decoded);
        assertArrayEquals(data, decoded, assertionMessage);
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageRoundTripCases")
    public void fuzzyStructuredMessageRoundTripNoCrc(int payloadBytes, int segmentBytes) throws IOException {
        String assertionMessage = "Fuzzy structured-message round trip (no-CRC) payloadBytes=" + payloadBytes
            + ", segmentBytes=" + segmentBytes;

        byte[] data = deterministicPayload(payloadBytes, segmentBytes ^ 0x55);
        byte[] encoded = encode(data, segmentBytes, StructuredMessageFlags.NONE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encoded.length);
        ByteBuffer result = decoder.decodeChunk(ByteBuffer.wrap(encoded).order(ByteOrder.LITTLE_ENDIAN));

        assertTrue(decoder.isComplete(), assertionMessage);
        assertNotNull(result, assertionMessage);
        byte[] decoded = new byte[result.remaining()];
        result.get(decoded);
        assertArrayEquals(data, decoded, assertionMessage);
    }

    // ===========================================================================================
    // Fuzzy decoder corruption (deterministic mutation positions)
    //
    // Encoded layout (single CRC-protected segment): msgHeader(13) + segHeader(10) + payload(N) +
    // segCrc(8) + msgCrc(8). For multi-segment encodings these tests pick offsets that fall inside
    // structural elements (after message header, inside a segment payload, near the segment footer,
    // near the message footer, near end). Each mutation must surface as IllegalArgumentException.
    // ===========================================================================================

    private static int[] deterministicMutationOffsets(int encodedLength, int payloadLength, int segmentLength) {
        // Single-segment layout offsets used as deterministic anchors:
        //   afterHeader      = 13 (right after message header, inside first segment header)
        //   insidePayload    = 13 + 10 + min(payloadLength - 1, segmentLength / 2)
        //   nearSegmentFooter = 13 + 10 + payloadLength + 1 (inside seg CRC)
        //   nearMessageFooter = encodedLength - 4
        //   nearEnd           = encodedLength - 1
        int afterHeader = MESSAGE_HEADER_LENGTH;
        int insidePayload = MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH
            + Math.min(Math.max(payloadLength - 1, 0), segmentLength / 2);
        int nearSegmentFooter
            = Math.min(MESSAGE_HEADER_LENGTH + SEGMENT_HEADER_LENGTH + payloadLength + 1, encodedLength - 1);
        int nearMessageFooter = Math.max(encodedLength - 4, 0);
        int nearEnd = encodedLength - 1;
        return new int[] { afterHeader, insidePayload, nearSegmentFooter, nearMessageFooter, nearEnd };
    }

    static Stream<Arguments> fuzzyStructuredMessageCorruptionCases() {
        // (payloadBytes, segmentBytes) tuples. We expand each (payload, segment) by every
        // deterministic mutation offset inside the runner to keep the supplier compact.
        return Stream.of(Arguments.of(64, 64), Arguments.of(257, 64), Arguments.of(1024, 256),
            Arguments.of(7 * 1024 + 3, 1024), Arguments.of(199 * 1024 + 5, 32 * 1024),
            Arguments.of(1 * 1024 * 1024 + 1, 1 * 1024 * 1024));
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageCorruptionCases")
    public void fuzzyStructuredMessageRejectsInjectedByte(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = deterministicPayload(payloadBytes, segmentBytes ^ 0xA5);
        byte[] encoded = encode(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        for (int offset : deterministicMutationOffsets(encoded.length, payloadBytes, segmentBytes)) {
            // Inject a byte derived from the original byte at `offset` (XOR 0xFF) so the value is
            // guaranteed different from what was there before. Truncating back to encoded.length keeps
            // the decoder's expected length unchanged but drops the original last byte, which means
            // the fed buffer always differs from the original encoding in at least one position.
            byte injectByte = (byte) (encoded[offset] ^ 0xFF);
            byte[] mutated = new byte[encoded.length + 1];
            System.arraycopy(encoded, 0, mutated, 0, offset);
            mutated[offset] = injectByte;
            System.arraycopy(encoded, offset, mutated, offset + 1, encoded.length - offset);

            String assertionMessage = "Fuzzy structured-message rejects injected byte payloadBytes=" + payloadBytes
                + ", segmentBytes=" + segmentBytes + ", mutationOffset=" + offset;

            StructuredMessageDecoder decoder = new StructuredMessageDecoder(encoded.length);
            assertThrows(IllegalArgumentException.class,
                () -> decoder.decodeChunk(ByteBuffer.wrap(mutated, 0, encoded.length).order(ByteOrder.LITTLE_ENDIAN)),
                assertionMessage);
        }
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageCorruptionCases")
    public void fuzzyStructuredMessageRejectsRemovedBytes(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = deterministicPayload(payloadBytes, segmentBytes ^ 0x3C);
        byte[] encoded = encode(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        for (int offset : deterministicMutationOffsets(encoded.length, payloadBytes, segmentBytes)) {
            // Single-byte removal: drop one byte at offset and feed the shorter buffer to a decoder
            // sized for the shorter buffer. The encoded message header still reports the original
            // length, so the header length check catches the truncation regardless of where the
            // removal landed.
            byte[] mutated = new byte[encoded.length - 1];
            System.arraycopy(encoded, 0, mutated, 0, offset);
            System.arraycopy(encoded, offset + 1, mutated, offset, encoded.length - offset - 1);

            String assertionMessage = "Fuzzy structured-message rejects removed byte payloadBytes=" + payloadBytes
                + ", segmentBytes=" + segmentBytes + ", mutationOffset=" + offset;

            StructuredMessageDecoder decoder = new StructuredMessageDecoder(mutated.length);
            assertThrows(IllegalArgumentException.class,
                () -> decoder.decodeChunk(ByteBuffer.wrap(mutated).order(ByteOrder.LITTLE_ENDIAN)), assertionMessage);
        }
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageCorruptionCases")
    public void fuzzyStructuredMessageRejectsRemovedRange(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = deterministicPayload(payloadBytes, segmentBytes ^ 0x71);
        byte[] encoded = encode(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        // Remove a deterministic 4-byte range starting from each anchor offset (clamped so we don't
        // overrun the encoded buffer).
        final int rangeLen = 4;
        for (int rawOffset : deterministicMutationOffsets(encoded.length, payloadBytes, segmentBytes)) {
            int offset = Math.min(rawOffset, encoded.length - rangeLen - 1);
            if (offset < 0) {
                continue;
            }
            byte[] mutated = new byte[encoded.length - rangeLen];
            System.arraycopy(encoded, 0, mutated, 0, offset);
            System.arraycopy(encoded, offset + rangeLen, mutated, offset, encoded.length - offset - rangeLen);

            String assertionMessage = "Fuzzy structured-message rejects removed bytes payloadBytes=" + payloadBytes
                + ", segmentBytes=" + segmentBytes + ", mutationOffset=" + offset + ", rangeLen=" + rangeLen;

            StructuredMessageDecoder decoder = new StructuredMessageDecoder(mutated.length);
            assertThrows(IllegalArgumentException.class,
                () -> decoder.decodeChunk(ByteBuffer.wrap(mutated).order(ByteOrder.LITTLE_ENDIAN)), assertionMessage);
        }
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageCorruptionCases")
    public void fuzzyStructuredMessageRejectsFlippedByte(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = deterministicPayload(payloadBytes, segmentBytes ^ 0x1B);
        byte[] encoded = encode(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        // Flip one byte at deterministic structural anchors: payload mutation triggers CRC mismatch,
        // header/footer mutations may trigger structural validation or CRC mismatch.
        for (int offset : deterministicMutationOffsets(encoded.length, payloadBytes, segmentBytes)) {
            byte[] mutated = Arrays.copyOf(encoded, encoded.length);
            mutated[offset] ^= 0x01;

            String assertionMessage = "Fuzzy structured-message rejects flipped byte payloadBytes=" + payloadBytes
                + ", segmentBytes=" + segmentBytes + ", mutationOffset=" + offset;

            StructuredMessageDecoder decoder = new StructuredMessageDecoder(mutated.length);
            assertThrows(IllegalArgumentException.class,
                () -> decoder.decodeChunk(ByteBuffer.wrap(mutated).order(ByteOrder.LITTLE_ENDIAN)), assertionMessage);
        }
    }

}
