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

import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.CRC64_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.DEFAULT_MESSAGE_VERSION;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_HEADER_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH;

/**
 * Decoder for structured messages with support for segmenting and CRC64 checksums.
 */
public class StructuredMessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageDecoder.class);
    private long messageLength;
    private StructuredMessageFlags flags;
    private int numSegments;
    private final long expectedContentLength;

    private long messageOffset = 0;
    private int currentSegmentNumber = 0;
    private long currentSegmentContentLength = 0;
    private long currentSegmentContentOffset = 0;

    private long messageCrc64 = 0;
    private long segmentCrc64 = 0;
    private final Map<Integer, Long> segmentCrcs = new HashMap<>();

    // Track the last complete segment boundary for smart retry
    private long lastCompleteSegmentStart = 0;
    private long currentSegmentStart = 0;

    /**
     * Constructs a new StructuredMessageDecoder.
     *
     * @param expectedContentLength The expected length of the content to be decoded.
     */
    public StructuredMessageDecoder(long expectedContentLength) {
        this.expectedContentLength = expectedContentLength;
    }

    /**
     * Gets the byte offset where the last complete segment ended.
     * This is used for smart retry to resume from a segment boundary.
     *
     * @return The byte offset of the last complete segment boundary.
     */
    public long getLastCompleteSegmentStart() {
        return lastCompleteSegmentStart;
    }

    /**
     * Gets the current message offset (total bytes consumed from the structured message).
     *
     * @return The current message offset.
     */
    public long getMessageOffset() {
        return messageOffset;
    }

    /**
     * Resets the decoder position to the last complete segment boundary.
     * This is used during smart retry to ensure the decoder is in sync with
     * the data being provided from the retry offset.
     */
    public void resetToLastCompleteSegment() {
        if (messageOffset != lastCompleteSegmentStart) {
            LOGGER.atInfo()
                .addKeyValue("fromOffset", messageOffset)
                .addKeyValue("toOffset", lastCompleteSegmentStart)
                .addKeyValue("currentSegmentNum", currentSegmentNumber)
                .addKeyValue("currentSegmentContentOffset", currentSegmentContentOffset)
                .addKeyValue("currentSegmentContentLength", currentSegmentContentLength)
                .log("Resetting decoder to last complete segment boundary");
            messageOffset = lastCompleteSegmentStart;
            // Reset current segment state - next decode will read the segment header
            currentSegmentContentOffset = 0;
            currentSegmentContentLength = 0;
        } else {
            LOGGER.atVerbose()
                .addKeyValue("offset", messageOffset)
                .log("Decoder already at last complete segment boundary, no reset needed");
        }
    }

    /**
     * Reads the message header from the given buffer.
     *
     * @param buffer The buffer containing the message header.
     * @throws IllegalArgumentException if the buffer does not contain a valid message header.
     */
    private void readMessageHeader(ByteBuffer buffer) {
        if (buffer.remaining() < V1_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Content not long enough to contain a valid " + "message header."));
        }

        int messageVersion = Byte.toUnsignedInt(buffer.get());
        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Unsupported structured message version: " + messageVersion));
        }

        messageLength = (int) buffer.getLong();
        if (messageLength < V1_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Content not long enough to contain a valid " + "message header."));
        }
        if (messageLength != expectedContentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Structured message length " + messageLength
                + " did not match content length " + expectedContentLength));
        }

        flags = StructuredMessageFlags.fromValue(Short.toUnsignedInt(buffer.getShort()));
        numSegments = Short.toUnsignedInt(buffer.getShort());

        messageOffset += V1_HEADER_LENGTH;
    }

    /**
     * Converts a ByteBuffer range to hex string for diagnostic purposes.
     */
    private static String toHex(ByteBuffer buf, int len) {
        int pos = buf.position();
        int peek = Math.min(len, buf.remaining());
        byte[] out = new byte[peek];
        buf.get(out, 0, peek);
        buf.position(pos);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < out.length; i++) {
            sb.append(String.format("%02X", out[i]));
            if (i < out.length - 1)
                sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Reads and validates segment length with diagnostic logging.
     */
    private long readAndValidateSegmentLength(ByteBuffer buffer, long remaining) {
        final int SEGMENT_SIZE_BYTES = 8; // segment size is 8 bytes (long)
        if (buffer.remaining() < SEGMENT_SIZE_BYTES) {
            LOGGER.error("Not enough bytes to read segment size. pos={}, remaining={}", buffer.position(),
                buffer.remaining());
            throw new IllegalStateException("Not enough bytes to read segment size");
        }

        // Diagnostic: dump first 16 bytes at this position so we can see what's being read
        LOGGER.atInfo()
            .addKeyValue("decoderOffset", messageOffset)
            .addKeyValue("bufferPos", buffer.position())
            .addKeyValue("bufferRemaining", buffer.remaining())
            .addKeyValue("peek16", toHex(buffer, 16))
            .addKeyValue("lastCompleteSegment", lastCompleteSegmentStart)
            .log("Decoder about to read segment length");

        long segmentLength = buffer.getLong();

        if (segmentLength < 0 || segmentLength > remaining) {
            // Peek next bytes for extra detail
            String peekNext = toHex(buffer, 16);
            LOGGER.error(
                "Invalid segment length read: segmentLength={}, remaining={}, decoderOffset={}, "
                    + "lastCompleteSegment={}, bufferPos={}, peek-next-bytes={}",
                segmentLength, remaining, messageOffset, lastCompleteSegmentStart, buffer.position(), peekNext);
            throw new IllegalArgumentException("Invalid segment size detected: " + segmentLength + " (remaining="
                + remaining + ", decoderOffset=" + messageOffset + ")");
        }

        LOGGER.atVerbose()
            .addKeyValue("segmentLength", segmentLength)
            .addKeyValue("decoderOffset", messageOffset)
            .log("Valid segment length read");

        return segmentLength;
    }

    /**
     * Reads the segment header from the given buffer.
     *
     * @param buffer The buffer containing the segment header.
     * @throws IllegalArgumentException if the buffer does not contain a valid segment header.
     */
    private void readSegmentHeader(ByteBuffer buffer) {
        if (buffer.remaining() < V1_SEGMENT_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Segment header is incomplete."));
        }

        // Mark the start of this segment (before reading the header)
        currentSegmentStart = messageOffset;

        int segmentNum = Short.toUnsignedInt(buffer.getShort());

        // Read segment size with validation and diagnostics
        long segmentSize = readAndValidateSegmentLength(buffer, buffer.remaining());

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

    /**
     * Reads the segment content from the given buffer and writes it to the output stream.
     *
     * @param buffer The buffer containing the segment content.
     * @param output The output stream to write the segment content to.
     * @param size The maximum number of bytes to read.
     * @throws IllegalArgumentException if there is a segment size mismatch.
     */
    private void readSegmentContent(ByteBuffer buffer, ByteArrayOutputStream output, int size) {
        long remaining = currentSegmentContentLength - currentSegmentContentOffset;
        int toRead = (int) Math.min(buffer.remaining(), Math.min(remaining, size));

        if (toRead == 0) {
            return;
        }

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
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Segment size mismatch detected in segment " + currentSegmentNumber));
        }

        if (currentSegmentContentOffset == currentSegmentContentLength) {
            readSegmentFooter(buffer);
        }
    }

    /**
     * Reads the segment footer from the given buffer.
     *
     * @param buffer The buffer containing the segment footer.
     * @throws IllegalArgumentException if the buffer does not contain a valid segment footer.
     */
    private void readSegmentFooter(ByteBuffer buffer) {
        if (currentSegmentContentOffset != currentSegmentContentLength) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Segment content length mismatch in segment " + currentSegmentNumber
                    + ". Expected: " + currentSegmentContentLength + ", Read: " + currentSegmentContentOffset));
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            if (buffer.remaining() < CRC64_LENGTH) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Segment footer is incomplete."));
            }

            long reportedCrc64 = buffer.getLong();
            if (segmentCrc64 != reportedCrc64) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("CRC64 mismatch detected in segment " + currentSegmentNumber));
            }
            segmentCrcs.put(currentSegmentNumber, segmentCrc64);
            messageOffset += CRC64_LENGTH;
        }

        // Mark that this segment is complete - update the last complete segment boundary
        // This is the position where we can safely resume if a retry occurs
        lastCompleteSegmentStart = messageOffset;
        LOGGER.atInfo()
            .addKeyValue("segmentNum", currentSegmentNumber)
            .addKeyValue("offset", lastCompleteSegmentStart)
            .addKeyValue("segmentLength", currentSegmentContentLength)
            .log("Segment complete at byte offset");

        if (currentSegmentNumber == numSegments) {
            readMessageFooter(buffer);
        }
    }

    /**
     * Reads the segment footer from the given buffer.
     *
     * @param buffer The buffer containing the segment footer.
     * @throws IllegalArgumentException if the buffer does not contain a valid segment footer.
     */
    private void readMessageFooter(ByteBuffer buffer) {
        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            if (buffer.remaining() < CRC64_LENGTH) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Message footer is incomplete."));
            }

            long reportedCrc = buffer.getLong();
            if (messageCrc64 != reportedCrc) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("CRC64 mismatch detected in message " + "footer."));
            }
            messageOffset += CRC64_LENGTH;
        }

        if (messageOffset != messageLength) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Decoded message length does not match " + "expected length."));
        }
    }

    /**
     * Decodes the structured message from the given buffer up to the specified size.
     *
     * @param buffer The buffer containing the structured message.
     * @param size The maximum number of bytes to decode.
     * @return A ByteBuffer containing the decoded message content.
     * @throws IllegalArgumentException if the buffer does not contain a valid structured message.
     */
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

    /**
     * Decodes the entire structured message from the given buffer.
     *
     * @param buffer The buffer containing the structured message.
     * @return A ByteBuffer containing the decoded message content.
     * @throws IllegalArgumentException if the buffer does not contain a valid structured message.
     */
    public ByteBuffer decode(ByteBuffer buffer) {
        return decode(buffer, buffer.remaining());
    }

    /**
     * Finalizes the decoding process and validates that the entire message has been decoded.
     *
     * @throws IllegalArgumentException if the decoded message length does not match the expected length.
     */
    public void finalizeDecoding() {
        if (messageOffset != messageLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Decoded message length does not match "
                + "expected length. Expected: " + messageLength + ", but was: " + messageOffset));
        }
    }
}
