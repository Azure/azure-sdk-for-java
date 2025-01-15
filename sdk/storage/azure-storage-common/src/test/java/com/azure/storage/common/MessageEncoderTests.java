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
        int segmentCount = (int) Math.ceil((double) data.length / segmentSize);
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
            Integer crc = flags == Flags.STORAGE_CRC64 ? 0 : null;
            writeSegment(1, data, crc, message);
        } else {
            // Segments
            int[] segmentSizes = new int[segmentCount];
            for (int i = 0; i < segmentCount; i++) {
                segmentSizes[i] = segmentSize;
            }

            int offset = 0;
            for (int i = 1; i <= segmentCount; i++) {
                int size = segmentSizes[i - 1];
                byte[] segmentData = new byte[size];
                System.arraycopy(data, offset, segmentData, 0, size);
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

    @Test
    public void readAllNoFlag() throws IOException {
        int size = 1024;
        int segment = 512;
        Flags flags = Flags.NONE;

        byte[] data = getRandomData(size);

        ByteBuffer innerBuffer = ByteBuffer.wrap(data);
        StructuredMessageEncoder structuredMessageEncoder
            = new StructuredMessageEncoder(innerBuffer, size, segment, flags);

        ByteBuffer actual = structuredMessageEncoder.encode(-1);
        ByteBuffer expected = buildStructuredMessage(data, segment, flags, 0);

        if (!Arrays.equals(expected.array(), actual.array())) {
            Assertions.assertArrayEquals(expected.array(), actual.array());
        }
    }

    @Test
    public void readAllWithFlag() throws IOException {
        int size = 1024;
        int segment = 512;
        Flags flags = Flags.STORAGE_CRC64;

        byte[] data = getRandomData(size);

        ByteBuffer innerBuffer = ByteBuffer.wrap(data);
        StructuredMessageEncoder structuredMessageEncoder
            = new StructuredMessageEncoder(innerBuffer, size, segment, flags);

        ByteBuffer actual = structuredMessageEncoder.encode(-1);
        ByteBuffer expected = buildStructuredMessage(data, segment, flags, 0);

        if (!Arrays.equals(expected.array(), actual.array())) {
            Assertions.assertArrayEquals(expected.array(), actual.array());
        }
    }
}
