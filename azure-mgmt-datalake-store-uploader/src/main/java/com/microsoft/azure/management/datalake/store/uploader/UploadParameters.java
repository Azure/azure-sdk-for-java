/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents parameters for the DataLake Uploader.
 */
public class UploadParameters {

    /**
     * Creates a new set of parameters for the DataLake Uploader with optional values set with their defaults:
     *  threadCount = 1
     *  isOverwrite = false
     *  isResume = false
     *  isBinary = true
     *  maxSegmentLength = 256mb
     *  localMetadataLocation = File.createTempFile()
     *
     * @param inputFilePath The full path to the file to be uploaded.
     * @param targetStreamPath The full stream path where the file will be uploaded to.
     * @param accountName Name of the account to upload to.
     */
    public UploadParameters(String inputFilePath, String targetStreamPath, String accountName) {
        this(inputFilePath, targetStreamPath, accountName, 1, false, false, true, 256*1024*1024, null);
    }

    /**
     * Creates a new set of parameters for the DataLake Uploader with the following optional values set with their defaults:
     *
     *  isBinary = true
     *  maxSegmentLength = 256mb
     *  localMetadataLocation = File.createTempFile()
     *
     * @param inputFilePath The full path to the file to be uploaded.
     * @param targetStreamPath The full stream path where the file will be uploaded to.
     * @param accountName Name of the account to upload to.
     * @param threadCount The maximum number of parallel threads to use for the upload.
     * @param isOverwrite Whether to overwrite the target stream or not.
     * @param isResume Indicates whether to resume a previously interrupted upload.
     */
    public UploadParameters(String inputFilePath, String targetStreamPath, String accountName, int threadCount, boolean isOverwrite, boolean isResume) {
        this(inputFilePath, targetStreamPath, accountName, threadCount, isOverwrite, isResume, true, 256*1024*1024, null);
    }

    /**
     * Creates a new set of parameters for the DataLake Uploader with the following optional values set with their defaults:
     *
     *  isBinary = true
     *  maxSegmentLength = 256mb
     *
     * @param inputFilePath The full path to the file to be uploaded.
     * @param targetStreamPath The full stream path where the file will be uploaded to.
     * @param accountName Name of the account to upload to.
     * @param threadCount The maximum number of parallel threads to use for the upload.
     * @param isOverwrite Whether to overwrite the target stream or not.
     * @param isResume Indicates whether to resume a previously interrupted upload.
     * @param localMetadataLocation Indicates the directory path where to store the local upload metadata file while the upload is in progress. This location must be writeable from this application. Default location if null: File.createTempFile()
     */
    public UploadParameters(String inputFilePath, String targetStreamPath, String accountName, int threadCount, boolean isOverwrite, boolean isResume, String localMetadataLocation) {
        this(inputFilePath, targetStreamPath, accountName, threadCount, isOverwrite, isResume, true, 256*1024*1024, localMetadataLocation);
    }

    /**
     * Creates a new set of parameters for the DataLake Uploader.
     *
     * @param inputFilePath The full path to the file to be uploaded.
     * @param targetStreamPath The full stream path where the file will be uploaded to.
     * @param accountName Name of the account to upload to.
     * @param threadCount The maximum number of parallel threads to use for the upload.
     * @param isOverwrite Whether to overwrite the target stream or not.
     * @param isResume Indicates whether to resume a previously interrupted upload.
     * @param isBinary Indicates whether to treat the input file as a binary file (true), or whether to align upload blocks to record boundaries (false).
     * @param maxSegmentLength The recommended value is 256mb, which gives optimal performance. Modify at your own risk.
     * @param localMetadataLocation Indicates the directory path where to store the local upload metadata file while the upload is in progress. This location must be writeable from this application. Default location if null: File.createTempFile()
     */
    public UploadParameters(String inputFilePath, String targetStreamPath, String accountName, int threadCount, boolean isOverwrite, boolean isResume, boolean isBinary, long maxSegmentLength, String localMetadataLocation) {
        this.setInputFilePath(inputFilePath);
        this.setTargetStreamPath(targetStreamPath);
        this.setThreadCount(threadCount);
        this.setAccountName(accountName);
        this.setOverwrite(isOverwrite);
        this.setResume(isResume);
        this.setBinary(isBinary);
        this.setMaxSegementLength(maxSegmentLength);

        if (localMetadataLocation == null || StringUtils.isEmpty(localMetadataLocation)) {
            localMetadataLocation = System.getProperty("java.io.tmpdir");
        }

        this.setLocalMetadataLocation(localMetadataLocation);

        this.setUseSegmentBlockBackOffRetryStrategy(true);

        // TODO: in the future we will expose these as optional parameters, allowing customers to specify encoding and delimiters.
        this.setFileEncoding(StandardCharsets.UTF_8);
        this.setDelimiter(null);
    }

