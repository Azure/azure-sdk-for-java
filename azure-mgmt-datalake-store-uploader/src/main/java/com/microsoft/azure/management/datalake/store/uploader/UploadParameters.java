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
 * Created by begoldsm on 4/11/2016.
 */
public class UploadParameters {
    /// <summary>
    /// Creates a new set of parameters for the DataLake Uploader.
    /// </summary>
    /// <param name="inputFilePath">The full path to the file to be uploaded.</param>
    /// <param name="targetStreamPath">The full stream path where the file will be uploaded to.</param>
    /// <param name="accountName">Name of the account to upload to.</param>
    /// <param name="threadCount">(Optional) The maximum number of parallel threads to use for the upload.</param>
    /// <param name="isOverwrite">(Optional) Whether to overwrite the target stream or not.</param>
    /// <param name="isResume">(Optional) Indicates whether to resume a previously interrupted upload.</param>
    /// <param name="isBinary">(Optional) Indicates whether to treat the input file as a binary file (true), or whether to align upload blocks to record boundaries (false).</param>
    /// <param name="maxSegmentLength">Maximum length of each segment. The default is 256mb, which gives optimal performance. Modify at your own risk.</param>
    /// <param name="localMetadataLocation">(Optional) Indicates the directory path where to store the local upload metadata file while the upload is in progress. This location must be writeable from this application. Default location: SpecialFolder.LocalApplicationData.</param>
    /// <param name="fileEncoding">(Optional) Indicates the type of encoding the file was saved in and should be interpreted as having. The default is UTF-8.</param>
    /// <param name="delimiter">(Optional) Indicates the character delimter for record boundaries within the file, if any.This must be a single character. The default is new lines (\r, \n or \r\n).</param>
    public UploadParameters(String inputFilePath, String targetStreamPath, String accountName, int threadCount, boolean isOverwrite, boolean isResume, boolean isBinary, long maxSegmentLength, String localMetadataLocation, Charset fileEncoding, String delimiter) {
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
        if (fileEncoding == null) {
            this.setFileEncoding(StandardCharsets.UTF_8);
        } else {
            this.setFileEncoding(fileEncoding);
        }
    }

    /// <summary>
    /// Creates a new set of parameters for the DataLake Uploader.
    /// </summary>
    /// <param name="inputFilePath">The full path to the file to be uploaded.</param>
    /// <param name="targetStreamPath">The full stream path where the file will be uploaded to.</param>
    /// <param name="accountName">Name of the account to upload to.</param>
    /// <param name="useSegmentBlockBackOffRetryStrategy">if set to <c>true</c> [use segment block back off retry strategy].</param>
    /// <param name="threadCount">(Optional) The maximum number of parallel threads to use for the upload.</param>
    /// <param name="isOverwrite">(Optional) Whether to overwrite the target stream or not.</param>
    /// <param name="isResume">(Optional) Indicates whether to resume a previously interrupted upload.</param>
    /// <param name="isBinary">(Optional) Indicates whether to treat the input file as a binary file (true), or whether to align upload blocks to record boundaries (false).</param>
    /// <param name="localMetadataLocation">(Optional) Indicates the directory path where to store the local upload metadata file while the upload is in progress. This location must be writeable from this application. Default location: SpecialFolder.LocalApplicationData.</param>
    /// <param name="fileEncoding">(Optional) Indicates the type of encoding the file was saved in and should be interpreted as having. The default is UTF-8.</param>
    /// <param name="delimiter">(Optional) Indicates the character delimter for record boundaries within the file, if any.This must be a single character. The default is new lines (\r, \n or \r\n).</param>
    protected UploadParameters(String inputFilePath, String targetStreamPath, String accountName, boolean useSegmentBlockBackOffRetryStrategy, int threadCount, boolean isOverwrite, boolean isResume, boolean isBinary, long maxSegmentLength, String localMetadataLocation, Charset fileEncoding, String delimiter) {
        this(inputFilePath, targetStreamPath, accountName, threadCount, isOverwrite, isResume, isBinary, maxSegmentLength, localMetadataLocation, fileEncoding, delimiter);
        this.setUseSegmentBlockBackOffRetryStrategy(useSegmentBlockBackOffRetryStrategy);
    }

