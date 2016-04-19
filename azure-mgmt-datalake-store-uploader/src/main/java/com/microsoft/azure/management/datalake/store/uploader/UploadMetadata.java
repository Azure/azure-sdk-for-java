/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.UUID;

/**
 * Represents general metadata pertaining to an upload.
 */
public class UploadMetadata implements Serializable {
    private static Object SaveSync = new Object();

    /**
     * Constructs a new UploadMetadata from the given parameters.
     *
     * @param metadataFilePath The file path to assign to this metadata file (for saving purposes).
     * @param uploadParameters The parameters to use for constructing this metadata.
     */
    public UploadMetadata(String metadataFilePath, UploadParameters uploadParameters) {
        this.MetadataFilePath = metadataFilePath;

        this.UploadId = UUID.randomUUID().toString();
        this.InputFilePath = uploadParameters.getInputFilePath();
        this.TargetStreamPath = uploadParameters.getTargetStreamPath();


        String[] streamData = SplitTargetStreamPathByName();
        String streamName = streamData[0];
        String streamDirectory = streamData[1];

        if (streamDirectory == null || StringUtils.isEmpty(streamDirectory)) {
            // the scenario where the file is being uploaded at the root
            this.SegmentStreamDirectory = MessageFormat.format("/{0}.segments.{1}", streamName, UUID.randomUUID());
        } else {
            // the scenario where the file is being uploaded in a sub folder
            this.SegmentStreamDirectory = MessageFormat.format("{0}/{1}.segments.{2}",
                    streamDirectory,
                    streamName, UUID.randomUUID());
        }

        this.IsBinary = uploadParameters.isBinary();

        File fileInfo = new File(uploadParameters.getInputFilePath());
        this.FileLength = fileInfo.length();

        this.EncodingName = uploadParameters.getFileEncoding().name();

        // we are taking the smaller number of segments between segment lengths of 256 and the segment growth logic.
        // this protects us against agressive increase of thread count resulting in far more segments than
        // is reasonable for a given file size. We also ensure that each segment is at least 256mb in size.
        // This is the size that ensures we have the optimal storage creation in the store.
        int preliminarySegmentCount = (int) Math.ceil((double) fileInfo.length() / uploadParameters.getMaxSegementLength());
        this.SegmentCount = Math.min(preliminarySegmentCount, UploadSegmentMetadata.CalculateSegmentCount(fileInfo.length()));
        this.SegmentLength = UploadSegmentMetadata.CalculateSegmentLength(fileInfo.length(), this.SegmentCount);

        this.Segments = new UploadSegmentMetadata[this.SegmentCount];
        for (int i = 0; i < this.SegmentCount; i++) {
            this.Segments[i] = new UploadSegmentMetadata(i, this);
        }
    }

    /**
     * Gets or sets a value indicating the unique identifier associated with this upload.
     */
    public String UploadId;

    /**
     * Gets or sets a value indicating the full path to the file to be uploaded.
     */
    public String InputFilePath;

    /**
     * Gets or sets a value indicating the length (in bytes) of the file to be uploaded.
     */
    public long FileLength;

    /**
     * Gets or sets a value indicating the full stream path where the file will be uploaded to.
     */
    public String TargetStreamPath;

    /**
     * Gets or sets a value indicating the directory path where intermediate segment streams will be stored.
     */
    public String SegmentStreamDirectory;

    /**
     * Gets or sets a value indicating the number of segments this file is split into for purposes of uploading it.
     */
    public int SegmentCount;

    /**
     * Gets or sets a value indicating the length (in bytes) of each segment of the file (except the last one, which may be less).
     */
    public long SegmentLength;

    /**
     * Gets a pointer to an array of segment metadata. The segments are ordered by their segment number (sequence).
     */
    public UploadSegmentMetadata[] Segments;

    /**
     * Gets a value indicating whether the upload file should be treated as a binary file or not.
     */
    public boolean IsBinary;
    
    /**
     * Gets the name of the current encoding being used.
     */
    public String EncodingName;

    /**
     * Gets a value indicating the record boundary delimiter for the file, if any.
     */
    public String Delimiter;

    /**
     * Gets a value indicating the path where this metadata file is located.
     */
    public transient String MetadataFilePath;

    /**
     * Initializes a new instance of the UploadMetadata class for use with unit testing
     */
    protected UploadMetadata() {
        this.EncodingName = StandardCharsets.UTF_8.name();
    }

