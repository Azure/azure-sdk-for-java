// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            Arguments.of(123456, 1234, Flags.NONE), Arguments.of(123456, 1234, Flags.STORAGE_CRC64),
            Arguments.of(10 * 1024, 1, Flags.NONE), Arguments.of(10 * 1024, 1, Flags.STORAGE_CRC64),
            Arguments.of(50 * 1024, 512, Flags.NONE), Arguments.of(50 * 1024, 512, Flags.STORAGE_CRC64));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("readAllSupplier")
    public void readAll(int size, int segment, Flags flags) throws IOException {
        Path filePath = Paths.get("C:\\randomData\\" + size + "_random_bytes.bin");
        byte[] data = Files.readAllBytes(filePath);

        ByteBuffer innerBuffer = ByteBuffer.wrap(data);
        StructuredMessageEncoder structuredMessageEncoder
            = new StructuredMessageEncoder(innerBuffer, size, segment, flags);

        byte[] actual = structuredMessageEncoder.encode(-1).array();
        byte[] expected = buildStructuredMessage(data, segment, flags, 0).array();

        if (!Arrays.equals(expected, actual)) {
            Assertions.assertArrayEquals(expected, actual);
        }
    }

    @Disabled
    @Test
    public void readAllWithOutput() throws IOException {
        int[] sizes = {
            0,
            0,
            10,
            10,
            1024,
            1024,
            1024,
            1024,
            1024,
            1024,
            123456,
            123456,
            10 * 1024,
            10 * 1024,
            50 * 1024,
            50 * 1024 };
        int[] segmentSizes = { 1, 1, 1, 1, 1024, 1024, 512, 512, 200, 200, 1234, 1234, 1, 1, 512, 512 };
        Flags[] flags = {
            Flags.NONE,
            Flags.STORAGE_CRC64,
            Flags.NONE,
            Flags.STORAGE_CRC64,
            Flags.NONE,
            Flags.STORAGE_CRC64,
            Flags.NONE,
            Flags.STORAGE_CRC64,
            Flags.NONE,
            Flags.STORAGE_CRC64,
            Flags.NONE,
            Flags.STORAGE_CRC64,
            Flags.NONE,
            Flags.STORAGE_CRC64,
            Flags.NONE,
            Flags.STORAGE_CRC64 };

        String outputFilePath = "javaOutput.txt";

        for (int i = 0; i < sizes.length; i++) {
            Path inputFilePath = Paths.get("C:\\randomData\\" + sizes[i] + "_random_bytes.bin");
            byte[] data = Files.readAllBytes(inputFilePath);

            ByteBuffer innerBuffer = ByteBuffer.wrap(data);
            StructuredMessageEncoder structuredMessageEncoder
                = new StructuredMessageEncoder(innerBuffer, sizes[i], segmentSizes[i], flags[i]);

            byte[] actual = structuredMessageEncoder.encode(-1).array();

            BigInteger unsignedCRC64 = structuredMessageEncoder.getUnsignedCRC64();

            int[] unsignedInts = new int[actual.length];
            for (int j = 0; j < actual.length; j++) {
                unsignedInts[j] = actual[j] & 0xFF;
            }

            System.out.println(Arrays.toString(unsignedInts));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                String headerContent
                    = "Size: " + sizes[i] + ", Segment Size: " + segmentSizes[i] + ", Flags: " + flags[i] + "\n";
                writer.write(headerContent);
                writer.write("Unsigned CRC64: " + unsignedCRC64 + "\n");
                writer.write("Unsigned encoded stream: " + Arrays.toString(unsignedInts) + "\n\n\n");

            }
        }
    }
}
