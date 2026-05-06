// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.FluxUtil;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageDecoder;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageEncoder;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageFlags;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Encoder + decoder fuzzy coverage.
 */
public final class BlobContentValidationStructuredMessageFuzzyTests {

    private static ByteBuffer collectStructuredMessageFlux(Flux<ByteBuffer> flux) {
        return ByteBuffer.wrap(FluxUtil.collectBytesInByteBufferStream(flux).block()).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static byte[] encodeStructuredMessage(byte[] originalData, int segmentLength, StructuredMessageFlags flags)
        throws IOException {
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, segmentLength, flags);
        ByteBuffer encoded = collectStructuredMessageFlux(encoder.encode(ByteBuffer.wrap(originalData)));
        byte[] encodedBytes = new byte[encoded.remaining()];
        encoded.get(encodedBytes);
        return encodedBytes;
    }

    private static byte[] structuredMessageDeterministicPayload(int size, int seedBase) {
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
        return Stream.of(Arguments.of(1, 1), Arguments.of(1, 64), Arguments.of(64, 1), Arguments.of(127, 1),
            Arguments.of(257, 7), Arguments.of(63, 64), Arguments.of(64, 64), Arguments.of(65, 64),
            Arguments.of(255, 256), Arguments.of(256, 256), Arguments.of(257, 256),
            Arguments.of(7 * 1024 + 3, 16 * 1024), Arguments.of(41 * 1024 + 17, 128 * 1024),
            Arguments.of(199 * 1024 + 5, 32 * 1024), Arguments.of(7 * 1024 + 3, 1024),
            Arguments.of(199 * 1024 + 5, 4 * 1024 + 17), Arguments.of(512 * 1024 - 31, 8 * 1024),
            Arguments.of(1 * 1024 * 1024, 1 * 1024 * 1024), Arguments.of(1 * 1024 * 1024 + 1, 1 * 1024 * 1024),
            Arguments.of(1 * 1024 * 1024 - 1, 1 * 1024 * 1024), Arguments.of(2 * 1024 * 1024 + 333, 256 * 1024),
            Arguments.of(4 * 1024 * 1024 + 1, 1024 * 1024 + 17));
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageRoundTripCases")
    void fuzzyStructuredMessageRoundTrip(int payloadBytes, int segmentBytes) throws IOException {
        String assertionMessage
            = "Fuzzy structured-message round trip payloadBytes=" + payloadBytes + ", segmentBytes=" + segmentBytes;

        byte[] data = structuredMessageDeterministicPayload(payloadBytes, segmentBytes);
        byte[] encoded = encodeStructuredMessage(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

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
    void fuzzyStructuredMessageRoundTripNoCrc(int payloadBytes, int segmentBytes) throws IOException {
        String assertionMessage = "Fuzzy structured-message round trip (no-CRC) payloadBytes=" + payloadBytes
            + ", segmentBytes=" + segmentBytes;

        byte[] data = structuredMessageDeterministicPayload(payloadBytes, segmentBytes ^ 0x55);
        byte[] encoded = encodeStructuredMessage(data, segmentBytes, StructuredMessageFlags.NONE);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encoded.length);
        ByteBuffer result = decoder.decodeChunk(ByteBuffer.wrap(encoded).order(ByteOrder.LITTLE_ENDIAN));

        assertTrue(decoder.isComplete(), assertionMessage);
        assertNotNull(result, assertionMessage);
        byte[] decoded = new byte[result.remaining()];
        result.get(decoded);
        assertArrayEquals(data, decoded, assertionMessage);
    }

    private static int[] structuredMessageDeterministicMutationOffsets(int encodedLength, int payloadLength,
        int segmentLength) {
        int afterHeader = StructuredMessageConstants.V1_HEADER_LENGTH;
        int insidePayload
            = StructuredMessageConstants.V1_HEADER_LENGTH + StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH
                + Math.min(Math.max(payloadLength - 1, 0), segmentLength / 2);
        int nearSegmentFooter = Math.min(StructuredMessageConstants.V1_HEADER_LENGTH
            + StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH + payloadLength + 1, encodedLength - 1);
        int nearMessageFooter = Math.max(encodedLength - 4, 0);
        int nearEnd = encodedLength - 1;
        return new int[] { afterHeader, insidePayload, nearSegmentFooter, nearMessageFooter, nearEnd };
    }

    static Stream<Arguments> fuzzyStructuredMessageCorruptionCases() {
        return Stream.of(Arguments.of(64, 64), Arguments.of(257, 64), Arguments.of(1024, 256),
            Arguments.of(7 * 1024 + 3, 1024), Arguments.of(199 * 1024 + 5, 32 * 1024),
            Arguments.of(1 * 1024 * 1024 + 1, 1 * 1024 * 1024));
    }

    @ParameterizedTest
    @MethodSource("fuzzyStructuredMessageCorruptionCases")
    void fuzzyStructuredMessageRejectsInjectedByte(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = structuredMessageDeterministicPayload(payloadBytes, segmentBytes ^ 0xA5);
        byte[] encoded = encodeStructuredMessage(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        for (int offset : structuredMessageDeterministicMutationOffsets(encoded.length, payloadBytes, segmentBytes)) {
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
    void fuzzyStructuredMessageRejectsRemovedBytes(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = structuredMessageDeterministicPayload(payloadBytes, segmentBytes ^ 0x3C);
        byte[] encoded = encodeStructuredMessage(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        for (int offset : structuredMessageDeterministicMutationOffsets(encoded.length, payloadBytes, segmentBytes)) {
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
    void fuzzyStructuredMessageRejectsRemovedRange(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = structuredMessageDeterministicPayload(payloadBytes, segmentBytes ^ 0x71);
        byte[] encoded = encodeStructuredMessage(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        final int rangeLen = 4;
        for (int rawOffset : structuredMessageDeterministicMutationOffsets(encoded.length, payloadBytes,
            segmentBytes)) {
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
    void fuzzyStructuredMessageRejectsFlippedByte(int payloadBytes, int segmentBytes) throws IOException {
        byte[] data = structuredMessageDeterministicPayload(payloadBytes, segmentBytes ^ 0x1B);
        byte[] encoded = encodeStructuredMessage(data, segmentBytes, StructuredMessageFlags.STORAGE_CRC64);

        for (int offset : structuredMessageDeterministicMutationOffsets(encoded.length, payloadBytes, segmentBytes)) {
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
