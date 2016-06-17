/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Represents metadata for a particular file segment.
 */
public class UploadSegmentMetadata implements Serializable {

    /**
     * Initializes a new instance of the UploadSegmentMetadata for use with unit tests.
     */
    protected UploadSegmentMetadata() {
        // does nothing, used for unit tests
    }

    /**
     * Creates a new UploadSegmentMetadata with the given segment number.
     *
     * @param segmentNumber The segment number for this instance.
     * @param metadata The full metadata associated with this segment.
     */
    public UploadSegmentMetadata(int segmentNumber, UploadMetadata metadata) {
        this.segmentNumber = segmentNumber;
        this.status = SegmentUploadStatus.Pending;

        String targetStreamName = metadata.splitTargetStreamPathByName()[0];
        this.path = MessageFormat.format("{0}/{1}.{2}.segment{3}", metadata.getSegmentStreamDirectory(), targetStreamName, metadata.getUploadId(), this.segmentNumber);
        this.offset = this.segmentNumber * metadata.getSegmentLength(); // segment number is zero-based
        this.length = calculateSegmentLength(this.segmentNumber, metadata);
    }

    /**
     * Calculates the length of a typical (non-terminal) segment for a file of the given length that is split into the given number of segments.
     *
     * @param fileLength The length of the file, in bytes.
     * @param segmentCount The number of segments to split the file into.
     * @return The length of this segment, in bytes.
     */
    public static long calculateSegmentLength(long fileLength, int segmentCount) {
        if (segmentCount < 0) {
            throw new IllegalArgumentException("Number of segments must be a positive integer");
        }

        if (segmentCount == 0) {
            // In this case, we are attempting to upload an empty file,
            // in which case the uploader should just return
            return 0;
        }

        long segmentLength = fileLength / segmentCount;

        //if the file cannot be split into even segments, we need to increment the typical segment length by 1
        //in order to have the last segment in the file be smaller than the other ones.
        if (fileLength % segmentCount != 0) {
            //BUT we can only do this IF this wouldn't cause the last segment to have a negative length
            if (fileLength - (segmentCount - 1) * (segmentLength + 1) > 0) {
                segmentLength++;
            }
        }

        return segmentLength;
    }

    /**
     * Calculates the length of the segment with given number for a file with given length that is split into the given number of segments.
     * @param segmentNumber The segment number.
     * @param metadata The metadata for the current upload.
     * @return The length of this segment, in bytes.
     */
    public static long calculateSegmentLength(int segmentNumber, UploadMetadata metadata) {
        if (segmentNumber < 0 || segmentNumber >= metadata.getSegmentCount()) {
            throw new IndexOutOfBoundsException("Segment Number must be at least zero and less than the total number of segments");
        }

        if (metadata.getFileLength() < 0) {
            throw new IllegalArgumentException("Cannot have a negative file length");
        }

        //verify if the last segment would have a positive value
        long lastSegmentLength = metadata.getFileLength() - (metadata.getSegmentCount() - 1) * metadata.getSegmentLength();
        if (lastSegmentLength < 0) {
            throw new IllegalArgumentException("The given values for segmentCount and segmentLength cannot possibly be used to split a file with the given fileLength (the last segment would have a negative length)");
        } else if (lastSegmentLength > metadata.getSegmentLength()) {
            //verify if the given segmentCount and segmentLength combination would produce an even split
            if (metadata.getFileLength() - (metadata.getSegmentCount() - 1) * (metadata.getSegmentLength() + 1) > 0) {
                throw new IllegalArgumentException("The given values for segmentCount and segmentLength would not produce an even split of a file with given fileLength");
            }
        }

        if (metadata.getFileLength() == 0) {
            return 0;
        }

        //all segments except the last one have the same length;
        //the last one only has the 'full' length if by some miracle the file length is a perfect multiple of the Segment length
        if (segmentNumber < metadata.getSegmentCount() - 1) {
            return metadata.getSegmentLength();
        } else {
            return lastSegmentLength;
        }
    }

