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
    private final LeaseStatusType leaseStatus;
    private final LeaseStateType leaseState;
    private final LeaseDurationType leaseDuration;
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
    public ShareFileProperties(String eTag, OffsetDateTime lastModified, Map<String, String> metadata, String fileType,
        Long contentLength, String contentType, byte[] contentMd5, String contentEncoding, String cacheControl,
        String contentDisposition, OffsetDateTime copyCompletionTime, String copyStatusDescription, String copyId,
        String copyProgress, String copySource, CopyStatusType copyStatus, Boolean isServerEncrypted,
        FileSmbProperties smbProperties) {
        this(eTag, lastModified, metadata, fileType, contentLength, contentType, contentMd5, contentEncoding,
            cacheControl, contentDisposition, null, null, null, copyCompletionTime, copyStatusDescription, copyId,
            copyProgress, copySource, copyStatus, isServerEncrypted, smbProperties);
    }

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
     * @param leaseStatusType Status of the lease.
     * @param leaseStateType State of the lease.
     * @param leaseDurationType How long the lease has left.
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
    public ShareFileProperties(String eTag, OffsetDateTime lastModified, Map<String, String> metadata, String fileType,
        Long contentLength, String contentType, byte[] contentMd5, String contentEncoding, String cacheControl,
        String contentDisposition, LeaseStatusType leaseStatusType, LeaseStateType leaseStateType,
        LeaseDurationType leaseDurationType, OffsetDateTime copyCompletionTime, String copyStatusDescription,
        String copyId, String copyProgress, String copySource, CopyStatusType copyStatus, Boolean isServerEncrypted,
        FileSmbProperties smbProperties) {
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
        this.leaseStatus = leaseStatusType;
        this.leaseState = leaseStateType;
        this.leaseDuration = leaseDurationType;
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
     * Gets the entity tag that corresponds to the directory.
     *
     * @return Entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the last time the directory was modified.
     *
     * @return Last time the directory was modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets a set of name-value pairs associated with this file as user-defined metadata.
     *
     * @return A set of name-value pairs associated with this file as user-defined metadata.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Gets the number of bytes present in the response body.
     *
     * @return The number of bytes present in the response body.
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     * Gets the type of the file.
     *
     * @return The type of the file.
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Gets the content type specified for the file. The default content type is application/octet-stream.
     *
     * @return The content type specified for the file. The default content type is application/octet-stream.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the MD5 hash of the file.
     *
     * @return The MD5 hash of the file.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Gets the value that was specified for the Content-Encoding request header.
     *
     * @return The value that was specified for the Content-Encoding request header.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Gets the value that was specified for the Content-Encoding request header.
     *
     * @return This header is returned if it was previously specified for the file.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Gets the value that was specified for the x-ms-content-disposition header and specifies how to process the
     * response.
     *
     * @return The value that was specified for the x-ms-content-disposition header and specifies how to process the
     * response.
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Gets the lease status of the file.
     *
     * @return the lease status of the file
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * Gets the lease state of the file.
     *
     * @return the lease state of the file
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * Gets the lease duration if the file is leased.
     *
     * @return the lease duration if the file is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Gets the conclusion time of the last attempted Copy File operation where this file was the destination file.
     *
     * @return Conclusion time of the last attempted Copy File operation where this file was the destination file.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * Gets the cause of fatal or non-fatal copy operation failure.
     *
     * @return When x-ms-copy-status is failed or pending. Describes cause of fatal or non-fatal copy operation failure.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * Gets the string identifier for the last attempted Copy File operation where this file was the destination file.
     *
     * @return String identifier for the last attempted Copy File operation where this file was the destination file.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * Gets the number of bytes copied and the total bytes in the source in the last attempted Copy File operation where
     * this file was the destination file.
     *
     * @return The number of bytes copied and the total bytes in the source in the last attempted Copy File operation
     * where this file was the destination file.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * Gets the source file used in the last attempted Copy File operation where this file was the destination file.
     *
     * @return URL up to 2KB in length that specifies the source file used in the last attempted Copy File operation
     * where this file was the destination file.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * Gets the state of the copy operation identified by x-ms-copy-id.
     *
     * @return State of the copy operation identified by x-ms-copy-id, with these values: - success: Copy completed
     * successfully. - pending: Copy is in progress. Check x-ms-copy-status-description if intermittent, non-fatal
     * errors impede copy progress but don't cause failure. - aborted: Copy was ended by Abort Copy File. - failed: Copy
     * failed. See x-ms-copy-status-description for failure details.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * Gets whether the file data and application metadata are completely encrypted using the specified algorithm.
     *
     * @return True if the file data and application metadata are completely encrypted using the specified algorithm.
     * Otherwise, return false.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * Gets the SMB properties of the file.
     *
     * @return The SMB properties of the file.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }
}
