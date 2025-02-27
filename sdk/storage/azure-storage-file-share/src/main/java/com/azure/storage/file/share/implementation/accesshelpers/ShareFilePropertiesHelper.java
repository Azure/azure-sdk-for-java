// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.CopyStatusType;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.LeaseDurationType;
import com.azure.storage.file.share.models.LeaseStateType;
import com.azure.storage.file.share.models.LeaseStatusType;
import com.azure.storage.file.share.models.ShareFileProperties;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Helper class to access private values of {@link ShareFileProperties} across package boundaries.
 */
public final class ShareFilePropertiesHelper {
    private static ShareFilePropertiesAccessor accessor;

    private ShareFilePropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link ShareFileProperties} instance.
     */
    public interface ShareFilePropertiesAccessor {
        /**
         * Creates a new instance of {@link ShareFileProperties}.
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
         * @param posixProperties The NFS properties of the file.
         * @return A new instance of {@link ShareFileProperties}.
         */
        ShareFileProperties create(String eTag, OffsetDateTime lastModified, Map<String, String> metadata,
            String fileType, Long contentLength, String contentType, byte[] contentMd5, String contentEncoding,
            String cacheControl, String contentDisposition, LeaseStatusType leaseStatusType,
            LeaseStateType leaseStateType, LeaseDurationType leaseDurationType, OffsetDateTime copyCompletionTime,
            String copyStatusDescription, String copyId, String copyProgress, String copySource,
            CopyStatusType copyStatus, Boolean isServerEncrypted, FileSmbProperties smbProperties,
            FilePosixProperties posixProperties);
    }

    /**
     * The method called from {@link ShareFileProperties} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(ShareFilePropertiesAccessor accessor) {
        ShareFilePropertiesHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareFileProperties}.
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
     * @param posixProperties The NFS properties of the file.
     * @return A new instance of {@link ShareFileProperties}.
     */
    public static ShareFileProperties create(String eTag, OffsetDateTime lastModified, Map<String, String> metadata,
        String fileType, Long contentLength, String contentType, byte[] contentMd5, String contentEncoding,
        String cacheControl, String contentDisposition, LeaseStatusType leaseStatusType, LeaseStateType leaseStateType,
        LeaseDurationType leaseDurationType, OffsetDateTime copyCompletionTime, String copyStatusDescription,
        String copyId, String copyProgress, String copySource, CopyStatusType copyStatus, Boolean isServerEncrypted,
        FileSmbProperties smbProperties, FilePosixProperties posixProperties) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses ShareFileProperties which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareFileProperties(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, metadata, fileType, contentLength, contentType, contentMd5,
            contentEncoding, cacheControl, contentDisposition, leaseStatusType, leaseStateType, leaseDurationType,
            copyCompletionTime, copyStatusDescription, copyId, copyProgress, copySource, copyStatus, isServerEncrypted,
            smbProperties, posixProperties);
    }
}
