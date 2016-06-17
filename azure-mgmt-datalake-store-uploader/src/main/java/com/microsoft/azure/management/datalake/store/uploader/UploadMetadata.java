/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.UUID;

/**
 * Represents general metadata pertaining to an upload.
 */
public class UploadMetadata implements Serializable {
    private static Object saveSync = new Object();

    /**
     * Constructs a new UploadMetadata from the given parameters.
     *
     * @param metadataFilePath The file path to assign to this metadata file (for saving purposes).
     * @param uploadParameters The parameters to use for constructing this metadata.
     */
    public UploadMetadata(String metadataFilePath, UploadParameters uploadParameters) {
        this.metadataFilePath = metadataFilePath;

        this.uploadId = UUID.randomUUID().toString();
        this.inputFilePath = uploadParameters.getInputFilePath();
        this.targetStreamPath = uploadParameters.getTargetStreamPath();


        String[] streamData = splitTargetStreamPathByName();
        String streamName = streamData[0];
        String streamDirectory = streamData[1];

        if (streamDirectory == null || StringUtils.isEmpty(streamDirectory)) {
            // the scenario where the file is being uploaded at the root
            this.segmentStreamDirectory = MessageFormat.format("/{0}.segments.{1}", streamName, UUID.randomUUID());
        } else {
            // the scenario where the file is being uploaded in a sub folder
            this.segmentStreamDirectory = MessageFormat.format("{0}/{1}.segments.{2}",
                    streamDirectory,
                    streamName, UUID.randomUUID());
        }

        this.isBinary = uploadParameters.isBinary();

        File fileInfo = new File(uploadParameters.getInputFilePath());
        this.fileLength = fileInfo.length();

        this.encodingName = uploadParameters.getFileEncoding().name();

        // we are taking the smaller number of segments between segment lengths of 256 and the segment growth logic.
        // this protects us against agressive increase of thread count resulting in far more segments than
        // is reasonable for a given file size. We also ensure that each segment is at least 256mb in size.
        // This is the size that ensures we have the optimal storage creation in the store.
        int preliminarySegmentCount = (int) Math.ceil((double) fileInfo.length() / uploadParameters.getMaxSegementLength());
        this.segmentCount = Math.min(preliminarySegmentCount, UploadSegmentMetadata.calculateSegmentCount(fileInfo.length()));
        this.segmentLength = UploadSegmentMetadata.calculateSegmentLength(fileInfo.length(), this.segmentCount);

        this.segments = new UploadSegmentMetadata[this.segmentCount];
        for (int i = 0; i < this.segmentCount; i++) {
            this.segments[i] = new UploadSegmentMetadata(i, this);
        }
    }

    /**
     *
     * @return A value indicating the unique identifier associated with this upload.
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     *
     * @return A value indicating the full path to the file to be uploaded.
     */
    public String getInputFilePath() {
        return inputFilePath;
    }

    /**
     *
     * @return A value indicating the length (in bytes) of the file to be uploaded.
     */
    public long getFileLength() {
        return fileLength;
    }

    /**
     *
     * @return A value indicating the full stream path where the file will be uploaded to.
     */
    public String getTargetStreamPath() {
        return targetStreamPath;
    }

    /**
     *
     * @return A value indicating the directory path where intermediate segment streams will be stored.
     */
    public String getSegmentStreamDirectory() {
        return segmentStreamDirectory;
    }

    /**
     *
     * @return A value indicating the number of segments this file is split into for purposes of uploading it.
     */
    public int getSegmentCount() {
        return segmentCount;
    }

    /**
     *
     * @param segCount Sets the segment count to the specified count.
     */
    public void setSegmentCount(int segCount) {
        segmentCount = segCount;
    }

    /**
     *
     * @return A value indicating the length (in bytes) of each segment of the file (except the last one, which may be less).
     */
    public long getSegmentLength() {
        return segmentLength;
    }

    /**
     *
     * @param segLength The length to set the segment length to.
     */
    public void setSegmentLength(long segLength) {
        segmentLength = segLength;
    }
    /**
     *
     * @return A pointer to an array of segment metadata. The segments are ordered by their segment number (sequence).
     */
    public UploadSegmentMetadata[] getSegments() {
        return segments;
    }

