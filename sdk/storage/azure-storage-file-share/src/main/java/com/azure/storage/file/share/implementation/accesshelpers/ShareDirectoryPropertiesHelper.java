// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.ShareDirectoryProperties;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Helper class to access private values of {@link ShareDirectoryProperties} across package boundaries.
 */
public class ShareDirectoryPropertiesHelper {

    private static ShareDirectoryPropertiesAccessor accessor;

    private ShareDirectoryPropertiesHelper() {
    }

    /**
     * Interface defining the methods that access non-public APIs of a {@link ShareDirectoryProperties} instance.
     */
    public interface ShareDirectoryPropertiesAccessor {
        /**
         * Creates a new instance of {@link ShareDirectoryProperties} .
         *
         * @return A new instance of {@link ShareDirectoryProperties}.
         */
        ShareDirectoryProperties create(Map<String, String> metadata, String eTag, OffsetDateTime lastModified,
            boolean isServerEncrypted, FileSmbProperties smbProperties, FilePosixProperties posixProperties);
    }

    /**
     * The method called from the static initializer of {@link ShareDirectoryProperties} to set it's accessor.
     *
     * @param accessor The {@link ShareDirectoryProperties} accessor.
     */
    public static void setAccessor(ShareDirectoryPropertiesAccessor accessor) {
        ShareDirectoryPropertiesHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareDirectoryProperties}.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param smbProperties The SMB properties of the directory.
     * @param posixProperties the POSIX properties of the directory.
     * @return A new instance of {@link ShareDirectoryProperties}.
     */
    public static ShareDirectoryProperties create(Map<String, String> metadata, String eTag,
        OffsetDateTime lastModified, boolean isServerEncrypted, FileSmbProperties smbProperties,
        FilePosixProperties posixProperties) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses BlobDownloadHeaders which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareDirectoryProperties(null, null, null, false, null);
        }

        assert accessor != null;
        return accessor.create(metadata, eTag, lastModified, isServerEncrypted, smbProperties, posixProperties);
    }
}
