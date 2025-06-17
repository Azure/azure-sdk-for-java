// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.CRC64_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.DEFAULT_MESSAGE_VERSION;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_HEADER_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH;

/**
 * Encoder for structured messages with support for segmenting and CRC64 checksums.
 */
public class StructuredMessageEncoder {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageEncoder.class);

    private final int messageVersion;
    private final int contentLength;
    private final int messageLength;
    private final StructuredMessageFlags structuredMessageFlags;
    private final int segmentSize;
    private final int numSegments;

    private int currentContentOffset;
    private int currentSegmentNumber;
    private int currentSegmentOffset;
    private int currentMessageLength;
    private long messageCRC64;
    private final Map<Integer, Long> segmentCRC64s;

    /**
     * Constructs a new StructuredMessageEncoder.
     * @param contentLength The length of the content to be encoded.
     * @param segmentSize The size of each segment.
     * @param structuredMessageFlags The structuredMessageFlags to be set.
     * @throws IllegalArgumentException If the segment size is less than 1, the content length is less than 1, or the
     * number of segments is greater than {@link java.lang.Short#MAX_VALUE}.
     */
    public StructuredMessageEncoder(int contentLength, int segmentSize, StructuredMessageFlags structuredMessageFlags) {
        if (segmentSize < 1) {
            StorageImplUtils.assertInBounds("segmentSize", segmentSize, 1, Long.MAX_VALUE);
        }
        if (contentLength < 1) {
            StorageImplUtils.assertInBounds("contentLength", contentLength, 1, Long.MAX_VALUE);
        }

        this.messageVersion = DEFAULT_MESSAGE_VERSION;
        this.contentLength = contentLength;
        this.structuredMessageFlags = structuredMessageFlags;
        this.segmentSize = segmentSize;
        this.numSegments = Math.max(1, (int) Math.ceil((double) this.contentLength / this.segmentSize));
        this.messageLength = calculateMessageLength();
        this.currentContentOffset = 0;
        this.currentSegmentNumber = 0;
        this.currentSegmentOffset = 0;
        this.messageCRC64 = 0;
        this.segmentCRC64s = new HashMap<>();
        this.currentMessageLength = 0;

        if (numSegments > Short.MAX_VALUE) {
            StorageImplUtils.assertInBounds("numSegments", numSegments, 1, Short.MAX_VALUE);
        }
    }

    private int getMessageHeaderLength() {
        return V1_HEADER_LENGTH;
    }

    private int getSegmentHeaderLength() {
        return V1_SEGMENT_HEADER_LENGTH;
    }

    private int getSegmentFooterLength() {
        return (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) ? CRC64_LENGTH : 0;
    }

    private int getMessageFooterLength() {
        return (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) ? CRC64_LENGTH : 0;
    }

    private int getSegmentContentLength() {
        // last segment size is remaining content
        if (currentSegmentNumber == numSegments) {
            return contentLength - ((currentSegmentNumber - 1) * segmentSize);
        } else {
            return segmentSize;
        }
    }

    private byte[] generateMessageHeader() {
        // 1 byte version, 8 byte size, 2 byte structuredMessageFlags, 2 byte numSegments
        ByteBuffer buffer = ByteBuffer.allocate(getMessageHeaderLength()).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) messageVersion);
        buffer.putLong(messageLength);
        buffer.putShort((short) structuredMessageFlags.getValue());
        buffer.putShort((short) numSegments);

        return buffer.array();
    }

    private byte[] generateSegmentHeader() {
        int segmentHeaderSize = Math.min(segmentSize, contentLength - currentContentOffset);
        // 2 byte number, 8 byte size
        ByteBuffer buffer = ByteBuffer.allocate(getSegmentHeaderLength()).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) currentSegmentNumber);
        buffer.putLong(segmentHeaderSize);

        return buffer.array();
    }

    /**
     * Encodes the given buffer into a structured message format.
     *
     * @param unencodedBuffer The buffer to be encoded.
     * @return The encoded buffer.
     * @throws IllegalArgumentException If the buffer length exceeds the content length, or the content has already been
     * encoded.
     */
    public ByteBuffer encode(ByteBuffer unencodedBuffer) {
        StorageImplUtils.assertNotNull("unencodedBuffer", unencodedBuffer);

        if (currentContentOffset == contentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Content has already been encoded."));
        }

        if ((unencodedBuffer.remaining() + currentContentOffset) > contentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Buffer length exceeds content length."));
        }

        if (!unencodedBuffer.hasRemaining()) {
            return ByteBuffer.allocate(0);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // if we are at the beginning of the message, encode message header
        if (currentMessageLength == 0) {
            encodeMessageHeader(byteArrayOutputStream);
        }

        while (unencodedBuffer.hasRemaining()) {
            // if we are at the beginning of a segment's content, encode segment header
            if (currentSegmentOffset == 0) {
                encodeSegmentHeader(byteArrayOutputStream);
            }

            encodeSegmentContent(unencodedBuffer, byteArrayOutputStream);

            // if we are at the end of a segment's content, encode segment footer
            if (currentSegmentOffset == getSegmentContentLength()) {
                encodeSegmentFooter(byteArrayOutputStream);
            }
        }

        // if all content has been encoded, encode message footer
        if (currentContentOffset == contentLength) {
            encodeMessageFooter(byteArrayOutputStream);
        }

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

    private void encodeMessageHeader(ByteArrayOutputStream output) {
        byte[] metadata = generateMessageHeader();
        output.write(metadata, 0, metadata.length);

        currentMessageLength += metadata.length;
    }

    private void encodeSegmentHeader(ByteArrayOutputStream output) {
        incrementCurrentSegment();
        byte[] metadata = generateSegmentHeader();
        output.write(metadata, 0, metadata.length);

        currentMessageLength += metadata.length;
    }

    private void encodeSegmentFooter(ByteArrayOutputStream output) {
        byte[] metadata;
        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            metadata = ByteBuffer.allocate(CRC64_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(segmentCRC64s.get(currentSegmentNumber))
                .array();
        } else {
            metadata = new byte[0];
        }
        output.write(metadata, 0, metadata.length);

        currentMessageLength += metadata.length;
        currentSegmentOffset = 0;
    }

    private void encodeMessageFooter(ByteArrayOutputStream output) {
        byte[] metadata;
        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            metadata = ByteBuffer.allocate(CRC64_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putLong(messageCRC64).array();
        } else {
            metadata = new byte[0];
        }

        output.write(metadata, 0, metadata.length);
        currentMessageLength += metadata.length;
    }

    private void encodeSegmentContent(ByteBuffer unencodedBuffer, ByteArrayOutputStream output) {
        int readSize = Math.min(unencodedBuffer.remaining(), getSegmentContentLength() - currentSegmentOffset);

        byte[] content = new byte[readSize];
        unencodedBuffer.get(content, 0, readSize);

        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCRC64s.put(currentSegmentNumber,
                StorageCrc64Calculator.compute(content, segmentCRC64s.get(currentSegmentNumber)));
            messageCRC64 = StorageCrc64Calculator.compute(content, messageCRC64);
        }

        currentContentOffset += readSize;
        currentSegmentOffset += readSize;

        output.write(content, 0, content.length);
        currentMessageLength += readSize;
    }

    private int calculateMessageLength() {
        int length = getMessageHeaderLength();

        length += (getSegmentHeaderLength() + getSegmentFooterLength()) * numSegments;
        length += contentLength;
        length += getMessageFooterLength();
        return length;
    }

    private void incrementCurrentSegment() {
        currentSegmentNumber++;
        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCRC64s.putIfAbsent(currentSegmentNumber, 0L);
        }
    }
}
