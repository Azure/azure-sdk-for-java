// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.storage.common.implementation.StorageCrc64Calculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class MessageEncoderTests {

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
        StructuredMessageFlags structuredMessageFlags) throws IOException {
        int segmentCount = Math.max(1, (int) Math.ceil((double) data.capacity() / segmentSize));
        int segmentFooterLength = structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64 ? CRC64_LENGTH : 0;

        int messageLength = V1_HEADER_LENGTH + ((V1_SEGMENT_HEADER_LENGTH + segmentFooterLength) * segmentCount)
            + data.capacity() + (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64 ? CRC64_LENGTH : 0);

        long messageCRC = 0;

        // Message Header
        ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN); //1 + 8 + 2 + 2
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
                    if (i == -1) {
                        segmentCrc += 5;
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
            Arguments.of(1024 * 1024, 512, StructuredMessageFlags.NONE),
            Arguments.of(1024 * 1024, 512, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024 * 1024, 1024, StructuredMessageFlags.NONE),
            Arguments.of(1024 * 1024, 1024, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024 * 1024 * 4, 1024 * 1024, StructuredMessageFlags.NONE),
            Arguments.of(1024 * 1024 * 4, 1024 * 1024, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1024 * 1024 * 8, 1024 * 1024, StructuredMessageFlags.NONE),
            Arguments.of(1024 * 1024 * 8, 1024 * 1024, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1234, 123, StructuredMessageFlags.NONE),
            Arguments.of(1234, 123, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1234 * 10, 12, StructuredMessageFlags.NONE),
            Arguments.of(1234 * 10, 12, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1234 * 1234, 567, StructuredMessageFlags.NONE),
            Arguments.of(1234 * 1234, 567, StructuredMessageFlags.STORAGE_CRC64),
            Arguments.of(1234 * 1234 * 8, 1234 * 1234, StructuredMessageFlags.NONE),
            Arguments.of(1234 * 1234 * 8, 1234 * 1234, StructuredMessageFlags.STORAGE_CRC64));
    }

    @ParameterizedTest
    @MethodSource("readAllSupplier")
    public void readAll(int size, int segmentSize, StructuredMessageFlags flags) throws IOException {
        byte[] data = getRandomData(size);

        ByteBuffer unencodedBuffer = ByteBuffer.wrap(data);

        StructuredMessageEncoder structuredMessageEncoder = new StructuredMessageEncoder(size, segmentSize, flags);

        byte[] actual = structuredMessageEncoder.encode(unencodedBuffer).array();
        byte[] expected = buildStructuredMessage(unencodedBuffer, segmentSize, flags).array();

        Assertions.assertArrayEquals(expected, actual);
    }

    private static Stream<Arguments> readMultipleSupplier() {
        return Stream.of(Arguments.of(30, StructuredMessageFlags.NONE),
            Arguments.of(30, StructuredMessageFlags.STORAGE_CRC64), Arguments.of(15, StructuredMessageFlags.NONE),
            Arguments.of(15, StructuredMessageFlags.STORAGE_CRC64), Arguments.of(11, StructuredMessageFlags.NONE),
            Arguments.of(11, StructuredMessageFlags.STORAGE_CRC64), Arguments.of(8, StructuredMessageFlags.NONE),
            Arguments.of(8, StructuredMessageFlags.STORAGE_CRC64));
    }

    @ParameterizedTest
    @MethodSource("readMultipleSupplier")
    public void readMultiple(int segmentSize, StructuredMessageFlags flags) throws IOException {
        byte[] data1 = getRandomData(10);
        byte[] data2 = getRandomData(10);
        byte[] data3 = getRandomData(10);

        ByteBuffer wrappedData1 = ByteBuffer.wrap(data1);
        ByteBuffer wrappedData2 = ByteBuffer.wrap(data2);
        ByteBuffer wrappedData3 = ByteBuffer.wrap(data3);

        ByteBuffer allWrappedData = ByteBuffer.allocate(30);
        allWrappedData.put(data1);
        allWrappedData.put(data2);
        allWrappedData.put(data3);

        StructuredMessageEncoder structuredMessageEncoder = new StructuredMessageEncoder(30, segmentSize, flags);

        byte[] expected = buildStructuredMessage(allWrappedData, segmentSize, flags).array();

        ByteArrayOutputStream allActualData = new ByteArrayOutputStream();
        allActualData.write(structuredMessageEncoder.encode(wrappedData1).array());
        allActualData.write(structuredMessageEncoder.encode(wrappedData2).array());
        allActualData.write(structuredMessageEncoder.encode(wrappedData3).array());

        Assertions.assertArrayEquals(expected, allActualData.toByteArray());
    }
}
