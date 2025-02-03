package com.azure.storage.common;

import com.azure.storage.common.implementation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class StructuredMessageDecoder {

    /**
     * temp comment to allow building
     */
    public static final int DEFAULT_MESSAGE_VERSION = 1;
    /**
     * temp comment to allow building
     */
    public static final int V1_HEADER_LENGTH = 13;
    /**
     * temp comment to allow building
     */
    public static final int V1_SEGMENT_HEADER_LENGTH = 10;
    /**
     * temp comment to allow building
     */
    public static final int CRC64_LENGTH = 8;

    private int messageVersion;
    private final int contentLength;
    private int messageLength;
    private StructuredMessageFlags flags;
    private final int segmentSize;
    private int numSegments;

    private ByteBuffer innerBuffer;
    private int contentOffset;
    private int currentSegmentNumber;
    private SMRegion currentRegion;
    private int currentRegionLength;
    private int currentRegionOffset;
    private int checksumOffset;
    private long messageCRC64;
    private final Map<Integer, Long> segmentCRC64s;

    private enum SMRegion {
        MESSAGE_HEADER, MESSAGE_FOOTER, SEGMENT_HEADER, SEGMENT_FOOTER, SEGMENT_CONTENT,
    }

    public StructuredMessageDecoder(int contentLength, int segmentSize, StructuredMessageFlags flags) {
        if (segmentSize < 1) {
            throw new IllegalArgumentException("Segment size must be at least 1.");
        }

        this.messageVersion = DEFAULT_MESSAGE_VERSION;
        this.contentLength = contentLength;
        this.flags = flags;
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
        return (flags == StructuredMessageFlags.STORAGE_CRC64) ? CRC64_LENGTH : 0;
    }

    private int getMessageFooterLength() {
        return (flags == StructuredMessageFlags.STORAGE_CRC64) ? CRC64_LENGTH : 0;
    }

    private int calculateMessageLength() {
        int length = getMessageHeaderLength();

        length += (getSegmentHeaderLength() + getSegmentFooterLength()) * numSegments;
        length += contentLength;
        length += getMessageFooterLength();
        return length;
    }

    /**
     * temp comment to allow building
     * Calculate the current position within the message being processed.
     */
    private int tell() {
        int position;

        switch (currentRegion) {
            case MESSAGE_HEADER:
                position = currentRegionOffset;
                break;

            case SEGMENT_HEADER:
                position = getMessageHeaderLength() + contentOffset
                    + (currentSegmentNumber - 1) * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + currentRegionOffset;
                break;

            case SEGMENT_CONTENT:
                position = getMessageHeaderLength() + contentOffset
                    + (currentSegmentNumber - 1) * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + getSegmentHeaderLength();
                break;

            case SEGMENT_FOOTER:
                position = getMessageHeaderLength() + contentOffset
                    + (currentSegmentNumber - 1) * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + getSegmentHeaderLength() + currentRegionOffset;
                break;

            case MESSAGE_FOOTER:
                position = getMessageHeaderLength() + contentOffset
                    + currentSegmentNumber * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + currentRegionOffset;
                break;

            default:
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
        }
        return position;
    }


    public void decode(ByteBuffer encodedBuffer) {
        StorageImplUtils.assertNotNull("encodedBuffer", encodedBuffer);
        this.innerBuffer = encodedBuffer.order(ByteOrder.LITTLE_ENDIAN);

        while (tell() < messageLength) {
            if (currentRegion == SMRegion.SEGMENT_CONTENT) {
                decodeContent();
            } else { // MESSAGE_HEADER, MESSAGE_FOOTER, SEGMENT_HEADER, SEGMENT_FOOTER
                decodeMetadataRegion(currentRegion);
            }
        }
    }

    private void decodeMetadataRegion(SMRegion region) {
        byte[] metadata = new byte[currentRegionLength];
        innerBuffer.get(metadata, 0, currentRegionLength);

        switch (region) {
            case MESSAGE_HEADER:
                decodeMessageHeader(metadata);
                break;

            case SEGMENT_HEADER:
                decodeSegmentHeader(metadata);
                break;

            case SEGMENT_FOOTER:
                if (flags == StructuredMessageFlags.STORAGE_CRC64) {
                    long segmentCRC64 = ByteBuffer.wrap(metadata).order(ByteOrder.LITTLE_ENDIAN).getLong();
                    segmentCRC64s.put(currentSegmentNumber, segmentCRC64);
                }
                break;

            case MESSAGE_FOOTER:
                if (flags == StructuredMessageFlags.STORAGE_CRC64) {
                    messageCRC64 = ByteBuffer.wrap(metadata).order(ByteOrder.LITTLE_ENDIAN).getLong();
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid metadata SMRegion " + currentRegion);
        }

        currentRegionOffset += currentRegionLength;
        if (currentRegion != SMRegion.MESSAGE_FOOTER) {
            advanceRegion(region);
        }
    }

    private void decodeContent() {
        int readSize = Math.min(innerBuffer.remaining(), currentRegionLength - currentRegionOffset);
        byte[] content = new byte[readSize];
        innerBuffer.get(content, 0, readSize);

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCRC64s.put(currentSegmentNumber,
                StorageCrc64Calculator.compute(content, segmentCRC64s.getOrDefault(currentSegmentNumber, 0L)));
            messageCRC64 = StorageCrc64Calculator.compute(content, messageCRC64);
        }

        contentOffset += readSize;
        currentRegionOffset += readSize;
        if (currentRegionOffset == currentRegionLength) {
            advanceRegion(SMRegion.SEGMENT_CONTENT);
        }
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

    private void incrementCurrentSegment() {
        currentSegmentNumber++;
        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCRC64s.putIfAbsent(currentSegmentNumber, 0L);
        }
    }

    private void decodeMessageHeader(byte[] metadata) {
        ByteBuffer buffer = ByteBuffer.wrap(metadata).order(ByteOrder.LITTLE_ENDIAN);
        this.messageVersion = buffer.getShort();
        this.messageLength = buffer.getInt();
        this.flags = StructuredMessageFlags.fromValue(buffer.getInt());
        this.numSegments = buffer.getInt();
    }

    private void decodeSegmentHeader(byte[] metadata) {
        ByteBuffer buffer = ByteBuffer.wrap(metadata).order(ByteOrder.LITTLE_ENDIAN);
        this.currentSegmentNumber = buffer.getInt();
        int segmentSize = buffer.getInt();
    }



    /**
     * temp comment to allow building
     * @return The length of the message.
     */
    public int getMessageLength() {
        return messageLength;
    }
}