    public boolean isUseSegmentBlockBackOffRetryStrategy() {
        return useSegmentBlockBackOffRetryStrategy;
    }

    private void setUseSegmentBlockBackOffRetryStrategy(boolean useSegmentBlockBackOffRetryStrategy) {
        this.useSegmentBlockBackOffRetryStrategy = useSegmentBlockBackOffRetryStrategy;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    private void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public String getTargetStreamPath() {
        return targetStreamPath;
    }

    private void setTargetStreamPath(String targetStreamPath) {
        this.targetStreamPath = targetStreamPath;
    }

    public String getAccountName() {
        return accountName;
    }

    private void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    private void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isResume() {
        return resume;
    }

    private void setResume(boolean resume) {
        this.resume = resume;
    }

    public boolean isBinary() {
        return binary;
    }

    private void setBinary(boolean binary) {
        this.binary = binary;
    }

    public long getMaxSegementLength() {
        return maxSegementLength;
    }

    private void setMaxSegementLength(long maxSegementLength) {
        this.maxSegementLength = maxSegementLength;
    }

    public String getLocalMetadataLocation() {
        return localMetadataLocation;
    }

    private void setLocalMetadataLocation(String localMetadataLocation) {
        this.localMetadataLocation = localMetadataLocation;
    }

    public Charset getFileEncoding() {
        return fileEncoding;
    }

    private void setFileEncoding(Charset fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public String getDelimiter() {
        return delimiter;
    }

    private void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /// <summary>
    /// Gets a value indicating whether [to use segment block back off retry strategy].
    /// </summary>
    /// <value>
    /// <c>true</c> if [to use segment block back off retry strategy]; otherwise, <c>false</c>.
    /// </value>
    private boolean useSegmentBlockBackOffRetryStrategy;

    /// <summary>
    /// Gets a value indicating the full path to the file to be uploaded.
    /// </summary>
    /// <value>
    /// The input file path.
    /// </value>
    private String inputFilePath;

    /// <summary>
    /// Gets a value indicating the full stream path where the file will be uploaded to.
    /// </summary>
    /// <value>
    /// The target stream path.
    /// </value>
    private String targetStreamPath;

    /// <summary>
    /// Gets a value indicating the name of the account to upload to.
    /// </summary>
    /// <value>
    /// The name of the account.
    /// </value>
    private String accountName;

    /// <summary>
    /// Gets a value indicating the maximum number of parallel threads to use for the upload.
    /// </summary>
    /// <value>
    /// The thread count.
    /// </value>
    private int threadCount;

    /// <summary>
    /// Gets a value indicating whether to overwrite the target stream if it already exists.
    /// </summary>
    /// <value>
    /// <c>true</c> if this instance is overwrite; otherwise, <c>false</c>.
    /// </value>
    private boolean overwrite;

    /// <summary>
    /// Gets a value indicating whether to resume a previously interrupted upload.
    /// </summary>
    /// <value>
    ///   <c>true</c> if this instance is resume; otherwise, <c>false</c>.
    /// </value>
    private boolean resume;

    /// <summary>
    /// Gets a value indicating whether the input file should be treated as a binary (true) or a delimited input (false).
    /// </summary>
    /// <value>
    ///   <c>true</c> if this instance is binary; otherwise, <c>false</c>.
    /// </value>
    private boolean binary;

    /// <summary>
    /// Gets the maximum length of each segement.
    /// </summary>
    /// <value>
    /// The maximum length of each segement.
    /// </value>
    private long maxSegementLength;

    /// <summary>
    /// Gets a value indicating the directory path where to store the metadata for the upload.
    /// </summary>
    /// <value>
    /// The local metadata location.
    /// </value>
    private String localMetadataLocation;

    /// <summary>
    /// Gets a value indicating the encoding of the file being uploaded.
    /// </summary>
    /// <value>
    /// The file encoding.
    /// </value>
    private Charset fileEncoding;

    /// <summary>
    /// Gets a value indicating the record boundary delimiter for the file, if any.
    /// </summary>
    /// <value>
    /// The record boundary delimiter
    /// </value>
    private String delimiter;
}
