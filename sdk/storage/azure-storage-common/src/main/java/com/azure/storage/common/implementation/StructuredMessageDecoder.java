package com.azure.storage.common.implementation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

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

        // Validate the actual bytes read vs expected segment size
        if (currentSegmentContentOffset > currentSegmentContentLength) {
            throw new IllegalArgumentException("Segment size mismatch detected in segment " + currentSegmentNumber);
        }

        if (currentSegmentContentOffset == currentSegmentContentLength) {
            readSegmentFooter();
        }
    }

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
