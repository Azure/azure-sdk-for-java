// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StructuredMessageEncoder {
    public static final int DEFAULT_SEGMENT_CONTENT_LENGTH = 4 * 1024 * 1024; //default segment size
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

    private int initial_content_position;
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

    public enum SeekOrigin {
        BEGIN, CURRENT, END
    }

    private byte[] generate_message_header(int version, int size, Flags flags, int numSegments) {
        System.out.println("Entering generate_message_header method");
        System.out.println("Version: " + version);
        System.out.println("Size: " + size);
        System.out.println("Flags: " + flags);
        System.out.println("Number of segments: " + numSegments);

        ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN); //1 + 8 + 2 + 2
        buffer.put((byte) version);
        buffer.putLong(size);
        buffer.putShort((short) flags.getValue());
        buffer.putShort((short) numSegments);

        System.out.println("Exiting generate_message_header method");
        return buffer.array();
    }

    private byte[] generate_segment_header(int number, int size) {
        System.out.println("Entering generate_segment_header method");
        System.out.println("Segment number: " + number);
        System.out.println("Segment size: " + size);

        ByteBuffer buffer = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN); //2 + 8
        buffer.putShort((short) number);
        buffer.putLong(size);

        System.out.println("Exiting generate_segment_header method");
        return buffer.array();
    }

    public StructuredMessageEncoder(ByteBuffer innerBuffer, int contentLength, int segmentSize, Flags flags) {
        System.out.println("Entering StructuredMessageEncoder constructor");
        System.out.println("Content length: " + contentLength);
        System.out.println("Segment size: " + segmentSize);
        System.out.println("Flags: " + flags);

        StorageImplUtils.assertNotNull("innerBuffer", innerBuffer);
        if (segmentSize < 1) { //python says at least 1, .net says at least 2?
            throw new IllegalArgumentException("Segment size must be at least 2.");
        }

        this.messageVersion = DEFAULT_MESSAGE_VERSION;
        this.contentLength = contentLength;
        this.flags = flags;
        this.innerBuffer = innerBuffer;
        this.segmentSize = segmentSize; //DEFAULT_SEGMENT_SIZE
        this.numSegments = (int) Math.ceil((double) this.contentLength / this.segmentSize);
        if (this.numSegments == 0) {
            this.numSegments = 1;
        }
        this.messageLength = calculate_message_length();
        this.contentOffset = 0;
        this.currentSegmentNumber = 0;
        this.currentRegion = SMRegion.MESSAGE_HEADER;
        this.currentRegionLength = getMessageHeaderLength();
        this.currentRegionOffset = 0;
        this.checksumOffset = 0;
        this.messageCRC64 = 0;
        this.segmentCRC64s = new HashMap<>();

        System.out.println("messageVersion: " + this.messageVersion);
        System.out.println("contentLength: " + this.contentLength);
        System.out.println("flags: " + this.flags);
        //System.out.println("innerBuffer: " + this.innerBuffer);
        System.out.println("segmentSize: " + this.segmentSize);
        System.out.println("numSegments: " + this.numSegments);
        System.out.println("messageLength: " + this.messageLength);
        System.out.println("contentOffset: " + this.contentOffset);
        System.out.println("currentSegmentNumber: " + this.currentSegmentNumber);
        System.out.println("currentRegion: SMRegion." + this.currentRegion);
        System.out.println("currentRegionLength: " + this.currentRegionLength);
        System.out.println("currentRegionOffset: " + this.currentRegionOffset);
        System.out.println("checksumOffset: " + this.checksumOffset);
        System.out.println("messageCRC64: " + this.messageCRC64);
        System.out.println("segmentCRC64s: " + this.segmentCRC64s);

        System.out.println("Exiting StructuredMessageEncoder constructor");
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

    private void update_current_region_length() {
        System.out.println("Entering update_current_region_length method");
        System.out.println("Current region: SMRegion." + currentRegion);
        System.out.println("Current segment number: " + currentSegmentNumber);
        System.out.println("Total number of segments: " + numSegments);
        System.out.println("Content length: " + contentLength);
        System.out.println("Segment size: " + segmentSize);

        switch (currentRegion) {
            case MESSAGE_HEADER:
                System.out.println("In MESSAGE_HEADER region");
                currentRegionLength = getMessageHeaderLength();
                System.out.println("Set current region length to message header length: " + currentRegionLength);
                break;

            case SEGMENT_HEADER:
                System.out.println("In SEGMENT_HEADER region");
                currentRegionLength = getSegmentHeaderLength();
                System.out.println("Set current region length to segment header length: " + currentRegionLength);
                break;

            case SEGMENT_CONTENT:
                System.out.println("In SEGMENT_CONTENT region");
                // last segment size is remaining content
                if (currentSegmentNumber == numSegments) {
                    System.out.println("In last segment");
                    currentRegionLength = contentLength - ((currentSegmentNumber - 1) * segmentSize);
                    System.out.println("Set current region length to remaining content length: " + currentRegionLength);
                } else {
                    System.out.println("In intermediate segment");
                    currentRegionLength = segmentSize;
                    System.out.println("Set current region length to segment size: " + currentRegionLength);
                }
                break;

            case SEGMENT_FOOTER:
                System.out.println("In SEGMENT_FOOTER region");
                currentRegionLength = getSegmentFooterLength();
                System.out.println("Set current region length to segment footer length: " + currentRegionLength);
                break;

            case MESSAGE_FOOTER:
                System.out.println("In MESSAGE_FOOTER region");
                currentRegionLength = getMessageFooterLength();
                System.out.println("Set current region length to message header length: " + currentRegionLength);
                break;

            default:
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
        }

        System.out.println("Exiting update_current_region_length method");
    }

    private int tell() {
        System.out.println("Entering tell method");
        System.out.println("Current region: SMRegion." + currentRegion);
        System.out.println("Current segment number: " + currentSegmentNumber);
        System.out.println("Content offset: " + contentOffset);
        System.out.println("Message header length: " + getMessageHeaderLength());
        System.out.println("Segment header length: " + getSegmentHeaderLength());
        System.out.println("Segment footer length: " + getSegmentFooterLength());
        System.out.println("Current region offset: " + currentRegionOffset);

        int position;
        switch (currentRegion) {
            case MESSAGE_HEADER:
                position = currentRegionOffset;
                System.out.println("In MESSAGE_HEADER region, returning: " + position);
                break;

            case SEGMENT_HEADER:
                position = getMessageHeaderLength() + contentOffset
                    + (currentSegmentNumber - 1) * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + currentRegionOffset;
                System.out.println("In SEGMENT_HEADER region, returning: " + position);
                break;

            case SEGMENT_CONTENT:
                position = getMessageHeaderLength() + contentOffset
                    + (currentSegmentNumber - 1) * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + getSegmentHeaderLength();
                System.out.println("In SEGMENT_CONTENT region, returning: " + position);
                break;

            case SEGMENT_FOOTER:
                position = getMessageHeaderLength() + contentOffset
                    + (currentSegmentNumber - 1) * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + getSegmentHeaderLength() + currentRegionOffset;
                System.out.println("In SEGMENT_FOOTER region, returning: " + position);
                break;

            case MESSAGE_FOOTER:
                position = getMessageHeaderLength() + contentOffset
                    + currentSegmentNumber * (getSegmentHeaderLength() + getSegmentFooterLength())
                    + currentRegionOffset;
                System.out.println("In MESSAGE_FOOTER region, returning: " + position);
                break;

            default:
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
        }

        System.out.println("Exiting tell method");
        return position;
    }

    private int seek(int offset, SeekOrigin whence) {
        System.out.println("Entering seek method");
        System.out.println("Offset: " + offset);
        System.out.println("Whence: " + whence);

        int position;
        switch (whence) {
            case BEGIN:
                position = offset;
                break;

            case CURRENT:
                position = tell() + offset;
                break;

            case END:
                position = messageLength + offset;
                break;

            default:
                throw new IllegalArgumentException("Invalid value for whence: " + whence);
        }

        if (position > tell()) {
            throw new UnsupportedOperationException("This stream only supports seeking backwards.");
        }

        if (position < getMessageHeaderLength()) { //message header
            currentRegion = SMRegion.MESSAGE_HEADER;
            currentRegionOffset = position;
            contentOffset = 0;
            currentSegmentNumber = 0;
        } else if (position >= messageLength - getMessageFooterLength()) { //message footer
            currentRegion = SMRegion.MESSAGE_FOOTER;
            currentRegionOffset = position - (messageLength - getMessageFooterLength());
            contentOffset = contentLength;
            currentSegmentNumber = numSegments;
        } else {
            // The size of a "full" segment. Fine to use for calculating new segment number and pos
            int fullSegmentSize = getSegmentHeaderLength() + segmentSize + getSegmentFooterLength();
            int newSegmentNum = 1 + (position - getMessageHeaderLength()) / fullSegmentSize;
            int segmentPos = (position - getMessageHeaderLength()) % fullSegmentSize;
            int previousSegmentsTotalContentSize = (newSegmentNum - 1) * segmentSize;

            // We need the size of the segment we are seeking to for some of the calculations below
            int newSegmentSize = segmentSize;
            if (newSegmentNum == numSegments) {
                // The last segment size is the remaining content length
                newSegmentSize = contentLength - previousSegmentsTotalContentSize;
            }

            if (segmentPos < getSegmentHeaderLength()) { // segment header
                currentRegion = SMRegion.SEGMENT_HEADER;
                currentRegionOffset = segmentPos;
                contentOffset = previousSegmentsTotalContentSize;
            } else if (segmentPos < getSegmentHeaderLength() + newSegmentSize) { //segment content
                currentRegion = SMRegion.SEGMENT_CONTENT;
                currentRegionOffset = segmentPos - getSegmentHeaderLength();
                contentOffset = previousSegmentsTotalContentSize + currentRegionOffset;
            } else { //segment footer
                currentRegion = SMRegion.SEGMENT_FOOTER;
                currentRegionOffset = segmentPos - getSegmentHeaderLength() - newSegmentSize;
                contentOffset = previousSegmentsTotalContentSize + newSegmentSize;
            }

            currentSegmentNumber = newSegmentNum;
        }

        update_current_region_length();
        innerBuffer.position(contentOffset);

        System.out.println("New position: " + position);
        System.out.println("Exiting seek method");
        return position;
    }

    public ByteBuffer encode(int size) throws IOException {
        System.out.println("Entering read/encode method");
        System.out.println("Requested size: " + size);

        if (size == 0) {
            return ByteBuffer.allocate(0);
        }
        if (size < 0) {
            System.out.println("Size is negative, setting size to max");
            size = Integer.MAX_VALUE;
        }

        System.out.println("Adjusted size: " + size);
        int count = 0;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //ByteBuffer outputBuffer = ByteBuffer.allocate(size);

        while (count < size && tell() < messageLength) {
            System.out.println("Current count: " + count);
            int remaining = size - count;
            System.out.println("Remaining size to encode: " + remaining);
            if (currentRegion == SMRegion.MESSAGE_HEADER
                || currentRegion == SMRegion.SEGMENT_HEADER
                || currentRegion == SMRegion.SEGMENT_FOOTER
                || currentRegion == SMRegion.MESSAGE_FOOTER) {
                System.out.println("Reading metadata region: SMRegion." + currentRegion);
                count += encodeMetadataRegion(currentRegion, remaining, byteArrayOutputStream);
            } else if (currentRegion == SMRegion.SEGMENT_CONTENT) {
                System.out.println("Reading content region: " + currentRegion);
                count += encodeContent(remaining, byteArrayOutputStream);
            } else {
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
            }
            System.out.println("Updated count: " + count);
        }

        System.out.println("Exiting read/encode method");
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

    private int calculate_message_length() {
        System.out.println("Entering calculate_message_length method");

        int length = getMessageHeaderLength();
        System.out.println("Initial length (message header): " + length);

        length += (getSegmentHeaderLength() + getSegmentFooterLength()) * numSegments;
        System.out.println("Added segment headers and footers: " + length);

        length += contentLength;
        System.out.println("Added content length: " + length);

        length += getMessageFooterLength();
        System.out.println("Added message footer length: " + length);

        System.out.println("Exiting calculate_message_length method");
        return length;
    }

    private byte[] get_metadata_region(SMRegion region) {
        System.out.println("Entering get_metadata_region method");
        System.out.println("Region: SMRegion." + region);

        byte[] metadata;
        switch (region) {
            case MESSAGE_HEADER:
                System.out.println("In MESSAGE_HEADER region");
                metadata = generate_message_header(messageVersion, messageLength, flags, numSegments);
                break;

            case SEGMENT_HEADER:
                System.out.println("In SEGMENT_HEADER region");
                System.out.println("Calculating segment size");
                System.out.println("Segment Size: " + segmentSize);
                System.out.println("Content Length: " + contentLength);
                System.out.println("Content Offset: " + contentOffset);
                int segmentSize = Math.min(this.segmentSize, contentLength - contentOffset);
                System.out.println("Calculated segment size: " + segmentSize);
                metadata = generate_segment_header(currentSegmentNumber, segmentSize);
                break;

            case SEGMENT_FOOTER:
                System.out.println("In SEGMENT_FOOTER region");
                if (flags == Flags.STORAGE_CRC64) {
                    System.out.println("CRC64 flag is set, returning segment CRC64");
                    metadata = ByteBuffer.allocate(CRC64_LENGTH)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(this.segmentCRC64s.get(this.currentSegmentNumber))
                        .array();
                } else {
                    System.out.println("CRC64 flag is not set, returning empty bytes");
                    metadata = new byte[0];
                }
                break;

            case MESSAGE_FOOTER:
                System.out.println("In MESSAGE_FOOTER region");
                if (flags == Flags.STORAGE_CRC64) {
                    System.out.println("CRC64 flag is set, returning message CRC64");
                    metadata = ByteBuffer.allocate(CRC64_LENGTH)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(this.messageCRC64)
                        .array();
                } else {
                    System.out.println("CRC64 flag is not set, returning empty bytes");
                    metadata = new byte[0];
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid metadata SMRegion " + currentRegion);
        }

        return metadata;
    }

    private void advance_region(SMRegion current) {
        System.out.println("Entering advance_region method");
        System.out.println("Current region: SMRegion." + current);

        currentRegionOffset = 0;
        switch (current) {
            case MESSAGE_HEADER:
                System.out.println("Advancing from MESSAGE_HEADER to SEGMENT_HEADER");
                currentRegion = SMRegion.SEGMENT_HEADER;
                increment_current_segment();
                break;

            case SEGMENT_HEADER:
                System.out.println("Advancing from SEGMENT_HEADER to SEGMENT_CONTENT");
                currentRegion = SMRegion.SEGMENT_CONTENT;
                break;

            case SEGMENT_CONTENT:
                System.out.println("Advancing from SEGMENT_CONTENT to SEGMENT_FOOTER");
                currentRegion = SMRegion.SEGMENT_FOOTER;
                break;

            case SEGMENT_FOOTER:
                System.out.println("Advancing from SEGMENT_FOOTER");
                if (contentOffset == contentLength) {
                    System.out.println("End of content, advancing to MESSAGE_FOOTER");
                    currentRegion = SMRegion.MESSAGE_FOOTER;
                } else {
                    System.out.println("Advancing to next SEGMENT_HEADER");
                    currentRegion = SMRegion.SEGMENT_HEADER;
                    increment_current_segment();
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid SMRegion " + currentRegion);
        }

        System.out.println("Updating current region length");
        update_current_region_length();
        System.out.println("Exiting advance_region method");
    }

    private int encodeMetadataRegion(SMRegion region, int size, ByteArrayOutputStream output) {
        System.out.println("Entering read_metadata_region/encodeMetadataRegion method");
        System.out.println("Region: SMRegion." + region);
        System.out.println("Size: " + size);
        System.out.println("Output: " + Arrays.toString(output.toByteArray()));

        System.out.println("Getting metadata");
        byte[] metadata = get_metadata_region(region);

        int readSize = Math.min(size, this.currentRegionLength - this.currentRegionOffset);
        System.out.println("Read size: " + readSize);

        output.write(metadata, this.currentRegionOffset, readSize);

        this.currentRegionOffset += readSize;
        System.out.println("Updated current region offset: " + this.currentRegionOffset);
        if (this.currentRegionOffset == this.currentRegionLength && this.currentRegion != SMRegion.MESSAGE_FOOTER) {
            System.out.println("Advancing region");
            advance_region(region);
        }

        System.out.println("Exiting read_metadata_region/encodeMetadataRegion method");
        return readSize;
    }

    private int encodeContent(int size, ByteArrayOutputStream output) throws IOException {
        System.out.println("Entering _read_content/encodeContent method");
        System.out.println("Size: " + size);
        System.out.println("Output: " + Arrays.toString(output.toByteArray()));

        checksumOffset = checksumOffset - contentOffset;
        System.out.println("Checksum offset: " + checksumOffset);

        int readSize = Math.min(size, this.currentRegionLength - this.currentRegionOffset);
        System.out.println("readSize: " + readSize);
        System.out.println("size: " + size);
        System.out.println("currentRegionLength: " + this.currentRegionLength);
        System.out.println("currentRegionOffset: " + this.currentRegionOffset);
        if (checksumOffset != 0) {
            System.out
                .println("Checksum offset is not 0, setting read size to minimum of read size and checksum offset");
            readSize = Math.min(readSize, checksumOffset);
        }
        System.out.println("Read size: " + readSize);

        byte[] content = new byte[readSize];
        this.innerBuffer.get(content, 0, readSize);
        if (readSize != content.length) {
            throw new IOException("Failed to read content from inner buffer.");
        }
        output.write(content);

        if (flags == Flags.STORAGE_CRC64) {
            if (checksumOffset == 0) {
                System.out.println("Checksum offset is 0, updating segment and message CRC64s");
                this.segmentCRC64s.put(this.currentSegmentNumber,
                    StorageCrc64Calculator.compute(content, this.segmentCRC64s.get(this.currentSegmentNumber)));
                this.messageCRC64 = StorageCrc64Calculator.compute(content, this.messageCRC64);
            }
        }

        this.contentOffset += readSize;
        System.out.println("Updated content offset: " + this.contentOffset);

        if (contentOffset > checksumOffset) {
            System.out.println("ContentOffset is greater than checksumOffset, updating checksumOffset");
            checksumOffset += readSize;
        }

        this.currentRegionOffset += readSize;
        System.out.println("Updated current region offset: " + this.currentRegionOffset);

        if (this.currentRegionOffset == this.currentRegionLength) {
            System.out.println("Advancing region from SEGMENT_CONTENT");
            advance_region(SMRegion.SEGMENT_CONTENT);
        }

        System.out.println("Exiting _read_content/encodeContent method");
        return readSize;
    }

    private void increment_current_segment() {
        System.out.println("Entering increment_current_segment method");
        currentSegmentNumber++;
        System.out.println("Incremented current segment number: " + currentSegmentNumber);
        if (flags == Flags.STORAGE_CRC64) {
            System.out.println("Checking CRC64 flag");
            segmentCRC64s.putIfAbsent(currentSegmentNumber, 0L);
        }
        System.out.println("Exiting increment_current_segment method");
    }

}