    /**
     * Attempts to load an UploadMetadata object from the given file.
     *
     * @param filePath The full path to the file where to load the metadata from
     * @return A deserialized {@link UploadMetadata} object from the file specified.
     * @throws FileNotFoundException
     * @throws InvalidMetadataException
     */
    public static UploadMetadata LoadFrom(String filePath) throws FileNotFoundException, InvalidMetadataException {
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
            result.MetadataFilePath = filePath;
            return result;
        } catch (Exception ex) {
            throw new InvalidMetadataException("Unable to parse metadata file", ex);
        }
    }

    /**
     * Saves the given metadata to its canonical location. This method is thread-safe.
     *
     * @throws IOException
     * @throws InvalidMetadataException
     */
    public void Save() throws IOException, InvalidMetadataException {
        if (this.MetadataFilePath == null || StringUtils.isEmpty(this.MetadataFilePath)) {
            throw new InvalidObjectException("Null or empty MetadataFilePath. Cannot save metadata until this property is set.");
        }

        //quick check to ensure that the metadata we constructed is sane
        this.ValidateConsistency();

        synchronized (SaveSync) {
            File curMetadata = new File(this.MetadataFilePath);
            if (curMetadata.exists()) {
                curMetadata.delete();
            }

            // always create the full path to the file, since this will not throw if it already exists.
            curMetadata.getParentFile().mkdirs();
            curMetadata.createNewFile();
            try {
                FileOutputStream fileOut =
                        new FileOutputStream(this.MetadataFilePath);
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
     * @throws InvalidObjectException
     */
    public void DeleteFile() throws InvalidObjectException {
        if (this.MetadataFilePath == null || StringUtils.isEmpty(this.MetadataFilePath)) {
            throw new InvalidObjectException("Null or empty MetadataFilePath. Cannot delete metadata until this property is set.");
        }

        File curMetadata = new File(this.MetadataFilePath);
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
     * @throws InvalidMetadataException
     */
    public void ValidateConsistency() throws InvalidMetadataException {
        if (this.Segments == null || this.Segments.length != this.SegmentCount) {
            throw new InvalidMetadataException("Inconsistent number of segments");
        }

        long sum = 0;
        int lastSegmentNumber = -1;
        BitSet segments = new BitSet(this.SegmentCount);

        for (UploadSegmentMetadata segment : this.Segments) {
            if (segment.SegmentNumber < 0 || segment.SegmentNumber >= this.SegmentCount) {
                throw new InvalidMetadataException(MessageFormat.format("Segment numbers must be at least 0 and less than {0}. Found segment number {1}.", this.SegmentCount, segment.SegmentNumber));
            }

            if (segment.SegmentNumber <= lastSegmentNumber) {
                throw new InvalidMetadataException(MessageFormat.format("Segment number {0} appears out of order.", segment.SegmentNumber));
            }

            if (segments.get(segment.SegmentNumber)) {
                throw new InvalidMetadataException(MessageFormat.format("Segment number {0} appears twice", segment.SegmentNumber));
            }

            if (segment.Offset != sum) {
                throw new InvalidMetadataException(MessageFormat.format("Segment number {0} has an invalid starting offset ({1}). Expected {2}.", segment.SegmentNumber, segment.Offset, sum));
            }

            segments.set(segment.SegmentNumber);
            sum += segment.Length;
            lastSegmentNumber = segment.SegmentNumber;
        }

        if (sum != this.FileLength) {
            throw new InvalidMetadataException("The individual segment lengths do not add up to the input File Length");
        }
    }

    /**
     * Splits the target stream path, returning the name of the stream and storing the full directory path (if any) in an out variable.
     *
     * @return A string array with the stream name is at index 0 and the stream path (if any) at index 1.
     */
    public String[] SplitTargetStreamPathByName() {
        String[] toReturn = new String[2];
        int numFoldersInPath = this.TargetStreamPath.split("/").length;
        if (numFoldersInPath - 1 == 0 || (numFoldersInPath - 1 == 1 && this.TargetStreamPath.startsWith("/"))) {
            // the scenario where the file is being uploaded at the root
            toReturn[0] = this.TargetStreamPath.replaceAll("^[/]", "");
            toReturn[1] = null;
        } else {
            // the scenario where the file is being uploaded in a sub folder
            toReturn[0] = this.TargetStreamPath.substring(this.TargetStreamPath.lastIndexOf('/') + 1);
            toReturn[1] = this.TargetStreamPath.substring(0, this.TargetStreamPath.lastIndexOf('/'));
        }

        return toReturn;
    }
}
