package com.azure.storage.common.implementation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

//
//public class StructuredMessageDecoder {
//
//    /**
//     * temp comment to allow building
//     */
//    private static final int DEFAULT_MESSAGE_VERSION = 1;
//    /**
//     * temp comment to allow building
//     */
//    private static final int V1_HEADER_LENGTH = 13;
//    /**
//     * temp comment to allow building
//     */
//    private static final int V1_SEGMENT_HEADER_LENGTH = 10;
//    /**
//     * temp comment to allow building
//     */
//    private static final int CRC64_LENGTH = 8;
//
//    private int messageVersion;
//    private int messageLength;
//    private StructuredMessageFlags flags;
//    private int numSegments;
//    private final ByteBuffer buffer;
//
//    private int messageOffset = 0;
//    private int currentSegmentNumber = 0;
//    private int currentSegmentContentLength = 0;
//    private int currentSegmentContentOffset = 0;
//
//    private long messageCrc64 = 0;
//    private long segmentCrc64 = 0;
//    private SMRegion currentRegion;
//    private final Map<Integer, Long> segmentCrcs = new HashMap<>();
//    // currentContentLength - to allow the decoder to decode in chunks and keeping track from content length to 0
//
//
//    private enum SMRegion {
//        MESSAGE_HEADER, MESSAGE_FOOTER, SEGMENT_HEADER, SEGMENT_FOOTER, SEGMENT_CONTENT,
//    }
//
//    public StructuredMessageDecoder(ByteBuffer inputBuffer) {
//        this.buffer = inputBuffer.order(ByteOrder.LITTLE_ENDIAN);
//        readMessageHeader();
//        // Validate supported versions
//        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
//            throw new IllegalArgumentException("Unsupported structured message version: " + messageVersion);
//        }
//        this.currentRegion = SMRegion.SEGMENT_HEADER;
//    }
//
//    /**
//     * Reads the structured message header.
//     */
//    private void readMessageHeader() {
//        if (buffer.remaining() < V1_HEADER_LENGTH) {
//            throw new IllegalArgumentException("Content not long enough to contain a valid message header.");
//        }
//
//        messageVersion = Byte.toUnsignedInt(buffer.get());
//        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
//            throw new IllegalArgumentException("Unsupported structured message version: " + messageVersion);
//        }
//
//        messageLength = (int) buffer.getLong();
//        flags = StructuredMessageFlags.fromValue(Short.toUnsignedInt(buffer.getShort()));
//        numSegments = Short.toUnsignedInt(buffer.getShort());
//
//        messageOffset += V1_HEADER_LENGTH;
//    }
//
////    private void readSegmentContent(ByteArrayOutputStream output) {
////        int toRead = Math.min(buffer.remaining(), currentSegmentContentLength - currentSegmentContentOffset);
////        byte[] content = new byte[toRead];
////        buffer.get(content);
////        output.write(content, 0, toRead);
////
////        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
////            segmentCrc64 = StorageCrc64Calculator.compute(content, segmentCrc64);
////            messageCrc64 = StorageCrc64Calculator.compute(content, messageCrc64);
////        }
////
////        messageOffset += toRead;
////        currentSegmentContentOffset += toRead;
////        if (currentSegmentContentOffset == currentSegmentContentLength) {
////            currentRegion = SMRegion.SEGMENT_FOOTER;
////        }
////    }
//private void readSegmentContent(ByteArrayOutputStream output) {
//    int toRead = Math.min(buffer.remaining(), currentSegmentContentLength - currentSegmentContentOffset);
//    byte[] content = new byte[toRead];
//    buffer.get(content);
//    output.write(content, 0, toRead);
//
//    // ✅ Compute CRC only after segment is fully read
//    if (flags == StructuredMessageFlags.STORAGE_CRC64) {
//        segmentCrc64 = StorageCrc64Calculator.compute(content, segmentCrc64);
//        messageCrc64 = StorageCrc64Calculator.compute(content, messageCrc64);
//    }
//
//    messageOffset += toRead;
//    currentSegmentContentOffset += toRead;
//    if (currentSegmentContentOffset == currentSegmentContentLength) {
//        currentRegion = SMRegion.SEGMENT_FOOTER;
//    }
//}
//
//    /**
//     * Reads the structured message footer, validating the CRC.
//     */
////    private void readMessageFooter() {
////        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
////            if (buffer.remaining() < CRC64_LENGTH) {
////                throw new IllegalArgumentException("Message footer is incomplete.");
////            }
////
////            long reportedCrc = buffer.getLong();
////            if (messageCrc64 != reportedCrc) {
////                throw new IllegalArgumentException("CRC64 mismatch detected in message footer.");
////            }
////        }
////        if (messageOffset != messageLength) {  // FIX: Validate message length
////            System.out.println("messageOffset: " + messageOffset);
////            System.out.println("messageLength: " + messageLength);
////            throw new IllegalArgumentException("Decoded message length does not match expected length.");
////        }
////    }
//    private void readMessageFooter() {
//        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
//            if (buffer.remaining() < CRC64_LENGTH) {
//                throw new IllegalArgumentException("Message footer is incomplete.");
//            }
//
//            long reportedCrc = buffer.order(ByteOrder.LITTLE_ENDIAN).getLong(); // Ensure little-endian order
//
//            if (messageCrc64 != reportedCrc) {
//                System.out.println("Computed CRC64: " + messageCrc64);
//                System.out.println("Reported CRC64: " + reportedCrc);
//                throw new IllegalArgumentException("CRC64 mismatch detected in message footer.");
//            }
//            messageOffset += CRC64_LENGTH; // ✅ Ensure messageOffset updates properly
//        }
//
//        if (messageOffset != messageLength) {
//            System.out.println("messageOffset: " + messageOffset);
//            System.out.println("messageLength: " + messageLength);
//            throw new IllegalArgumentException("Decoded message length does not match expected length.");
//        }
//    }
//
//
////    /**
////     * Reads the segment header.
////     */
////    private void readSegmentHeader() {
////        if (buffer.remaining() < V1_SEGMENT_HEADER_LENGTH) {
////            throw new IllegalArgumentException("Segment header is incomplete.");
////        }
////
////        int segmentNum = Short.toUnsignedInt(buffer.getShort());
////        int segmentSize = (int) buffer.getLong();
////
////        if (segmentNum != currentSegmentNumber + 1) {
////            throw new IllegalArgumentException("Unexpected segment number.");
////        }
////
////        currentSegmentNumber = segmentNum;
////        currentSegmentContentLength = segmentSize;
////        currentSegmentContentOffset = 0;
////        segmentCrc64 = 0;
////
////        messageOffset += V1_SEGMENT_HEADER_LENGTH;  // FIX: Increment messageOffset
////        currentRegion = SMRegion.SEGMENT_CONTENT;
////    }
//
//    private void readSegmentHeader() {
//        if (buffer.remaining() < V1_SEGMENT_HEADER_LENGTH) {
//            throw new IllegalArgumentException("Segment header is incomplete.");
//        }
//
//        int segmentNum = Short.toUnsignedInt(buffer.getShort());
//        int segmentSize = (int) buffer.getLong();
//
//        if (segmentNum != currentSegmentNumber + 1) {
//            throw new IllegalArgumentException("Unexpected segment number.");
//        }
//
//        currentSegmentNumber = segmentNum;
//        currentSegmentContentLength = segmentSize;
//        currentSegmentContentOffset = 0;
//
//        if (segmentSize == 0) {  // ✅ Handle empty segments properly
//            currentRegion = SMRegion.SEGMENT_FOOTER;
//        } else {
//            currentRegion = SMRegion.SEGMENT_CONTENT;
//        }
//
//        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
//            segmentCrc64 = 0;
//        }
//
//        messageOffset += V1_SEGMENT_HEADER_LENGTH;
//    }
//
//    /**
//     * Reads the segment footer, validating the segment CRC.
//     */
////    private void readSegmentFooter() {
////        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
////            if (buffer.remaining() < CRC64_LENGTH) {
////                throw new IllegalArgumentException("Segment footer is incomplete.");
////            }
////
////            long reportedCrc64 = buffer.getLong();
////            if (segmentCrc64 != reportedCrc64) {
////                throw new IllegalArgumentException("CRC64 mismatch detected in segment " + currentSegmentNumber);
////            }
////            segmentCrcs.put(currentSegmentNumber, segmentCrc64);
////        }
////
////        if (currentSegmentNumber == numSegments) {
////            currentRegion = SMRegion.MESSAGE_FOOTER;
////        } else {
////            currentRegion = SMRegion.SEGMENT_HEADER;
////            readSegmentHeader();
////        }
////    }
//    private void readSegmentFooter() {
//        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
//            if (buffer.remaining() < CRC64_LENGTH) {
//                throw new IllegalArgumentException("Segment footer is incomplete.");
//            }
//
//            long reportedCrc64 = buffer.order(ByteOrder.LITTLE_ENDIAN).getLong();
//            if (segmentCrc64 != reportedCrc64) {
//                throw new IllegalArgumentException("CRC64 mismatch detected in segment " + currentSegmentNumber);
//            }
//            segmentCrcs.put(currentSegmentNumber, segmentCrc64);
//            messageOffset += CRC64_LENGTH; // ✅ Increment messageOffset correctly
//        }
//
//        if (currentSegmentNumber == numSegments) {
//            currentRegion = SMRegion.MESSAGE_FOOTER;
//        } else {
//            currentRegion = SMRegion.SEGMENT_HEADER;
//            readSegmentHeader();
//        }
//    }
//
//
//    /**
//     * Decodes the structured message and returns the raw content as a byte array.
//     *
//     * @return The decoded raw content.
//     */
////    public byte[] decode() {
////        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();
////
////        while (buffer.hasRemaining()) {
////            switch (currentRegion) {
////                case SEGMENT_HEADER:
////                    readSegmentHeader();
////                    break;
////
////                case SEGMENT_CONTENT:
////                    int toRead
////                        = Math.min(buffer.remaining(), currentSegmentContentLength - currentSegmentContentOffset);
////                    byte[] content = new byte[toRead];
////                    buffer.get(content);
////                    decodedContent.write(content, 0, toRead);
////
////                    if (flags == StructuredMessageFlags.STORAGE_CRC64) {
////                        segmentCrc64 = StorageCrc64Calculator.compute(content, segmentCrc64);
////                        messageCrc64 = StorageCrc64Calculator.compute(content, messageCrc64);
////                    }
////
////                    currentSegmentContentOffset += toRead;
////                    if (currentSegmentContentOffset == currentSegmentContentLength) {
////                        currentRegion = SMRegion.SEGMENT_FOOTER;
////                    }
////                    break;
////
////                case SEGMENT_FOOTER:
////                    readSegmentFooter();
////                    break;
////
////                case MESSAGE_FOOTER:
////                    readMessageFooter();
////
////                    if (messageOffset != messageLength) {
////                        throw new IllegalArgumentException("Decoded message length does not match expected length.");
////                    }
////
////                    return decodedContent.toByteArray();
////            }
////        }
////        throw new IllegalStateException("Failed to fully decode the structured message.");
////    }
////    public byte[] decode() {
////        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();
////
////        while (buffer.hasRemaining()) {
////            switch (currentRegion) {
////                case MESSAGE_HEADER:
////                    readMessageHeader();
////                    break;
////                case SEGMENT_HEADER:
////                    readSegmentHeader();
////                    break;
////                case SEGMENT_CONTENT:
////                    readSegmentContent(decodedContent);
////                    break;
////                case SEGMENT_FOOTER:
////                    readSegmentFooter();
////                    break;
////                case MESSAGE_FOOTER:
////                    readMessageFooter();
////                    return decodedContent.toByteArray();
////            }
////        }
////
////        return decodedContent.toByteArray();
////    }
//    /**
//     * Reads a chunk of data from the structured message.
//     * @param size The maximum size of the data to read.
//     * @return The decoded data chunk.
//     */
//    public byte[] decode(int size) {
//        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();
//
//        while (buffer.hasRemaining() && decodedContent.size() < size) {
//            if (currentSegmentContentOffset == currentSegmentContentLength) {
//                readSegmentHeader();
//            }
//
//            int toRead = Math.min(buffer.remaining(), currentSegmentContentLength - currentSegmentContentOffset);
//            toRead = Math.min(toRead, size - decodedContent.size());
//
//            byte[] content = new byte[toRead];
//            buffer.get(content);
//            decodedContent.write(content, 0, toRead);
//
//            if (flags == StructuredMessageFlags.STORAGE_CRC64) {
//                segmentCrc64 = StorageCrc64Calculator.compute(content, segmentCrc64);
//                messageCrc64 = StorageCrc64Calculator.compute(content, messageCrc64);
//            }
//
//            currentSegmentContentOffset += toRead;
//
//            if (currentSegmentContentOffset == currentSegmentContentLength) {
//                readSegmentFooter();
//            }
//        }
//
//        return decodedContent.toByteArray();
//    }
//
//    /**
//     * Reads the entire message until the end.
//     * @return The fully decoded message content.
//     */
//    public byte[] decodeFully() throws IOException {
//        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();
//        byte[] chunk;
//
//        while ((chunk = decode(1024)).length > 0) { // Read in chunks of 1024
//            decodedContent.write(chunk);
//        }
//
//        return decodedContent.toByteArray();
//    }
//}
public class StructuredMessageDecoder {
    private static final int DEFAULT_MESSAGE_VERSION = 1;
    private static final int V1_HEADER_LENGTH = 13;
    private static final int V1_SEGMENT_HEADER_LENGTH = 10;
    private static final int CRC64_LENGTH = 8;

