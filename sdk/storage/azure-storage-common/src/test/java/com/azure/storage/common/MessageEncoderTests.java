// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.implementation.StorageCrc64Calculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
            ByteBuffer segFooter
                = ByteBuffer.allocate(StructuredMessageEncoder.CRC64_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            segFooter.putLong(dataCrc);
            stream.write(segFooter.array()); // Write segment footer
        }
    }

    private static ByteBuffer buildStructuredMessage(byte[] data, int segmentSize, Flags flags,
        int invalidateCrcSegment) throws IOException {
        int segmentCount = Math.max(1, (int) Math.ceil((double) data.length / segmentSize));
        int segmentFooterLength = flags == Flags.STORAGE_CRC64 ? StructuredMessageEncoder.CRC64_LENGTH : 0;

        int messageLength = StructuredMessageEncoder.V1_HEADER_LENGTH
            + ((StructuredMessageEncoder.V1_SEGMENT_HEADER_LENGTH + segmentFooterLength) * segmentCount) + data.length
            + (flags == Flags.STORAGE_CRC64 ? StructuredMessageEncoder.CRC64_LENGTH : 0);

        long messageCRC = 0;

        // Message Header
        ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN); //1 + 8 + 2 + 2
        buffer.put((byte) 0x01);
        buffer.putLong(messageLength);
        buffer.putShort((short) flags.getValue());
        buffer.putShort((short) segmentCount);

        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(buffer.array());

        if (data.length == 0) {
            int crc = flags == Flags.STORAGE_CRC64 ? 0 : -1;
            writeSegment(1, data, crc, message);
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
                if (flags == Flags.STORAGE_CRC64) {
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
        if (flags == Flags.STORAGE_CRC64) {
            if (invalidateCrcSegment == -1) {
                messageCRC += 5;
            }
            byte[] crcBytes = ByteBuffer.allocate(StructuredMessageEncoder.CRC64_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(messageCRC)
                .array();
            message.write(crcBytes);
        }

        return ByteBuffer.wrap(message.toByteArray());
    }

    public static byte[] customCopyOfRange(byte[] original, int from, int size) {
        int end = Math.min(from + size, original.length);
        return Arrays.copyOfRange(original, from, end);
    }

    private static Stream<Arguments> readAllSupplier() {
        return Stream.of(Arguments.of(0, 1, Flags.NONE), Arguments.of(0, 1, Flags.STORAGE_CRC64),
            Arguments.of(10, 1, Flags.NONE), Arguments.of(10, 1, Flags.STORAGE_CRC64),
            Arguments.of(1024, 1024, Flags.NONE), Arguments.of(1024, 1024, Flags.STORAGE_CRC64),
            Arguments.of(1024, 512, Flags.NONE), Arguments.of(1024, 512, Flags.STORAGE_CRC64),
            Arguments.of(1024, 200, Flags.NONE), Arguments.of(1024, 200, Flags.STORAGE_CRC64),
            Arguments.of(1024 * 10, 2, Flags.NONE), Arguments.of(1024 * 10, 2, Flags.STORAGE_CRC64),
            Arguments.of(1025 * 50, 512, Flags.NONE), Arguments.of(1024 * 50, 512, Flags.STORAGE_CRC64),
            Arguments.of(1024 * 1024, 512, Flags.NONE), Arguments.of(1024 * 1024, 512, Flags.STORAGE_CRC64),
            Arguments.of(1024 * 1024, 1024, Flags.NONE), Arguments.of(1024 * 1024, 1024, Flags.STORAGE_CRC64),
            Arguments.of(1024 * 1024 * 4, 1024 * 1024, Flags.NONE),
            Arguments.of(1024 * 1024 * 4, 1024 * 1024, Flags.STORAGE_CRC64),
            Arguments.of(1024 * 1024 * 8, 1024 * 1024, Flags.NONE),
            Arguments.of(1024 * 1024 * 8, 1024 * 1024, Flags.STORAGE_CRC64), Arguments.of(1234, 123, Flags.NONE),
            Arguments.of(1234, 123, Flags.STORAGE_CRC64), Arguments.of(1234 * 10, 12, Flags.NONE),
            Arguments.of(1234 * 10, 12, Flags.STORAGE_CRC64), Arguments.of(1234 * 1234, 567, Flags.NONE),
            Arguments.of(1234 * 1234, 567, Flags.STORAGE_CRC64), Arguments.of(1234 * 1234 * 8, 1234 * 1234, Flags.NONE),
            Arguments.of(1234 * 1234 * 8, 1234 * 1234, Flags.STORAGE_CRC64));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("readAllSupplier")
    public void readAll(int size, int segmentSize, Flags flags) throws IOException {
        byte[] data = getRandomData(size);

        ByteBuffer innerBuffer = ByteBuffer.wrap(data);

        StructuredMessageEncoder structuredMessageEncoder = new StructuredMessageEncoder(segmentSize, flags);

        byte[] actual = structuredMessageEncoder.encode(innerBuffer).array();
        byte[] expected = buildStructuredMessage(data, segmentSize, flags, 0).array();
        
        Assertions.assertArrayEquals(expected, actual);
    }
}
