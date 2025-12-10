// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private byte[] generateSegmentHeader(int segmentContentSize) {
        ByteBuffer buffer = ByteBuffer.allocate(getSegmentHeaderLength()).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) currentSegmentNumber);
        buffer.putLong(segmentContentSize);

        return buffer.array();
    }

    /**
     * Encodes the given buffer into a structured message format as a stream of ByteBuffers.
     *
     * @param unencodedBuffer The buffer to be encoded.
     * @return A Flux of encoded ByteBuffers.
     * @throws IllegalArgumentException If the buffer length exceeds the content length, or the content has already been
     * encoded.
     */
    public Flux<ByteBuffer> encode(ByteBuffer unencodedBuffer) {
        StorageImplUtils.assertNotNull("unencodedBuffer", unencodedBuffer);

        if (currentContentOffset == contentLength) {
            return Flux
                .error(LOGGER.logExceptionAsError(new IllegalArgumentException("Content has already been encoded.")));
        }

        if ((unencodedBuffer.remaining() + currentContentOffset) > contentLength) {
            return Flux.error(
                LOGGER.logExceptionAsError(new IllegalArgumentException("Buffer length exceeds content length.")));
        }

        if (!unencodedBuffer.hasRemaining()) {
            return Flux.empty();
        }

        return Flux.defer(() -> {
            List<ByteBuffer> buffers = new ArrayList<>();

            // if we are at the beginning of the message, encode message header
            if (currentContentOffset == 0) {
                buffers.add(ByteBuffer.wrap(generateMessageHeader()));
            }

            while (unencodedBuffer.hasRemaining()) {
                // if we are at the beginning of a segment's content, encode segment header
                if (currentSegmentOffset == 0) {
                    incrementCurrentSegment();
                    // Calculate actual segment size based on remaining content
                    int actualSegmentSize = Math.min(segmentSize, contentLength - currentContentOffset);
                    buffers.add(ByteBuffer.wrap(generateSegmentHeader(actualSegmentSize)));
                }

                buffers.add(encodeSegmentContent(unencodedBuffer));

                // if we are at the end of a segment's content, encode segment footer
                if (currentSegmentOffset == getSegmentContentLength()) {
                    byte[] footer = generateSegmentFooter();
                    if (footer.length > 0) {
                        buffers.add(ByteBuffer.wrap(footer));
                    }
                    currentSegmentOffset = 0;
                }
            }

            // if all content has been encoded, encode message footer
            if (currentContentOffset == contentLength) {
                byte[] footer = generateMessageFooter();
                if (footer.length > 0) {
                    buffers.add(ByteBuffer.wrap(footer));
                }
            }

            return Flux.fromIterable(buffers);
        });
    }

    private byte[] generateSegmentFooter() {
        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            return ByteBuffer.allocate(CRC64_LENGTH)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(segmentCRC64s.get(currentSegmentNumber))
                .array();
        }
        return new byte[0];
    }

    private byte[] generateMessageFooter() {
        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            return ByteBuffer.allocate(CRC64_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putLong(messageCRC64).array();
        }
        return new byte[0];
    }

    private ByteBuffer encodeSegmentContent(ByteBuffer unencodedBuffer) {
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

        return ByteBuffer.wrap(content);
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

    /**
     * Returns the length of the message.
     *
     * @return The length of the message.
     */
    public long getEncodedMessageLength() {
        return messageLength;
    }
}
