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
public class ShareFilePropertiesHelper {
    private static ShareFilePropertiesAccessor accessor;

    private ShareFilePropertiesHelper() {
    }

    /**
     * Interface defining the methods that access non-public APIs of a {@link ShareFileProperties} instance.
     */
    public interface ShareFilePropertiesAccessor {
        /**
         * Creates a new instance of {@link ShareFileProperties} .
         *
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
     * The method called from the static initializer of {@link ShareFileProperties} to set it's accessor.
     *
     * @param accessor The {@link ShareFileProperties} accessor.
     */
    public static void setAccessor(ShareFilePropertiesAccessor accessor) {
        ShareFilePropertiesHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareFileProperties}.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param smbProperties The SMB properties of the directory.
     * @param posixProperties the POSIX properties of the directory.
     * @return A new instance of {@link ShareFileProperties}.
     */
    public static ShareFileProperties create(String eTag, OffsetDateTime lastModified, Map<String, String> metadata,
        String fileType, Long contentLength, String contentType, byte[] contentMd5, String contentEncoding,
        String cacheControl, String contentDisposition, LeaseStatusType leaseStatusType, LeaseStateType leaseStateType,
        LeaseDurationType leaseDurationType, OffsetDateTime copyCompletionTime, String copyStatusDescription,
        String copyId, String copyProgress, String copySource, CopyStatusType copyStatus, Boolean isServerEncrypted,
        FileSmbProperties smbProperties, FilePosixProperties posixProperties) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
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
