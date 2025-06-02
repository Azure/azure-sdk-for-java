// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.storage.common.implementation.StorageCrc64Calculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class MessageDecoderTests {

    private static final int V1_HEADER_LENGTH = 13;
    private static final int V1_SEGMENT_HEADER_LENGTH = 10;
    private static final int CRC64_LENGTH = 8;

    private static byte[] getRandomData(int size) {
        byte[] result = new byte[size];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    private static void writeSegment(int number, byte[] data, long dataCrc, ByteArrayOutputStream stream)
        throws IOException {
        ByteBuffer segHeader = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN); //2 + 8
        segHeader.putShort((short) number);
        segHeader.putLong(data.length);

        stream.write(segHeader.array()); // Write segment header
        stream.write(data); // Write segment content
        if (dataCrc != -1) {
            ByteBuffer segFooter = ByteBuffer.allocate(CRC64_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            segFooter.putLong(dataCrc);
            stream.write(segFooter.array()); // Write segment footer
        }
    }

    private static ByteBuffer buildStructuredMessage(ByteBuffer data, int segmentSize,
        StructuredMessageFlags structuredMessageFlags, Integer invalidSegment) throws IOException {
        int segmentCount = Math.max(1, (int) Math.ceil((double) data.capacity() / segmentSize));
        int segmentFooterLength = structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64 ? CRC64_LENGTH : 0;

        int messageLength = V1_HEADER_LENGTH + ((V1_SEGMENT_HEADER_LENGTH + segmentFooterLength) * segmentCount)
            + data.capacity() + (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64 ? CRC64_LENGTH : 0);

        long messageCRC = 0;

        // Message Header
        ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0x01);
        buffer.putLong(messageLength);
        buffer.putShort((short) structuredMessageFlags.getValue());
        buffer.putShort((short) segmentCount);

        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(buffer.array());

        if (data.capacity() == 0) {
            int crc = structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64 ? 0 : -1;
            writeSegment(1, data.array(), crc, message);
        } else {
            // Segments
            int[] segmentSizes = new int[segmentCount];
            Arrays.fill(segmentSizes, segmentSize);

            int offset = 0;
            for (int i = 1; i <= segmentCount; i++) {
                int size = segmentSizes[i - 1];
                byte[] segmentData = customCopyOfRange(data, offset, size);
                offset += size;

                long segmentCrc = -1;
                if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
                    segmentCrc = StorageCrc64Calculator.compute(segmentData, 0);
                    if (invalidSegment != null && i == invalidSegment) {  // Introduce CRC Mismatch Here
                        segmentCrc += 5;  // Corrupt the CRC value for this segment
                    }
                }
                writeSegment(i, segmentData, segmentCrc, message);

                messageCRC = StorageCrc64Calculator.compute(segmentData, messageCRC);
            }
        }

        // Message footer
        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            byte[] crcBytes
                = ByteBuffer.allocate(CRC64_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putLong(messageCRC).array();
            message.write(crcBytes);
        }

        return ByteBuffer.wrap(message.toByteArray());
    }

    public static byte[] customCopyOfRange(ByteBuffer original, int from, int size) {
        int end = Math.min(from + size, original.capacity());
        return Arrays.copyOfRange(original.array(), from, end);
    }

    private static Stream<Arguments> readAllSupplier() {
        return Stream.of(Arguments.of(10, 1, StructuredMessageFlags.NONE),
            Arguments.of(10, 1, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024, 1024, StructuredMessageFlags.NONE),
            Arguments.of(1024, 1024, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024, 512, StructuredMessageFlags.NONE),
            Arguments.of(1024, 512, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024, 200, StructuredMessageFlags.NONE),
            Arguments.of(1024, 200, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024 * 10, 2, StructuredMessageFlags.NONE),
            Arguments.of(1024 * 10, 2, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024 * 50, 512, StructuredMessageFlags.NONE),
            Arguments.of(1024 * 50, 512, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(123456, 1234, StructuredMessageFlags.NONE),
            Arguments.of(123456, 1234, StructuredMessageFlags.STORAGE_CRC64));
    }

    @ParameterizedTest
    @MethodSource("readAllSupplier")
    public void testReadAll(int size, int segmentSize, StructuredMessageFlags flags) throws IOException {
        byte[] data = getRandomData(size);
        ByteBuffer encodedBuffer = buildStructuredMessage(ByteBuffer.wrap(data), segmentSize, flags, null);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());
        ByteBuffer decodedBuffer = decoder.decode(encodedBuffer);

        byte[] decodedData = new byte[decodedBuffer.remaining()];
        decodedBuffer.get(decodedData);

        // Call finalizeDecoding to ensure the entire message has been processed
        decoder.finalizeDecoding();

        Assertions.assertArrayEquals(data, decodedData);
    }

    @Test
    public void testReadPastEnd() throws IOException {
        byte[] data = getRandomData(10);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 10, StructuredMessageFlags.STORAGE_CRC64, null);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());
        ByteBuffer decodedBuffer = decoder.decode(encodedBuffer);

        byte[] decodedData = new byte[decodedBuffer.remaining()];
        decodedBuffer.get(decodedData);

        Assertions.assertArrayEquals(data, decodedData);
        Assertions.assertArrayEquals(new byte[0], decoder.decode(encodedBuffer, 10).array()); // Should return empty on subsequent reads
        // Call finalizeDecoding to ensure the entire message has been processed
        decoder.finalizeDecoding();
    }

    @Test
    public void testEmptyStream() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(emptyBuffer.capacity());
        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(emptyBuffer));
    }

    private static Stream<Arguments> crcMismatchSupplier() {
        return Stream.of(Arguments.of(1), Arguments.of(2), Arguments.of(3));
    }

    @ParameterizedTest
    @MethodSource("crcMismatchSupplier")
    public void testCrcMismatchReadAll(int invalidSegment) throws IOException {
        byte[] data = getRandomData(3 * 1024);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 1024, StructuredMessageFlags.STORAGE_CRC64, invalidSegment);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());

        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(encodedBuffer));
    }

    @Test
    public void testRandomReads() throws IOException {
        int dataSize = 1024;
        byte[] data = getRandomData(dataSize);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), dataSize / 3, StructuredMessageFlags.STORAGE_CRC64, null);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Random random = new Random();
        int readCount = 0;

        while (readCount < dataSize) {
            int readSize = Math.min(random.nextInt(50) + 1, dataSize - readCount);
            ByteBuffer chunkBuffer = decoder.decode(encodedBuffer, readSize);

            byte[] chunk = new byte[chunkBuffer.remaining()];
            chunkBuffer.get(chunk);

            result.write(chunk);
            readCount += chunk.length;
        }
        // Call finalizeDecoding to ensure the entire message has been processed
        decoder.finalizeDecoding();

        Assertions.assertArrayEquals(data, result.toByteArray());
    }

    @Test
    public void testInvalidMessageVersion() throws IOException {
        byte[] data = getRandomData(1024);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 512, StructuredMessageFlags.NONE, null);

        // Modify the first byte (message version) to an invalid value (0xFF)
        encodedBuffer.put(0, (byte) 0xFF);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());
        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(encodedBuffer));
    }

    @ParameterizedTest
    @MethodSource("invalidMessageLengths")
    public void testIncorrectMessageLength(long invalidMessageLength) throws IOException {
        byte[] data = getRandomData(1024);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 512, StructuredMessageFlags.NONE, null);

        // Modify the message length field
        encodedBuffer.putLong(1, invalidMessageLength);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());
        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(encodedBuffer));
    }

    private static Stream<Arguments> invalidMessageLengths() {
        return Stream.of(Arguments.of(100L), Arguments.of(1234567L)); // Incorrect message lengths
    }

    @ParameterizedTest
    @MethodSource("invalidSegmentCounts")
    public void testIncorrectSegmentCount(int invalidSegmentCount) throws IOException {
        byte[] data = getRandomData(1024);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 256, StructuredMessageFlags.NONE, null);

        // Modify the segment count
        encodedBuffer.putShort(11, (short) invalidSegmentCount);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());
        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(encodedBuffer));
    }

    private static Stream<Arguments> invalidSegmentCounts() {
        return Stream.of(Arguments.of(2), Arguments.of(123)); // Incorrect segment counts
    }

    @ParameterizedTest
    @MethodSource("invalidSegmentNumbers")
    public void testIncorrectSegmentNumber(int invalidSegmentNumber) throws IOException {
        byte[] data = getRandomData(1024);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 256, StructuredMessageFlags.NONE, null);

        // Modify the second segment's number
        int position = V1_HEADER_LENGTH + V1_SEGMENT_HEADER_LENGTH + 256;
        encodedBuffer.putShort(position, (short) invalidSegmentNumber);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());
        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(encodedBuffer));
    }

    private static Stream<Arguments> invalidSegmentNumbers() {
        return Stream.of(Arguments.of(123, StructuredMessageFlags.NONE),
            Arguments.of(345, StructuredMessageFlags.STORAGE_CRC64));
    }

    @ParameterizedTest
    @MethodSource("invalidSegmentNumbers")
    public void testIncorrectSegmentSize(int invalidSegmentSize, StructuredMessageFlags flags) throws IOException {
        byte[] data = getRandomData(1024);
        ByteBuffer encodedBuffer = buildStructuredMessage(ByteBuffer.wrap(data), 256, flags, null);

        byte[] encodedArray = encodedBuffer.array();
        ByteBuffer modifiableBuffer = ByteBuffer.wrap(encodedArray).order(ByteOrder.LITTLE_ENDIAN);

        boolean hasCrc64 = flags == StructuredMessageFlags.STORAGE_CRC64;
        int crcLength = hasCrc64 ? CRC64_LENGTH : 0;

        int position = V1_HEADER_LENGTH + V1_SEGMENT_HEADER_LENGTH  // First segment header
            + 256  // First segment data
            + crcLength  // First segment CRC if present
            + 2;  // The actual position where segment size is stored

        modifiableBuffer.putShort(position, (short) invalidSegmentSize);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(ByteBuffer.wrap(encodedArray).capacity());

        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(ByteBuffer.wrap(encodedArray)));
    }

    @Test
    public void testIncorrectSegmentSizeSingleSegment() throws IOException {
        byte[] data = getRandomData(256);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 256, StructuredMessageFlags.NONE, null);

        // Modify the segment size (15th byte)
        encodedBuffer.putShort(15, (short) 123);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());
        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(encodedBuffer));
    }

    @ParameterizedTest
    @MethodSource("crcMismatchSupplier")
    public void testCrcMismatchReadChunks(int invalidSegment) throws IOException {
        byte[] data = getRandomData(3 * 1024);
        ByteBuffer encodedBuffer
            = buildStructuredMessage(ByteBuffer.wrap(data), 1024, StructuredMessageFlags.STORAGE_CRC64, invalidSegment);

        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedBuffer.capacity());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            int read = 0;
            while (read < data.length) {
                decoder.decode(encodedBuffer, 512); // Read in chunks
                read += 512;
            }
            // Call finalizeDecoding to ensure the entire message has been processed
            decoder.finalizeDecoding();
        });
    }
}