    /**
     *
     * @param segs The value to set the segment array to.
     */
    public void setSegments(UploadSegmentMetadata[] segs) {
        segments = segs;
    }

    /**
     *
     * @return A value indicating whether the upload file should be treated as a binary file or not.
     */
    public boolean isBinary() {
        return isBinary;
    }

    /**
     *
     * @return The name of the current encoding being used.
     */
    public String getEncodingName() {
        return encodingName;
    }

    /**
     *
     * @return A value indicating the record boundary delimiter for the file, if any.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     *
     * @return A value indicating the path where this metadata file is located.
     */
    public String getMetadataFilePath() {
        return metadataFilePath;
    }

    /**
     *
     * @param metadataFilePath A value indicating the path where this metadata file is located.
     */
    public void setMetadataFilePath(String metadataFilePath) {
        this.metadataFilePath = metadataFilePath;
    }

    private transient String metadataFilePath;

    /**
     *
     * @param uploadId A value indicating the unique identifier associated with this upload.
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /**
     *
     * @param inputFilePath A value indicating the full path to the file to be uploaded.
     */
    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    /**
     *
     * @param fileLength A value indicating the length (in bytes) of the file to be uploaded.
     */
    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    /**
     *
     * @param targetStreamPath A value indicating the full stream path where the file will be uploaded to.
     */
    public void setTargetStreamPath(String targetStreamPath) {
        this.targetStreamPath = targetStreamPath;
    }

    /**
     *
     * @param segmentStreamDirectory A value indicating the directory path where intermediate segment streams will be stored.
     */
    public void setSegmentStreamDirectory(String segmentStreamDirectory) {
        this.segmentStreamDirectory = segmentStreamDirectory;
    }

    /**
     *
     * @param binary A value indicating whether the upload file should be treated as a binary file or not.
     */
    public void setBinary(boolean binary) {
        isBinary = binary;
    }

    /**
     *
     * @param encodingName The name of the current encoding being used.
     */
    public void setEncodingName(String encodingName) {
        this.encodingName = encodingName;
    }

    /**
     *
     * @param delimiter A value indicating the record boundary delimiter for the file, if any.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    private String uploadId;

    private String inputFilePath;

    private long fileLength;

    private String targetStreamPath;

    private String segmentStreamDirectory;

    private int segmentCount;

    private long segmentLength;

    private UploadSegmentMetadata[] segments;

    private boolean isBinary;

    private String encodingName;

    private String delimiter;

    /**
     * Initializes a new instance of the UploadMetadata class for use with unit testing.
     */
    protected UploadMetadata() {
        this.encodingName = StandardCharsets.UTF_8.name();
    }