    /**
     * Used to calculate the total number of segments that we should create.
     */
    private static final int BASE_MULTIPLIER = 50;

    /**
     * The Multiplier is the number of times the segment count is inflated when the length of the file increases by a factor of 'Reducer'.
     */
    private static final int SEGMENT_COUNT_MULTIPLIER = 2;

    /**
     * The minimum number of bytes in a segment. For best performance, should be sync-ed with the upload buffer length.
     */
    public static final int MINIMUM_SEGMENT_SIZE = SingleSegmentUploader.BUFFER_LENGTH;

    /**
     * Calculates the number of segments a file of the given length should be split into.
     * The method to calculate this is based on some empirical measurements that allows both the number of segments and the length of each segment to grow as the input file size grows.
     * They both grow on a logarithmic pattern as the file length increases.
     * The formula is roughly this:
     *  Multiplier = Min(100, 50 * 2 ^ Log10(FileLengthInGB))
     *  SegmentCount = Max(1, Multiplier * 2 ^ Log10(FileLengthInGB)
     * Essentially we quadruple the number of segments for each tenfold increase in the file length, with certain caps. The formula is designed to support both small files and
     * extremely large files (and not cause very small segment lengths or very large number of segments).
     *
     * @param fileLength The length of the file, in bytes.
     * @return The number of segments to split the file into. Returns 0 if fileLength is 0.
     */
    public static int calculateSegmentCount(long fileLength) {
        if (fileLength < 0) {
            throw new IllegalArgumentException("File length cannot be negative");
        }

        if (fileLength == 0) {
            //empty file => no segments
            return 0;
        }

        int minNumberOfSegments = (int) Math.max(1, fileLength / MINIMUM_SEGMENT_SIZE);

        //convert the file length into GB
        double lengthInGb = fileLength / 1024.0 / 1024 / 1024;

        //apply the formula described in the class description and return the result
        double baseMultiplier = calculateBaseMultiplier(lengthInGb);
        int segmentCount = (int) (baseMultiplier * Math.pow(SEGMENT_COUNT_MULTIPLIER, Math.log10(lengthInGb)));
        if (segmentCount > minNumberOfSegments) {
            segmentCount = minNumberOfSegments;
        }

        if (segmentCount < 1) {
            segmentCount = 1;
        }

        return segmentCount;
    }

    private static double calculateBaseMultiplier(double lengthInGb) {
        double value = BASE_MULTIPLIER * Math.pow(2, Math.log10(lengthInGb));
        return Math.min(100, value);
    }

    private int segmentNumber;

    private long offset;

    private long length;

    private SegmentUploadStatus status;

    private String path;

    /**
     *
     * @return A value indicating the stream path assigned to this segment.
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @param path A value indicating the stream path assigned to this segment.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @return A value indicating the number (sequence) of the segment in the file.
     */
    public int getSegmentNumber() {
        return segmentNumber;
    }

    /**
     *
     * @param segmentNumber A value indicating the number (sequence) of the segment in the file.
     */
    public void setSegmentNumber(int segmentNumber) {
        this.segmentNumber = segmentNumber;
    }

    /**
     *
     * @return A value indicating the starting offset of the segment in the file.
     */
    public long getOffset() {
        return offset;
    }

    /**
     *
     * @param offset A value indicating the starting offset of the segment in the file.
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     *
     * @return A value indicating the size of the segment (in bytes).
     */
    public long getLength() {
        return length;
    }

    /**
     *
     * @param length A value indicating the size of the segment (in bytes).
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     *
     * @return A value indicating the current upload status for this segment.
     */
    public SegmentUploadStatus getStatus() {
        return status;
    }

    /**
     *
     * @param status A value indicating the current upload status for this segment.
     */
    public void setStatus(SegmentUploadStatus status) {
        this.status = status;
    }
}