    /**
     * Creates a new set of parameters for the DataLake Uploader used for unit testing
     *
     * @param inputFilePath The full path to the file to be uploaded.
     * @param targetStreamPath The full stream path where the file will be uploaded to.
     * @param accountName Name of the account to upload to.
     * @param useSegmentBlockBackOffRetryStrategy if set to <code>true</code> [use segment block back off retry strategy].
     * @param threadCount The maximum number of parallel threads to use for the upload.
     * @param isOverwrite Whether to overwrite the target stream or not.
     * @param isResume Indicates whether to resume a previously interrupted upload.
     * @param isBinary Indicates whether to treat the input file as a binary file (true), or whether to align upload blocks to record boundaries (false).
     * @param maxSegmentLength The recommended value is 256mb, which gives optimal performance. Modify at your own risk.
     * @param localMetadataLocation Indicates the directory path where to store the local upload metadata file while the upload is in progress. This location must be writeable from this application. Default location if null: File.createTempFile()
     */
    protected UploadParameters(String inputFilePath, String targetStreamPath, String accountName, boolean useSegmentBlockBackOffRetryStrategy, int threadCount, boolean isOverwrite, boolean isResume, boolean isBinary, long maxSegmentLength, String localMetadataLocation) {
        this(inputFilePath, targetStreamPath, accountName, threadCount, isOverwrite, isResume, isBinary, maxSegmentLength, localMetadataLocation);
        this.setUseSegmentBlockBackOffRetryStrategy(useSegmentBlockBackOffRetryStrategy);
    }

    /**
     * Gets a value indicating whether [to use segment block back off retry strategy].
     *
     * @return <code>true</code> if [to use segment block back off retry strategy]; otherwise, <code>false</code>.
     */
    public boolean isUseSegmentBlockBackOffRetryStrategy() {
        return useSegmentBlockBackOffRetryStrategy;
    }

    /**
     * Internally sets the value of whether [to use segment block back off retry strategy].
     *
     * @param useSegmentBlockBackOffRetryStrategy
     */
    private void setUseSegmentBlockBackOffRetryStrategy(boolean useSegmentBlockBackOffRetryStrategy) {
        this.useSegmentBlockBackOffRetryStrategy = useSegmentBlockBackOffRetryStrategy;
    }

    /**
     * Gets a value indicating the full path to the file to be uploaded.
     *
     * @return The input file path.
     */
    public String getInputFilePath() {
        return inputFilePath;
    }

    /**
     * Internally sets the input file path
     *
     * @param inputFilePath
     */
    private void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    /**
     * Gets a value indicating the full stream path where the file will be uploaded to.
     *
     * @return The target stream path.
     */
    public String getTargetStreamPath() {
        return targetStreamPath;
    }

    /**
     * Internally sets the target stream path.
     *
     * @param targetStreamPath
     */
    private void setTargetStreamPath(String targetStreamPath) {
        this.targetStreamPath = targetStreamPath;
    }

    /**
     * Gets a value indicating the name of the account to upload to.
     *
     * @return The name of the account.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Internally sets the account name to upload to.
     *
     * @param accountName
     */
    private void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Gets a value indicating the maximum number of parallel threads to use for the upload.
     *
     * @return The thread count.
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Internally sets the number of threads that are allowed for the upload.
     *
     * @param threadCount The number of threads to use for the upload.
     */
    protected void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * Gets a value indicating whether to overwrite the target stream if it already exists.
     *
     * @return <code>true</code> if this instance is overwrite; otherwise, <code>false</code>.
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * Internally sets whether the target stream can be overwritten.
     *
     * @param overwrite
     */
    private void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * Gets a value indicating whether to resume a previously interrupted upload.
     *
     * @return <code>true</code> if this instance is resume; otherwise, <code>false</code>.
     */
    public boolean isResume() {
        return resume;
    }

    /**
     * Internally set whether this is a previous upload being resumed.
     *
     * @param resume
     */
    private void setResume(boolean resume) {
        this.resume = resume;
    }

    /**
     * Gets a value indicating whether the input file should be treated as a binary (true) or a delimited input (false).
     *
     * @return <code>true</code> if this instance is binary; otherwise, <code>false</code>.
     */
    public boolean isBinary() {
        return binary;
    }

    /**
     * Internally set whether the file being uploaded should be binary or delimited input.
     *
     * @param binary
     */
    private void setBinary(boolean binary) {
        this.binary = binary;
    }

    /**
     * Gets the maximum length of each segement in bytes.
     *
     * @return The maximum length of each segment in bytes.
     */
    public long getMaxSegementLength() {
        return maxSegementLength;
    }

    /**
     * Internally set the maximum length of each segment in bytes.
     *
     * @param maxSegementLength
     */
    private void setMaxSegementLength(long maxSegementLength) {
        this.maxSegementLength = maxSegementLength;
    }

    /**
     * Gets a value indicating the directory path where to store the metadata for the upload.
     *
     * @return The local metadata location.
     */
    public String getLocalMetadataLocation() {
        return localMetadataLocation;
    }

    /**
     * Internally set the local metadata location.
     *
     * @param localMetadataLocation
     */
    private void setLocalMetadataLocation(String localMetadataLocation) {
        this.localMetadataLocation = localMetadataLocation;
    }

    /**
     * Gets a value indicating the encoding of the file being uploaded.
     *
     * @return The file encoding.
     */
    public Charset getFileEncoding() {
        return fileEncoding;
    }

    /**
     * Internally sets the value of the file encoding
     *
     * @param fileEncoding
     */
    private void setFileEncoding(Charset fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    /**
     * Gets a value indicating the record boundary delimiter for the file, if any.
     *
     * @return The record boundary delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Internally set the value of the record boundary delimiter.
     *
     * @param delimiter
     */
    private void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    private boolean useSegmentBlockBackOffRetryStrategy;

    private String inputFilePath;

    private String targetStreamPath;

    private String accountName;

    private int threadCount;

    private boolean overwrite;

    private boolean resume;

    private boolean binary;

    private long maxSegementLength;

    private String localMetadataLocation;

    private Charset fileEncoding;

    private String delimiter;
}
