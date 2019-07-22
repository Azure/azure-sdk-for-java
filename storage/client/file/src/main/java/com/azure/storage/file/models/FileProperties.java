// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.implementation.util.ImplUtils;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contains property information about a File in the storage File service.
 */
public final class FileProperties {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final Map<String, String> metadata;
    private final String fileType;
    private final Long contentLength;
    private final String contentType;
    private final byte[] contentMD5;
    private final String contentEncoding;
    private final String cacheControl;
    private final String contentDisposition;
    private final OffsetDateTime copyCompletionTime;
    private final String copyStatusDescription;
    private final String copyId;
    private final String copyProgress;
    private final String copySource;
    private final CopyStatusType copyStatus;
    private final Boolean isServerEncrypted;

    /**
     * Creates an instance of property information about a specific File.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param metadata A set of name-value pairs associated with this file as user-defined metadata.
     * @param fileType Type of the file.
     * @param contentLength The number of bytes present in the response body.
     * @param contentType The content type specified for the file. The default content type is application/octet-stream.
     * @param contentMD5 The MD5 hash of the file to check the message content integrity.
     * @param contentEncoding This header returns the value that was specified for the Content-Encoding request header.
     * @param cacheControl This header is returned if it was previously specified for the file.
     * @param contentDisposition The value that was specified for the x-ms-content-disposition header and specifies how to process the response.
     * @param copyCompletionTime Conclusion time of the last attempted Copy File operation where this file was the destination file.
     * @param copyStatusDescription Appears when x-ms-copy-status is failed or pending. Describes cause of fatal or non-fatal copy operation failure.
     * @param copyId String identifier for the last attempted Copy File operation where this file was the destination file.
     * @param copyProgress Contains the number of bytes copied and the total bytes in the source in the last attempted Copy File operation where this file was the destination file.
     * @param copySource  URL up to 2KB in length that specifies the source file used in the last attempted Copy File operation where this file was the destination file.
     * @param copyStatus State of the copy operation identified by x-ms-copy-id, with these values:
     *                       - success: Copy completed successfully.
     *                       - pending: Copy is in progress. Check x-ms-copy-status-description if intermittent, non-fatal errors impede copy progress but don't cause failure.
     *                       - aborted: Copy was ended by Abort Copy File.
     *                       - failed: Copy failed. See x-ms-copy-status-description for failure details.
     * @param isServerEncrypted The value of this header is set to true if the file data and application metadata are completely encrypted using the specified algorithm. Otherwise, the value is set to false.
     */
    public FileProperties(final String eTag, final OffsetDateTime lastModified, final Map<String, String> metadata,
                          final String fileType, final Long contentLength, final String contentType, final byte[] contentMD5,
                          final String contentEncoding, final String cacheControl, final String contentDisposition,
                          final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final String copyId,
                          final String copyProgress, final String copySource, final CopyStatusType copyStatus, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.metadata = metadata;
        this.fileType = fileType;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentMD5 = ImplUtils.clone(contentMD5);
        this.contentEncoding = contentEncoding;
        this.cacheControl = cacheControl;
        this.contentDisposition = contentDisposition;
        this.copyCompletionTime = copyCompletionTime;
        this.copyStatusDescription = copyStatusDescription;
        this.copyId = copyId;
        this.copyProgress = copyProgress;
        this.copySource = copySource;
        this.copyStatus = copyStatus;
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * @return Entity tag that corresponds to the directory.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return Last time the directory was modified.
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return A set of name-value pairs associated with this file as user-defined metadata.
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * @return The number of bytes present in the response body.
     */
    public Long contentLength() {
        return contentLength;
    }

    /**
     * @return The type of the file.
     */
    public String fileType() {
        return fileType;
    }
    /**
     * @return The content type specified for the file. The default content type is application/octet-stream.
     */
    public String contentType() {
        return contentType;
    }

    /**
     * @return The MD5 hash of the file.
     */
    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return The value that was specified for the Content-Encoding request header.
     */
    public String contentEncoding() {
        return contentEncoding;
    }

    /**
     * @return This header is returned if it was previously specified for the file.
     */
    public String cacheControl() {
        return cacheControl;
    }

    /**
     * @return The value that was specified for the x-ms-content-disposition header and specifies how to process the response.
     */
    public String contentDisposition() {
        return contentDisposition;
    }

    /**
     * @return Conclusion time of the last attempted Copy File operation where this file was the destination file.
     */
    public OffsetDateTime copyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * @return When x-ms-copy-status is failed or pending. Describes cause of fatal or non-fatal copy operation failure.
     */
    public String copyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * @return String identifier for the last attempted Copy File operation where this file was the destination file.
     */
    public String copyId() {
        return copyId;
    }

    /**
     * @return The number of bytes copied and the total bytes in the source in the last attempted Copy File operation where this file was the destination file.
     */
    public String copyProgress() {
        return copyProgress;
    }

    /**
     * @return URL up to 2KB in length that specifies the source file used in the last attempted Copy File operation where this file was the destination file.
     */
    public String copySource() {
        return copySource;
    }

    /**
     * @return State of the copy operation identified by x-ms-copy-id, with these values:
     *                       - success: Copy completed successfully.
     *                       - pending: Copy is in progress. Check x-ms-copy-status-description if intermittent, non-fatal errors impede copy progress but don't cause failure.
     *                       - aborted: Copy was ended by Abort Copy File.
     *                       - failed: Copy failed. See x-ms-copy-status-description for failure details.
     */
    public CopyStatusType copyStatus() {
        return copyStatus;
    }

    /**
     * @return True if the file data and application metadata are completely encrypted using the specified algorithm. Otherwise, return false.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
