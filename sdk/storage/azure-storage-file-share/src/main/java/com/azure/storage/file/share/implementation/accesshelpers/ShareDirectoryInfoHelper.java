// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.ShareDirectoryInfo;

import java.time.OffsetDateTime;

/**
 * Helper class to access private values of {@link ShareDirectoryInfo} across package boundaries.
 */
public final class ShareDirectoryInfoHelper {

    private static ShareDirectoryInfoAccessor accessor;

    private ShareDirectoryInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link ShareDirectoryInfo} instance.
     */
    public interface ShareDirectoryInfoAccessor {
        /**
         * Creates a new instance of {@link ShareDirectoryInfo}.
         *
         * @param eTag Entity tag that corresponds to the directory.
         * @param lastModified Last time the directory was modified.
         * @param smbProperties The SMB properties of the directory.
         * @param posixProperties The NFS properties of the directory.
         * @return A new instance of {@link ShareDirectoryInfo}.
         */
        ShareDirectoryInfo create(String eTag, OffsetDateTime lastModified, FileSmbProperties smbProperties,
            FilePosixProperties posixProperties);
    }

    /**
     * The method called from {@link ShareDirectoryInfo} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ShareDirectoryInfoAccessor accessor) {
        ShareDirectoryInfoHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareDirectoryInfo}.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param smbProperties The SMB properties of the directory.
     * @param posixProperties the NFS properties of the directory.
     * @return A new instance of {@link ShareDirectoryInfo}.
     */
    public static ShareDirectoryInfo create(String eTag, OffsetDateTime lastModified, FileSmbProperties smbProperties,
        FilePosixProperties posixProperties) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses ShareDirectoryInfo which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareDirectoryInfo(null, null, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, smbProperties, posixProperties);
    }
}
