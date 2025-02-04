// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.storage.common.implementation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * temp comment to allow building
 */
public class StructuredMessageEncoder {
    /**
     * temp comment to allow building
     */
    private static final int DEFAULT_MESSAGE_VERSION = 1;
    /**
     * temp comment to allow building
     */
    private static final int V1_HEADER_LENGTH = 13;
    /**
     * temp comment to allow building
     */
    private static final int V1_SEGMENT_HEADER_LENGTH = 10;
    /**
     * temp comment to allow building
     */
    private static final int CRC64_LENGTH = 8;

    private final int messageVersion;
    private final int contentLength;
    private final int messageLength;
    private final StructuredMessageFlags structuredMessageFlags;
    private final int segmentSize;
    private final int numSegments;

    private int contentOffset;
    private int currentSegmentNumber;
    private SMRegion currentRegion;
    private int currentRegionLength;
    private int currentRegionOffset;
    private int checksumOffset;
    private long messageCRC64;
    private final Map<Integer, Long> segmentCRC64s;
    private int currentEncodedDataLength;

    private enum SMRegion {
        MESSAGE_HEADER, MESSAGE_FOOTER, SEGMENT_HEADER, SEGMENT_FOOTER, SEGMENT_CONTENT,
    }

    /**
     * temp comment to allow building
     * @param contentLength The length of the content to be encoded.
     * @param segmentSize The size of each segment.
     * @param structuredMessageFlags The structuredMessageFlags to be set.
     */
    public StructuredMessageEncoder(int contentLength, int segmentSize, StructuredMessageFlags structuredMessageFlags) {
        if (segmentSize < 1) {
            throw new IllegalArgumentException("Segment size must be at least 1.");
        }

        this.messageVersion = DEFAULT_MESSAGE_VERSION;
        this.contentLength = contentLength;
        this.structuredMessageFlags = structuredMessageFlags;
        this.segmentSize = segmentSize;
        this.numSegments = Math.max(1, (int) Math.ceil((double) this.contentLength / this.segmentSize));
        this.messageLength = calculateMessageLength();
        this.contentOffset = 0;
        this.currentSegmentNumber = 0;
        this.currentRegion = SMRegion.MESSAGE_HEADER;
        this.currentRegionLength = getMessageHeaderLength();
        this.currentRegionOffset = 0;
        this.checksumOffset = 0;
        this.messageCRC64 = 0;
        this.segmentCRC64s = new HashMap<>();
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
        int segmentHeaderSize = Math.min(segmentSize, contentLength - contentOffset);
        // 2 byte number, 8 byte size
        ByteBuffer buffer = ByteBuffer.allocate(getSegmentHeaderLength()).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) currentSegmentNumber);
        buffer.putLong(segmentHeaderSize);

        return buffer.array();
    }

    private void updateCurrentRegionLength() {
        switch (currentRegion) {
            case MESSAGE_HEADER:
                currentRegionLength = getMessageHeaderLength();
                break;

            case SEGMENT_HEADER:
                currentRegionLength = getSegmentHeaderLength();
                break;

            case SEGMENT_CONTENT:
                // last segment size is remaining content
                if (currentSegmentNumber == numSegments) {
                    currentRegionLength = contentLength - ((currentSegmentNumber - 1) * segmentSize);
                } else {
                    currentRegionLength = segmentSize;
                }
                break;

            case SEGMENT_FOOTER:
                currentRegionLength = getSegmentFooterLength();
                break;

            case MESSAGE_FOOTER:
                currentRegionLength = getMessageFooterLength();
                break;

            default:
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
        }
    }

    /**
     * temp comment to allow building
     * @param unencodedBuffer The buffer to be encoded.
     * @return The encoded buffer.
     * @throws IOException If an error occurs while encoding the buffer.
     */
    public ByteBuffer encode(ByteBuffer unencodedBuffer) throws IOException {
        StorageImplUtils.assertNotNull("unencodedBuffer", unencodedBuffer);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (currentEncodedDataLength < messageLength) {
            if (currentRegion == SMRegion.SEGMENT_CONTENT) {
                encodeContent(unencodedBuffer, byteArrayOutputStream);
                if (currentRegion != SMRegion.SEGMENT_FOOTER && !unencodedBuffer.hasRemaining()) {
                    break;
                }
            } else { // MESSAGE_HEADER, MESSAGE_FOOTER, SEGMENT_HEADER, SEGMENT_FOOTER
                encodeMetadataRegion(currentRegion, byteArrayOutputStream);
            }
        }

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

    private void encodeMetadataRegion(SMRegion region, ByteArrayOutputStream output) {
        byte[] metadata = getMetadataRegion(region);

        int readSize = currentRegionLength;

        output.write(metadata, 0, readSize);

        currentRegionOffset += readSize;
        currentEncodedDataLength += readSize;
        // If we have read all the metadata for this region, advance to the next region
        if (currentRegion != SMRegion.MESSAGE_FOOTER) {
            advanceRegion(region);
        }
    }

    private void encodeContent(ByteBuffer unencodedBuffer, ByteArrayOutputStream output) throws IOException {
        int tempChecksumOffset = checksumOffset - contentOffset;

        int readSize = Math.min(unencodedBuffer.remaining(), currentRegionLength - currentRegionOffset);

        if (tempChecksumOffset != 0) {
            readSize = Math.min(readSize, tempChecksumOffset);
        }

        byte[] content = new byte[readSize];
        unencodedBuffer.get(content, 0, readSize);
        output.write(content);

        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            if (tempChecksumOffset == 0) {
                segmentCRC64s.put(currentSegmentNumber,
                    StorageCrc64Calculator.compute(content, segmentCRC64s.get(currentSegmentNumber)));
                messageCRC64 = StorageCrc64Calculator.compute(content, messageCRC64);
            }
        }

        contentOffset += readSize;
        if (contentOffset > checksumOffset) {
            checksumOffset += readSize;
        }

        currentRegionOffset += readSize;
        currentEncodedDataLength += readSize;
        if (currentRegionOffset == currentRegionLength) {
            advanceRegion(SMRegion.SEGMENT_CONTENT);
        }
    }

    private int calculateMessageLength() {
        int length = getMessageHeaderLength();

        length += (getSegmentHeaderLength() + getSegmentFooterLength()) * numSegments;
        length += contentLength;
        length += getMessageFooterLength();
        return length;
    }

    private byte[] getMetadataRegion(SMRegion region) {
        byte[] metadata;
        switch (region) {
            case MESSAGE_HEADER:
                metadata = generateMessageHeader();
                break;

            case SEGMENT_HEADER:
                metadata = generateSegmentHeader();
                break;

            case SEGMENT_FOOTER:
                if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
                    metadata = ByteBuffer.allocate(CRC64_LENGTH)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(segmentCRC64s.get(currentSegmentNumber))
                        .array();
                } else {
                    metadata = new byte[0];
                }
                break;

            case MESSAGE_FOOTER:
                if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
                    metadata = ByteBuffer.allocate(CRC64_LENGTH)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(messageCRC64)
                        .array();
                } else {
                    metadata = new byte[0];
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid metadata SMRegion " + currentRegion);
        }

        return metadata;
    }

    private void advanceRegion(SMRegion current) {
        currentRegionOffset = 0;
        switch (current) {
            case MESSAGE_HEADER:
                currentRegion = SMRegion.SEGMENT_HEADER;
                incrementCurrentSegment();
                break;

            case SEGMENT_HEADER:
                currentRegion = SMRegion.SEGMENT_CONTENT;
                break;

            case SEGMENT_CONTENT:
                currentRegion = SMRegion.SEGMENT_FOOTER;
                break;

            case SEGMENT_FOOTER:
                if (contentOffset == contentLength) {
                    currentRegion = SMRegion.MESSAGE_FOOTER;
                } else {
                    currentRegion = SMRegion.SEGMENT_HEADER;
                    incrementCurrentSegment();
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
        }

        updateCurrentRegionLength();
    }

    private void incrementCurrentSegment() {
        currentSegmentNumber++;
        if (structuredMessageFlags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCRC64s.putIfAbsent(currentSegmentNumber, 0L);
        }
    }

    /**
     * temp comment to allow building
     * @return The length of the message.
     */
    public int getMessageLength() {
        return messageLength;
    }
}
