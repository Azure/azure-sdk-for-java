// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;

import java.util.EnumSet;

/**
 * Contains file or directory reference information in the storage File service.
 */
@Immutable
public final class ShareFileItem {
    private final String name;
    private final boolean isDirectory;
    private final String id;
    private final ShareFileItemProperties properties;
    private final EnumSet<NtfsFileAttributes> fileAttributes;
    private final String permissionKey;
    private final Long fileSize;

    /**
     * Creates an instance of file or directory reference information about a specific Share.
     *
     * @param name Name of the file or the directory.
     * @param isDirectory A boolean set to true if the reference is a directory, false if the reference is a file.
     * @param fileSize Size of a file. Pass {@code null} if the reference is a directory.
     */
    public ShareFileItem(final String name, final boolean isDirectory, final Long fileSize) {
        this(name, isDirectory, null, null, null, null, fileSize);
    }

    /**
     * Creates an instance of file or directory reference information about a specific Share.
     *
     * @param name Name of the file or the directory.
     * @param isDirectory A boolean set to true if the reference is a directory, false if the reference is a file.
     * @param id ID of the file or directory.
     * @param properties Properties of the file or directory.
     * @param fileAttributes NTFS attributes of the file or directory.
     * @param permissionKey Permission key of the file or directory.
     * @param fileSize Size of a file. Pass {@code null} if the reference is a directory.
     */
    public ShareFileItem(final String name, final boolean isDirectory, String id, ShareFileItemProperties properties,
        EnumSet<NtfsFileAttributes> fileAttributes, String permissionKey, final Long fileSize) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.id = id;
        this.properties = properties;
        this.fileAttributes = fileAttributes;
        this.permissionKey = permissionKey;
        this.fileSize = fileSize;
    }

    /**
     * @return Name of the file or the directory.
     */
    public String getName() {
        return name;
    }

    /**
     * @return True if the reference is a directory, or false if the reference is a file.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * @return Size of a file, {@code null} if the reference is a directory.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * @return ID of the file or directory.
     */
    public String getId() {
        return id;
    }

    /**
     * @return Properties of the file or directory.
     */
    public ShareFileItemProperties getProperties() {
        return properties;
    }

    /**
     * @return NTFS attributes of the file or directory.
     */
    public EnumSet<NtfsFileAttributes> getFileAttributes() {
        return fileAttributes;
    }

    /**
     * @return Permission key of the file or directory.
     */
    public String getPermissionKey() {
        return permissionKey;
    }
}