    private int messageVersion;
    private int messageLength;
    private StructuredMessageFlags flags;
    private int numSegments;
    private final ByteBuffer buffer;

    private int messageOffset = 0;
    private int currentSegmentNumber = 0;
    private int currentSegmentContentLength = 0;
    private int currentSegmentContentOffset = 0;

    private long messageCrc64 = 0;
    private long segmentCrc64 = 0;
    private final Map<Integer, Long> segmentCrcs = new HashMap<>();

    public StructuredMessageDecoder(ByteBuffer inputBuffer) {
        this.buffer = inputBuffer.order(ByteOrder.LITTLE_ENDIAN);
        readMessageHeader();
    }

    private void readMessageHeader() {
        if (buffer.remaining() < V1_HEADER_LENGTH) {
            throw new IllegalArgumentException("Content not long enough to contain a valid message header.");
        }

        messageVersion = Byte.toUnsignedInt(buffer.get());
        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
            throw new IllegalArgumentException("Unsupported structured message version: " + messageVersion);
        }

        messageLength = (int) buffer.getLong();
        flags = StructuredMessageFlags.fromValue(Short.toUnsignedInt(buffer.getShort()));
        numSegments = Short.toUnsignedInt(buffer.getShort());

        messageOffset += V1_HEADER_LENGTH;
    }

    private void readSegmentHeader() {
        if (buffer.remaining() < V1_SEGMENT_HEADER_LENGTH) {
            throw new IllegalArgumentException("Segment header is incomplete.");
        }

        int segmentNum = Short.toUnsignedInt(buffer.getShort());
        int segmentSize = (int) buffer.getLong();

        // Validate segment size BEFORE modifying state
        if (segmentSize < 0 || segmentSize > buffer.remaining()) {
            throw new IllegalArgumentException("Invalid segment size detected: " + segmentSize);
        }

        if (segmentNum != currentSegmentNumber + 1) {
            throw new IllegalArgumentException("Unexpected segment number.");
        }

        currentSegmentNumber = segmentNum;
        currentSegmentContentLength = segmentSize;
        currentSegmentContentOffset = 0;  // Reset content offset for new segment

        if (segmentSize == 0) {
            readSegmentFooter(); // Handle empty segments
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = 0;
        }

        messageOffset += V1_SEGMENT_HEADER_LENGTH;
    }

    private void readSegmentContent(ByteArrayOutputStream output, int size) {
        int toRead = Math.min(buffer.remaining(), currentSegmentContentLength - currentSegmentContentOffset);
        toRead = Math.min(toRead, size);

        if (toRead == 0)
            return;

        byte[] content = new byte[toRead];
        buffer.get(content);
        output.write(content, 0, toRead);

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = StorageCrc64Calculator.compute(content, segmentCrc64);
            messageCrc64 = StorageCrc64Calculator.compute(content, messageCrc64);
        }

        messageOffset += toRead;
        currentSegmentContentOffset += toRead;

        // ✅ Validate the actual bytes read vs expected segment size
        if (currentSegmentContentOffset > currentSegmentContentLength) {
            throw new IllegalArgumentException("Segment size mismatch detected in segment " + currentSegmentNumber);
        }

        if (currentSegmentContentOffset == currentSegmentContentLength) {
            readSegmentFooter();
        }
    }

    //    private void readSegmentFooter() {
    //        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
    //            if (buffer.remaining() < CRC64_LENGTH) {
    //                throw new IllegalArgumentException("Segment footer is incomplete.");
    //            }
    //
    //            long reportedCrc64 = buffer.getLong();
    //            if (segmentCrc64 != reportedCrc64) {
    //                throw new IllegalArgumentException("CRC64 mismatch detected in segment " + currentSegmentNumber);
    //            }
    //            segmentCrcs.put(currentSegmentNumber, segmentCrc64);
    //            messageOffset += CRC64_LENGTH;
    //        }
    //
    //        if (currentSegmentNumber == numSegments) {
    //            readMessageFooter();
    //        } else {
    //            readSegmentHeader();
    //        }
    //    }
    private void readSegmentFooter() {
        if (currentSegmentContentOffset != currentSegmentContentLength) {
            throw new IllegalArgumentException("Segment content length mismatch in segment " + currentSegmentNumber
                + ". Expected: " + currentSegmentContentLength + ", Read: " + currentSegmentContentOffset);
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            if (buffer.remaining() < CRC64_LENGTH) {
                throw new IllegalArgumentException("Segment footer is incomplete.");
            }

            long reportedCrc64 = buffer.getLong();
            if (segmentCrc64 != reportedCrc64) {
                throw new IllegalArgumentException("CRC64 mismatch detected in segment " + currentSegmentNumber);
            }
            segmentCrcs.put(currentSegmentNumber, segmentCrc64);
            messageOffset += CRC64_LENGTH;
        }

        if (currentSegmentNumber == numSegments) {
            readMessageFooter();
        } else {
            readSegmentHeader();
        }
    }

    private void readMessageFooter() {
        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            if (buffer.remaining() < CRC64_LENGTH) {
                throw new IllegalArgumentException("Message footer is incomplete.");
            }

            long reportedCrc = buffer.getLong();
            if (messageCrc64 != reportedCrc) {
                throw new IllegalArgumentException("CRC64 mismatch detected in message footer.");
            }
            messageOffset += CRC64_LENGTH;
        }

        if (messageOffset != messageLength) {
            throw new IllegalArgumentException("Decoded message length does not match expected length.");
        }
    }

    public byte[] decode(int size) {
        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();

        while (buffer.hasRemaining() && decodedContent.size() < size) {
            if (currentSegmentContentOffset == currentSegmentContentLength) {
                readSegmentHeader();
            }

            readSegmentContent(decodedContent, size - decodedContent.size());
        }

        return decodedContent.toByteArray();
    }

    public byte[] decodeFully() throws IOException {
        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();
        byte[] chunk;

        while ((chunk = decode(1024)).length > 0) {
            decodedContent.write(chunk);
        }

        return decodedContent.toByteArray();
    }
}
