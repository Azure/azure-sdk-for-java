// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.ShareFileInfo;

import java.time.OffsetDateTime;

/**
 * Helper class to access private values of {@link ShareFileInfo} across package boundaries.
 */
public final class ShareFileInfoHelper {

    private static ShareFileInfoAccessor accessor;

    private ShareFileInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link ShareFileInfo} instance.
     */
    public interface ShareFileInfoAccessor {
        /**
         * Creates a new instance of {@link ShareFileInfo}.
         *
         * @param eTag Entity tag that corresponds to the directory.
         * @param lastModified Last time the directory was modified.
         * @param isServerEncrypted The value of this header is set to true if the directory metadata is completely
         * encrypted using the specified algorithm. Otherwise, the value is set to false.
         * @param smbProperties The SMB properties of the file.
         * @param posixProperties The NFS properties of the file.
         * @return A new instance of {@link ShareFileInfo}.
         */
        ShareFileInfo create(String eTag, OffsetDateTime lastModified, Boolean isServerEncrypted,
            FileSmbProperties smbProperties, FilePosixProperties posixProperties);
    }

    /**
     * The method called from {@link ShareFileInfo} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(ShareFileInfoAccessor accessor) {
        ShareFileInfoHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareFileInfo}.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param isServerEncrypted The value of this header is set to true if the directory metadata is completely
     * encrypted using the specified algorithm. Otherwise, the value is set to false.
     * @param smbProperties The SMB properties of the file.
     * @param posixProperties The NFS properties of the file.
     * @return A new instance of {@link ShareFileInfo}.
     */
    public static ShareFileInfo create(String eTag, OffsetDateTime lastModified, Boolean isServerEncrypted,
        FileSmbProperties smbProperties, FilePosixProperties posixProperties) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses ShareFileInfo which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareFileInfo(null, null, false, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, isServerEncrypted, smbProperties, posixProperties);
    }
}
