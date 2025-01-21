// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class StructuredMessageEncoder {
    public static final int DEFAULT_MESSAGE_VERSION = 1;
    public static final int V1_HEADER_LENGTH = 13;
    public static final int V1_SEGMENT_HEADER_LENGTH = 10;
    public static final int CRC64_LENGTH = 8;

    private int messageVersion;
    private int contentLength;
    private int messageLength;
    private Flags flags;
    private ByteBuffer innerBuffer;
    private int segmentSize;
    private int numSegments;

    private int contentOffset;
    private int currentSegmentNumber;
    private SMRegion currentRegion;
    private int currentRegionLength;
    private int currentRegionOffset;
    private int checksumOffset;
    private long messageCRC64;
    private Map<Integer, Long> segmentCRC64s;

    private enum SMRegion {
        MESSAGE_HEADER, MESSAGE_FOOTER, SEGMENT_HEADER, SEGMENT_FOOTER, SEGMENT_CONTENT,
    }

    public StructuredMessageEncoder(ByteBuffer innerBuffer, int contentLength, int segmentSize, Flags flags) {
        StorageImplUtils.assertNotNull("innerBuffer", innerBuffer);
        if (segmentSize < 1) { //python says at least 1, .net says at least 2?
            throw new IllegalArgumentException("Segment size must be at least 2.");
        }

        this.messageVersion = DEFAULT_MESSAGE_VERSION;
        this.contentLength = contentLength;
        this.flags = flags;
        this.innerBuffer = innerBuffer;
        this.segmentSize = segmentSize;
        this.numSegments = (int) Math.ceil((double) this.contentLength / this.segmentSize);
        if (this.numSegments == 0) {
            this.numSegments = 1;
        }
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
        return (flags == Flags.STORAGE_CRC64) ? CRC64_LENGTH : 0;
    }

    private int getMessageFooterLength() {
        return (flags == Flags.STORAGE_CRC64) ? CRC64_LENGTH : 0;
    }

    private byte[] generateMessageHeader(int version, int size, Flags flags, int numSegments) {
        // 1 byte version, 8 byte size, 2 byte flags, 2 byte numSegments
        ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) version);
        buffer.putLong(size);
        buffer.putShort((short) flags.getValue());
        buffer.putShort((short) numSegments);

        return buffer.array();
    }

    private byte[] generateSegmentHeader(int number, int size) {
        // 2 byte number, 8 byte size
        ByteBuffer buffer = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) number);
        buffer.putLong(size);

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

    public ByteBuffer encode(int size) throws IOException {
        if (size == 0) {
            return ByteBuffer.allocate(0);
        }
        if (size < 0) {
            size = Integer.MAX_VALUE;
        }

        int count = 0;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (count < size && tell() < messageLength) {
            int remaining = size - count;
            //If we are in a metadata region, encode the metadata
            if (currentRegion == SMRegion.MESSAGE_HEADER
                || currentRegion == SMRegion.SEGMENT_HEADER
                || currentRegion == SMRegion.SEGMENT_FOOTER
                || currentRegion == SMRegion.MESSAGE_FOOTER) {
                count += encodeMetadataRegion(currentRegion, remaining, byteArrayOutputStream);
                //If we are in the content region, encode the content
            } else if (currentRegion == SMRegion.SEGMENT_CONTENT) {
                count += encodeContent(remaining, byteArrayOutputStream);
            } else {
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
            }
        }
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
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
                metadata = generateMessageHeader(messageVersion, messageLength, flags, numSegments);
                break;

            case SEGMENT_HEADER:
                int segmentSize = Math.min(this.segmentSize, contentLength - contentOffset);
                metadata = generateSegmentHeader(currentSegmentNumber, segmentSize);
                break;

            case SEGMENT_FOOTER:
                if (flags == Flags.STORAGE_CRC64) {
                    metadata = ByteBuffer.allocate(CRC64_LENGTH)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(this.segmentCRC64s.get(this.currentSegmentNumber))
                        .array();
                } else {
                    metadata = new byte[0];
                }
                break;

            case MESSAGE_FOOTER:
                if (flags == Flags.STORAGE_CRC64) {
                    metadata = ByteBuffer.allocate(CRC64_LENGTH)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(this.messageCRC64)
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

    private int encodeMetadataRegion(SMRegion region, int size, ByteArrayOutputStream output) {
        byte[] metadata = getMetadataRegion(region);

        int readSize = Math.min(size, this.currentRegionLength - this.currentRegionOffset);

        output.write(metadata, this.currentRegionOffset, readSize);

        this.currentRegionOffset += readSize;
        // If we have read all the metadata for this region, advance to the next region
        if (this.currentRegionOffset == this.currentRegionLength && this.currentRegion != SMRegion.MESSAGE_FOOTER) {
            advanceRegion(region);
        }
        return readSize;
    }

    private int encodeContent(int size, ByteArrayOutputStream output) throws IOException {
        int tempChecksumOffset = checksumOffset - contentOffset;

        int readSize = Math.min(size, this.currentRegionLength - this.currentRegionOffset);

        if (tempChecksumOffset != 0) {
            readSize = Math.min(readSize, tempChecksumOffset);
        }

        byte[] content = new byte[readSize];
        this.innerBuffer.get(content, 0, readSize);
        output.write(content);

        if (flags == Flags.STORAGE_CRC64) {
            if (tempChecksumOffset == 0) {
                this.segmentCRC64s.put(this.currentSegmentNumber,
                    StorageCrc64Calculator.compute(content, this.segmentCRC64s.get(this.currentSegmentNumber)));
                this.messageCRC64 = StorageCrc64Calculator.compute(content, this.messageCRC64);
            }
        }

        this.contentOffset += readSize;
        if (contentOffset > checksumOffset) {
            checksumOffset += readSize;
        }

        this.currentRegionOffset += readSize;
        if (this.currentRegionOffset == this.currentRegionLength) {
            advanceRegion(SMRegion.SEGMENT_CONTENT);
        }

        return readSize;
    }

    private void incrementCurrentSegment() {
        currentSegmentNumber++;
        if (flags == Flags.STORAGE_CRC64) {
            segmentCRC64s.putIfAbsent(currentSegmentNumber, 0L);
        }
    }

    public int getMessageLength() {
        return messageLength;
    }

    //temporary for testing:
    public BigInteger getUnsignedCRC64() {
        return new BigInteger(Long.toUnsignedString(messageCRC64));
    }

    // Untested
    //
    //    private int seek(int offset, SeekOrigin whence) {
    //        System.out.println("Entering seek method");
    //        System.out.println("Offset: " + offset);
    //        System.out.println("Whence: " + whence);
    //
    //        int position;
    //        switch (whence) {
    //            case BEGIN:
    //                position = offset;
    //                break;
    //
    //            case CURRENT:
    //                position = tell() + offset;
    //                break;
    //
    //            case END:
    //                position = messageLength + offset;
    //                break;
    //
    //            default:
    //                throw new IllegalArgumentException("Invalid value for whence: " + whence);
    //        }
    //
    //        if (position > tell()) {
    //            throw new UnsupportedOperationException("This stream only supports seeking backwards.");
    //        }
    //
    //        if (position < getMessageHeaderLength()) { //message header
    //            currentRegion = SMRegion.MESSAGE_HEADER;
    //            currentRegionOffset = position;
    //            contentOffset = 0;
    //            currentSegmentNumber = 0;
    //        } else if (position >= messageLength - getMessageFooterLength()) { //message footer
    //            currentRegion = SMRegion.MESSAGE_FOOTER;
    //            currentRegionOffset = position - (messageLength - getMessageFooterLength());
    //            contentOffset = contentLength;
    //            currentSegmentNumber = numSegments;
    //        } else {
    //            // The size of a "full" segment. Fine to use for calculating new segment number and pos
    //            int fullSegmentSize = getSegmentHeaderLength() + segmentSize + getSegmentFooterLength();
    //            int newSegmentNum = 1 + (position - getMessageHeaderLength()) / fullSegmentSize;
    //            int segmentPos = (position - getMessageHeaderLength()) % fullSegmentSize;
    //            int previousSegmentsTotalContentSize = (newSegmentNum - 1) * segmentSize;
    //
    //            // We need the size of the segment we are seeking to for some of the calculations below
    //            int newSegmentSize = segmentSize;
    //            if (newSegmentNum == numSegments) {
    //                // The last segment size is the remaining content length
    //                newSegmentSize = contentLength - previousSegmentsTotalContentSize;
    //            }
    //
    //            if (segmentPos < getSegmentHeaderLength()) { // segment header
    //                currentRegion = SMRegion.SEGMENT_HEADER;
    //                currentRegionOffset = segmentPos;
    //                contentOffset = previousSegmentsTotalContentSize;
    //            } else if (segmentPos < getSegmentHeaderLength() + newSegmentSize) { //segment content
    //                currentRegion = SMRegion.SEGMENT_CONTENT;
    //                currentRegionOffset = segmentPos - getSegmentHeaderLength();
    //                contentOffset = previousSegmentsTotalContentSize + currentRegionOffset;
    //            } else { //segment footer
    //                currentRegion = SMRegion.SEGMENT_FOOTER;
    //                currentRegionOffset = segmentPos - getSegmentHeaderLength() - newSegmentSize;
    //                contentOffset = previousSegmentsTotalContentSize + newSegmentSize;
    //            }
    //
    //            currentSegmentNumber = newSegmentNum;
    //        }
    //
    //        update_current_region_length();
    //        innerBuffer.position(contentOffset);
    //
    //        System.out.println("New position: " + position);
    //        System.out.println("Exiting seek method");
    //        return position;
    //    }
}
