//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.storage.common;
//
//import org.junit.jupiter.api.Test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class MessageEncoderTests {
//    private static byte[] getRandomData(int size) {
//        byte[] result = new byte[size];
//        ThreadLocalRandom.current().nextBytes(result);
//        return result;
//    }
//
//    private static ByteBuffer buildStructuredMessageForTest(byte[] data, int segmentSize, Flags flags) throws IOException {
//        int segmentCount = (int) Math.ceil((double) data.length / segmentSize);
//        int segmentFooterLength = flags == Flags.STORAGE_CRC64 ? StructuredMessageEncoder.CRC64_LENGTH : 0;
//
//        int messageLength = StructuredMessageEncoder.V1_HEADER_LENGTH + ((StructuredMessageEncoder.V1_SEGMENT_HEADER_LENGTH
//        + segmentFooterLength) * segmentCount) + data.length + (flags == Flags.STORAGE_CRC64 ? StructuredMessageEncoder.CRC64_LENGTH : 0);
//
//        ByteArrayOutputStream message = new ByteArrayOutputStream();
//        int messageCRC = 0;
//
//        //Message Header
//        message.write(0x01); //version
//        message.write(ByteBuffer.allocate(8).putLong(messageLength).array()); // Message length
//        message.write(ByteBuffer.allocate(2).put(flags.toString().getBytes()).array()); // Flags
//        message.write(ByteBuffer.allocate(2).putShort((short) segmentCount).array()); // Num segments
//
//        //Special case for 0 length content
//        if (data.length == 0) {
//            Integer crc = flags == Flags.STORAGE_CRC64 ? 0 : null;
//            writeSegment(1, data, crc, message);
//        } else {
//            //segments
//            int[] segmentSizes = new int[segmentCount];
//            for (int i = 0; i < segmentCount; i++) {
//                segmentSizes[i] = segmentSize;
//            }
//
//            int offset = 0;
//            for (int i = 1; i <= segmentCount; i++) {
//                int size = segmentSizes[i - 1];
//            }
//        }
//    }
//
//    private static void writeSegment(int number, byte[] data, Integer dataCrc, ByteArrayOutputStream stream) throws IOException {
//        stream.write(ByteBuffer.allocate(2).putShort((short) number).array()); // Write segment number
//        stream.write(ByteBuffer.allocate(8).putLong(data.length).array()); //Write segment length
//        stream.write(data); //Write segment content
//        if (dataCrc != null) {
//            stream.write(ByteBuffer.allocate(StructuredMessageEncoder.CRC64_LENGTH).putInt(dataCrc).array());
//        }
//    }
//
//    @Test
//    public void temp() throws IOException {
//        int size = 1024;
//        int segment = 512;
//        Flags flags = Flags.NONE;
//
////        int size = 1024;
////        int segment = 512;
////        Flags flags = Flags.STORAGE_CRC64;
//
//        byte[] data = getRandomData(size);
//        ByteBuffer innerBuffer = ByteBuffer.wrap(data);
//        ByteBuffer outputBuffer = ByteBuffer.allocate(1024);
//        StructuredMessageEncoder structuredMessageEncoder = new StructuredMessageEncoder(innerBuffer, size, segment, flags);
//        structuredMessageEncoder.encode(outputBuffer, -1);
//
//
//    }
//
//}