    /**
     * Attempts to load an UploadMetadata object from the given file.
     *
     * @param filePath The full path to the file where to load the metadata from
     * @return A deserialized {@link UploadMetadata} object from the file specified.
     * @throws FileNotFoundException Thrown if the filePath is inaccessible or does not exist
     * @throws InvalidMetadataException Thrown if the metadata is not in the expected format.
     */
    public static UploadMetadata loadFrom(String filePath) throws FileNotFoundException, InvalidMetadataException {
        if (!new File(filePath).exists()) {
            throw new FileNotFoundException("Could not find metadata file: " + filePath);
        }

        UploadMetadata result = null;
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (UploadMetadata) in.readObject();
            in.close();
            fileIn.close();
            result.metadataFilePath = filePath;
            return result;
        } catch (Exception ex) {
            throw new InvalidMetadataException("Unable to parse metadata file", ex);
        }
    }

    /**
     * Saves the given metadata to its canonical location. This method is thread-safe.
     *
     * @throws IOException Thrown if the file cannot be saved due to accessibility or there is an error saving the stream to disk.
     * @throws InvalidMetadataException Thrown if the metadata is invalid.
     */
    public void save() throws IOException, InvalidMetadataException {
        if (this.metadataFilePath == null || StringUtils.isEmpty(this.metadataFilePath)) {
            throw new InvalidObjectException("Null or empty metadataFilePath. Cannot save metadata until this property is set.");
        }

        //quick check to ensure that the metadata we constructed is sane
        this.validateConsistency();

        synchronized (saveSync) {
            File curMetadata = new File(this.metadataFilePath);
            if (curMetadata.exists()) {
                curMetadata.delete();
            }

            // always create the full path to the file, since this will not throw if it already exists.
            curMetadata.getParentFile().mkdirs();
            curMetadata.createNewFile();
            try {
                FileOutputStream fileOut =
                        new FileOutputStream(this.metadataFilePath);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(this);
                out.close();
                fileOut.close();
            } catch (Exception ex) {
                throw new InvalidMetadataException("Unable to parse metadata object and write it to a file", ex);
            }
        }
    }

    /**
     * Deletes the metadata file from disk.
     *
     * @throws InvalidObjectException Thrown if the metadata file path has not yet been set.
     */
    public void deleteFile() throws InvalidObjectException {
        if (this.metadataFilePath == null || StringUtils.isEmpty(this.metadataFilePath)) {
            throw new InvalidObjectException("Null or empty metadataFilePath. Cannot delete metadata until this property is set.");
        }

        File curMetadata = new File(this.metadataFilePath);
        if (curMetadata.exists()) {
            curMetadata.delete();
        }
    }

    /**
     * Verifies the given metadata for consistency. Checks include:
     *  Completeness
     *  Existence and consistency with local file
     *  Segment data consistency
     *
     * @throws InvalidMetadataException Thrown if the metadata is invalid.
     */
    public void validateConsistency() throws InvalidMetadataException {
        if (this.segments == null || this.segments.length != this.segmentCount) {
            throw new InvalidMetadataException("Inconsistent number of segments");
        }

        long sum = 0;
        int lastSegmentNumber = -1;
        BitSet segments = new BitSet(this.segmentCount);

        for (UploadSegmentMetadata segment : this.segments) {
            if (segment.getSegmentNumber() < 0 || segment.getSegmentNumber() >= this.segmentCount) {
                throw new InvalidMetadataException(MessageFormat.format("Segment numbers must be at least 0 and less than {0}. Found segment number {1}.", this.segmentCount, segment.getSegmentNumber()));
            }

            if (segment.getSegmentNumber() <= lastSegmentNumber) {
                throw new InvalidMetadataException(MessageFormat.format("Segment number {0} appears out of order.", segment.getSegmentNumber()));
            }

            if (segments.get(segment.getSegmentNumber())) {
                throw new InvalidMetadataException(MessageFormat.format("Segment number {0} appears twice", segment.getSegmentNumber()));
            }

            if (segment.getOffset() != sum) {
                throw new InvalidMetadataException(MessageFormat.format("Segment number {0} has an invalid starting offset ({1}). Expected {2}.", segment.getSegmentNumber(), segment.getOffset(), sum));
            }

            segments.set(segment.getSegmentNumber());
            sum += segment.getLength();
            lastSegmentNumber = segment.getSegmentNumber();
        }

        if (sum != this.fileLength) {
            throw new InvalidMetadataException("The individual segment lengths do not add up to the input File length");
        }
    }

    /**
     * Splits the target stream path, returning the name of the stream and storing the full directory path (if any) in an out variable.
     *
     * @return A string array with the stream name is at index 0 and the stream path (if any) at index 1.
     */
    public String[] splitTargetStreamPathByName() {
        String[] toReturn = new String[2];
        int numFoldersInPath = this.targetStreamPath.split("/").length;
        if (numFoldersInPath - 1 == 0 || (numFoldersInPath - 1 == 1 && this.targetStreamPath.startsWith("/"))) {
            // the scenario where the file is being uploaded at the root
            toReturn[0] = this.targetStreamPath.replaceAll("^[/]", "");
            toReturn[1] = null;
        } else {
            // the scenario where the file is being uploaded in a sub folder
            toReturn[0] = this.targetStreamPath.substring(this.targetStreamPath.lastIndexOf('/') + 1);
            toReturn[1] = this.targetStreamPath.substring(0, this.targetStreamPath.lastIndexOf('/'));
        }

        return toReturn;
    }
}
