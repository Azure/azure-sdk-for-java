// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MessageEncoderTests {
    private static byte[] getRandomData(int size) {
        byte[] result = new byte[size];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    private static void write_Segment(int number, byte[] data, long dataCrc, ByteArrayOutputStream stream)
        throws IOException {
        System.out.println("Entering write_Segment method");
        System.out.println("Segment number: " + number);
        System.out.println("Data length: " + data.length);
        System.out.println("Data CRC: " + dataCrc);

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

        System.out.println("Exiting write_Segment method");
    }

    private static ByteBuffer build_structured_message(byte[] data, int segmentSize, Flags flags,
        int invalidateCrcSegment) throws IOException {
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("Entering build_structured_message method");
        System.out.println("Data length: " + data.length);
        System.out.println("Segment size: " + segmentSize);
        System.out.println("Flags: " + flags);
        //        System.out.println("Invalidate CRC segment: " + invalidateCrcSegment);

        int segmentCount = (int) Math.ceil((double) data.length / segmentSize);
        System.out.println("Segment count: " + segmentCount);
        int segmentFooterLength = flags == Flags.STORAGE_CRC64 ? StructuredMessageEncoder.CRC64_LENGTH : 0;
        System.out.println("Segment footer length: " + segmentFooterLength);

        int messageLength = StructuredMessageEncoder.V1_HEADER_LENGTH
            + ((StructuredMessageEncoder.V1_SEGMENT_HEADER_LENGTH + segmentFooterLength) * segmentCount) + data.length
            + (flags == Flags.STORAGE_CRC64 ? StructuredMessageEncoder.CRC64_LENGTH : 0);
        System.out.println("Message length: " + messageLength);

        long messageCRC = 0;

        // Message Header
        System.out.println("Writing message header");

        ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN); //1 + 8 + 2 + 2
        buffer.put((byte) 0x01);
        buffer.putLong(messageLength);
        buffer.putShort((short) flags.getValue());
        buffer.putShort((short) segmentCount);

        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(buffer.array());

        System.out.println(Arrays.toString(message.toByteArray()));

        if (data.length == 0) {
            System.out.println("Special case for 0 length content");
            Integer crc = flags == Flags.STORAGE_CRC64 ? 0 : null;
            System.out.println("Computed CRC for 0 length content: " + crc);
            write_Segment(1, data, crc, message);
            System.out.println("Segment 1 written with CRC: " + crc);
        } else {
            // Segments
            int[] segmentSizes = new int[segmentCount];
            for (int i = 0; i < segmentCount; i++) {
                segmentSizes[i] = segmentSize;
                System.out.println("Segment size for segment " + (i + 1) + ": " + segmentSize);
            }

            int offset = 0;
            for (int i = 1; i <= segmentCount; i++) {
                int size = segmentSizes[i - 1];
                byte[] segmentData = new byte[size];
                System.arraycopy(data, offset, segmentData, 0, size);
                System.out.println("Copied data for segment " + i + " from offset " + offset + " with size " + size);
                offset += size;

                long segmentCrc = -1;
                if (flags == Flags.STORAGE_CRC64) {
                    segmentCrc = StorageCrc64Calculator.compute(segmentData, 0);
                    System.out.println("Computed CRC for segment " + i + ": " + segmentCrc);
                    if (i == -1) {
                        segmentCrc += 5;
                        System.out.println("Invalidated CRC for segment " + i + ": " + segmentCrc);
                    }
                }
                write_Segment(i, segmentData, segmentCrc, message);

                messageCRC = StorageCrc64Calculator.compute(segmentData, messageCRC);
                System.out
                    .println("Segment " + i + " written, segment CRC: " + segmentCrc + ", message : " + messageCRC);
            }
        }

        // Message footer
        if (flags == Flags.STORAGE_CRC64) {
            if (invalidateCrcSegment == -1) {
                messageCRC += 5;
                System.out.println("Invalidated message CRC: " + messageCRC);
            }
            byte[] crcBytes = ByteBuffer.allocate(StructuredMessageEncoder.CRC64_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(messageCRC)
                .array();
            message.write(crcBytes);
            System.out.println("Message footer written, message CRC: " + messageCRC);
        }

        System.out.println("Exiting build_structured_message method");
        return ByteBuffer.wrap(message.toByteArray());
    }

    @Test
    public void temp() throws IOException {
        System.out.println("Entering test_read_all_2 function");

        int size = 1024;
        int segment = 512;
        Flags flags = Flags.NONE;

        System.out.println("Size: " + size);
        System.out.println("Segment size: " + segment);
        System.out.println("Flags: " + flags);

        byte[] data = getRandomData(size);

        System.out.println("Generated random data of size: " + data.length);

        ByteBuffer innerBuffer = ByteBuffer.wrap(data);
        System.out.println("Created inner stream");
        StructuredMessageEncoder structuredMessageEncoder
            = new StructuredMessageEncoder(innerBuffer, size, segment, flags);
        System.out.println("Created StructuredMessageEncoder");

        ByteBuffer actual = structuredMessageEncoder.encode(-1);
        System.out.println("Encoded data");

        ByteBuffer expected = build_structured_message(data, segment, flags, 0);
        System.out.println("Built expected structured message");

        System.out.println("Actual data length: " + actual.array().length);
        System.out.println("Expected data length: " + expected.array().length);

        System.out.println("Actual data: " + Arrays.toString(actual.array()));
        System.out.println("Expected data: " + Arrays.toString(expected.array()));

        if (!Arrays.equals(expected.array(), actual.array())) {
            Assertions.assertArrayEquals(expected.array(), actual.array());
        }

        System.out.println("Assertion passed, actual data matches expected data");
    }

    @Test
    public void temp2() throws IOException {
        System.out.println("Entering test_read_all_2 function");

        int size = 1024;
        int segment = 512;
        Flags flags = Flags.STORAGE_CRC64;

        System.out.println("Size: " + size);
        System.out.println("Segment size: " + segment);
        System.out.println("Flags: " + flags);

        byte[] data = getRandomData(size);

        System.out.println("Generated random data of size: " + data.length);

        ByteBuffer innerBuffer = ByteBuffer.wrap(data);
        System.out.println("Created inner stream");
        StructuredMessageEncoder structuredMessageEncoder
            = new StructuredMessageEncoder(innerBuffer, size, segment, flags);
        System.out.println("Created StructuredMessageEncoder");

        ByteBuffer actual = structuredMessageEncoder.encode(-1);
        System.out.println("Encoded data");

        ByteBuffer expected = build_structured_message(data, segment, flags, 0);
        System.out.println("Built expected structured message");

        System.out.println("Actual data length: " + actual.array().length);
        System.out.println("Expected data length: " + expected.array().length);

        System.out.println("Actual data: " + Arrays.toString(actual.array()));
        System.out.println("Expected data: " + Arrays.toString(expected.array()));

        if (!Arrays.equals(expected.array(), actual.array())) {
            Assertions.assertArrayEquals(expected.array(), actual.array());
        }

        System.out.println("Assertion passed, actual data matches expected data");
    }
}
