// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.storage.file.share.FileSmbProperties;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contains property information about a File in the storage File service.
 */
@Immutable
public final class ShareFileProperties {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final Map<String, String> metadata;
    private final String fileType;
    private final Long contentLength;
    private final String contentType;
    private final byte[] contentMd5;
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
    private final FileSmbProperties smbProperties;

    /**
     * Creates an instance of property information about a specific File.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param metadata A set of name-value pairs associated with this file as user-defined metadata.
     * @param fileType Type of the file.
     * @param contentLength The number of bytes present in the response body.
     * @param contentType The content type specified for the file. The default content type is
     * application/octet-stream.
     * @param contentMd5 The MD5 hash of the file to check the message content integrity.
     * @param contentEncoding This header returns the value that was specified for the Content-Encoding request header.
     * @param cacheControl This header is returned if it was previously specified for the file.
     * @param contentDisposition The value that was specified for the x-ms-content-disposition header and specifies how
     * to process the response.
     * @param copyCompletionTime Conclusion time of the last attempted Copy File operation where this file was the
     * destination file.
     * @param copyStatusDescription Appears when x-ms-copy-status is failed or pending. Describes cause of fatal or
     * non-fatal copy operation failure.
     * @param copyId String identifier for the last attempted Copy File operation where this file was the destination
     * file.
     * @param copyProgress Contains the number of bytes copied and the total bytes in the source in the last attempted
     * Copy File operation where this file was the destination file.
     * @param copySource URL up to 2KB in length that specifies the source file used in the last attempted Copy File
     * operation where this file was the destination file.
     * @param copyStatus State of the copy operation identified by x-ms-copy-id, with these values:
     * <ul>
     * <li>success: Copy completed successfully</li>
     * <li>pending: Copy is in progress. Check x-ms-copy-status-description if intermittent, non-fatal errors impede
     * copy progress but don't cause failure.</li>
     * <li>aborted: Copy was ended by Abort Copy File.</li>
     * <li>failed: Copy failed. See x-ms-copy-status-description for failure details.</li>
     * </ul>
     * @param isServerEncrypted The value of this header is set to true if the file data and application metadata are
     * completely encrypted using the specified algorithm. Otherwise, the value is set to false.
     * @param smbProperties The SMB properties of the file.
     */
    public ShareFileProperties(final String eTag, final OffsetDateTime lastModified, final Map<String, String> metadata,
        final String fileType, final Long contentLength, final String contentType, final byte[] contentMd5,
        final String contentEncoding, final String cacheControl, final String contentDisposition,
        final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final String copyId,
        final String copyProgress, final String copySource, final CopyStatusType copyStatus,
        final Boolean isServerEncrypted, final FileSmbProperties smbProperties) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.metadata = metadata;
        this.fileType = fileType;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentMd5 = CoreUtils.clone(contentMd5);
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
        this.smbProperties = smbProperties;
    }

    /**
     * @return Entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return Last time the directory was modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return A set of name-value pairs associated with this file as user-defined metadata.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return The number of bytes present in the response body.
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     * @return The type of the file.
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * @return The content type specified for the file. The default content type is application/octet-stream.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return The MD5 hash of the file.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * @return The value that was specified for the Content-Encoding request header.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @return This header is returned if it was previously specified for the file.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * @return The value that was specified for the x-ms-content-disposition header and specifies how to process the
     * response.
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * @return Conclusion time of the last attempted Copy File operation where this file was the destination file.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * @return When x-ms-copy-status is failed or pending. Describes cause of fatal or non-fatal copy operation failure.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * @return String identifier for the last attempted Copy File operation where this file was the destination file.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * @return The number of bytes copied and the total bytes in the source in the last attempted Copy File operation
     * where this file was the destination file.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * @return URL up to 2KB in length that specifies the source file used in the last attempted Copy File operation
     * where this file was the destination file.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * @return State of the copy operation identified by x-ms-copy-id, with these values: - success: Copy completed
     * successfully. - pending: Copy is in progress. Check x-ms-copy-status-description if intermittent, non-fatal
     * errors impede copy progress but don't cause failure. - aborted: Copy was ended by Abort Copy File. - failed: Copy
     * failed. See x-ms-copy-status-description for failure details.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * @return True if the file data and application metadata are completely encrypted using the specified algorithm.
     * Otherwise, return false.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return The SMB properties of the file.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }
}
