// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageCrc64Calculator;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.*;

public class StructuredMessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageDecoder.class);
    private int messageVersion;
    private long messageLength;
    private StructuredMessageFlags flags;
    private int numSegments;
    private final long expectedContentLength;


    private int messageOffset = 0;
    private int currentSegmentNumber = 0;
    private int currentSegmentContentLength = 0;
    private int currentSegmentContentOffset = 0;

    private long messageCrc64 = 0;
    private long segmentCrc64 = 0;
    private final Map<Integer, Long> segmentCrcs = new HashMap<>();

    public StructuredMessageDecoder(long expectedContentLength) {
        this.expectedContentLength = expectedContentLength;
    }

    private void readMessageHeader(ByteBuffer buffer) {
        if (buffer.remaining() < V1_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Content not long enough to contain a valid message header."));
        }

        messageVersion = Byte.toUnsignedInt(buffer.get());
        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unsupported structured message version: " + messageVersion));
        }

        messageLength = (int) buffer.getLong();
        if (messageLength < V1_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Content not long enough to contain a valid message header."));
        }
        if (messageLength != expectedContentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Structured message length " + messageLength + " did not match content length " + expectedContentLength));
        }

        flags = StructuredMessageFlags.fromValue(Short.toUnsignedInt(buffer.getShort()));
        numSegments = Short.toUnsignedInt(buffer.getShort());

        messageOffset += V1_HEADER_LENGTH;
    }

    private void readSegmentHeader(ByteBuffer buffer) {
        if (buffer.remaining() < V1_SEGMENT_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Segment header is incomplete."));
        }

        int segmentNum = Short.toUnsignedInt(buffer.getShort());
        int segmentSize = (int) buffer.getLong();

        if (segmentSize < 0 || segmentSize > buffer.remaining()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid segment size detected: " + segmentSize));
        }

        if (segmentNum != currentSegmentNumber + 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unexpected segment number."));
        }

        currentSegmentNumber = segmentNum;
        currentSegmentContentLength = segmentSize;
        currentSegmentContentOffset = 0;

        if (segmentSize == 0) {
            readSegmentFooter(buffer);
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = 0;
        }

        messageOffset += V1_SEGMENT_HEADER_LENGTH;
    }

    private void readSegmentContent(ByteBuffer buffer, ByteArrayOutputStream output, int size) {
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

        if (currentSegmentContentOffset > currentSegmentContentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Segment size mismatch detected in segment " + currentSegmentNumber));
        }

        if (currentSegmentContentOffset == currentSegmentContentLength) {
            readSegmentFooter(buffer);
        }
    }

    private void readSegmentFooter(ByteBuffer buffer) {
        if (currentSegmentContentOffset != currentSegmentContentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Segment content length mismatch in segment " + currentSegmentNumber
                + ". Expected: " + currentSegmentContentLength + ", Read: " + currentSegmentContentOffset));
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            if (buffer.remaining() < CRC64_LENGTH) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Segment footer is incomplete."));
            }

            long reportedCrc64 = buffer.getLong();
            if (segmentCrc64 != reportedCrc64) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("CRC64 mismatch detected in segment " + currentSegmentNumber));
            }
            segmentCrcs.put(currentSegmentNumber, segmentCrc64);
            messageOffset += CRC64_LENGTH;
        }

        if (currentSegmentNumber == numSegments) {
            readMessageFooter(buffer);
        } else {
            readSegmentHeader(buffer);
        }
    }

    private void readMessageFooter(ByteBuffer buffer) {
        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            if (buffer.remaining() < CRC64_LENGTH) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Message footer is incomplete."));
            }

            long reportedCrc = buffer.getLong();
            if (messageCrc64 != reportedCrc) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("CRC64 mismatch detected in message footer."));
            }
            messageOffset += CRC64_LENGTH;
        }

        if (messageOffset != messageLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Decoded message length does not match expected length."));
        }
    }

    public ByteBuffer decode(ByteBuffer buffer, int size) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();

        if (messageOffset == 0) {
            readMessageHeader(buffer);
        }

        while (buffer.hasRemaining() && decodedContent.size() < size) {
            if (currentSegmentContentOffset == currentSegmentContentLength) {
                readSegmentHeader(buffer);
            }

            readSegmentContent(buffer, decodedContent, size - decodedContent.size());
        }

        return ByteBuffer.wrap(decodedContent.toByteArray());
    }

    public ByteBuffer decode(ByteBuffer buffer) {
        return decode(buffer, buffer.remaining());
    }
}
