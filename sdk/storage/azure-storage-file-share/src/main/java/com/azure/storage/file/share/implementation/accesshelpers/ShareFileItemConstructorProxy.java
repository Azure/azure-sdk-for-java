// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.models.NfsFileType;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareFileItemProperties;

import java.util.EnumSet;

/**
 * Helper class to access private values of {@link ShareFileItem} across package boundaries.
 */
public final class ShareFileItemConstructorProxy {
    private static ShareFileItemConstructorAccessor accessor;

    private ShareFileItemConstructorProxy() {
    }

    /**
     * Type defining the methods to create a {@link ShareFileItem} instance with non-public properties.
     */
    public interface ShareFileItemConstructorAccessor {
        /**
         * Creates a new instance of {@link ShareFileItem}.
         *
         * @param name Name of the file or the directory.
         * @param isDirectory A boolean set to true if the reference is a directory, false if the reference is a file.
         * @param id ID of the file or directory.
         * @param properties Properties of the file or directory.
         * @param fileAttributes NTFS attributes of the file or directory.
         * @param permissionKey Permission key of the file or directory.
         * @param fileSize Size of a file.
         * @param linkCount The number of hard links to the file or directory.
         * @param fileType The type of the file or directory.
         * @param linkText The target path of the symbolic link.
         * @param deviceMajor The major device number.
         * @param deviceMinor The minor device number.
         * @return A new instance of {@link ShareFileItem}.
         */
        ShareFileItem create(String name, boolean isDirectory, String id, ShareFileItemProperties properties,
            EnumSet<NtfsFileAttributes> fileAttributes, String permissionKey, Long fileSize, Long linkCount,
            NfsFileType fileType, String linkText, Long deviceMajor, Long deviceMinor);
    }

    /**
     * The method called from {@link ShareFileItem} to set its accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final ShareFileItemConstructorAccessor accessor) {
        ShareFileItemConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareFileItem}.
     *
     * @param name Name of the file or the directory.
     * @param isDirectory A boolean set to true if the reference is a directory, false if the reference is a file.
     * @param id ID of the file or directory.
     * @param properties Properties of the file or directory.
     * @param fileAttributes NTFS attributes of the file or directory.
     * @param permissionKey Permission key of the file or directory.
     * @param fileSize Size of a file.
     * @param linkCount The number of hard links to the file or directory.
     * @param fileType The type of the file or directory.
     * @param linkText The target path of the symbolic link.
     * @param deviceMajor The major device number.
     * @param deviceMinor The minor device number.
     * @return A new instance of {@link ShareFileItem}.
     */
    public static ShareFileItem create(String name, boolean isDirectory, String id, ShareFileItemProperties properties,
        EnumSet<NtfsFileAttributes> fileAttributes, String permissionKey, Long fileSize, Long linkCount,
        NfsFileType fileType, String linkText, Long deviceMajor, Long deviceMinor) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses ShareFileItem which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareFileItem(null, false, null);
        }

        assert accessor != null;
        if (accessor == null) {
            throw new IllegalStateException("ShareFileItemConstructorAccessor is not configured.");
        }

        return accessor.create(name, isDirectory, id, properties, fileAttributes, permissionKey, fileSize, linkCount,
            fileType, linkText, deviceMajor, deviceMinor);
    }
}
