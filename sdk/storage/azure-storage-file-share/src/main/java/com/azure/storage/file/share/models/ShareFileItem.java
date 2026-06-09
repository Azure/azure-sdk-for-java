// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.file.share.implementation.accesshelpers.ShareFileItemConstructorProxy;

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
    private final Long linkCount;
    private final NfsFileType fileType;
    private final String linkText;
    private final Long deviceMajor;
    private final Long deviceMinor;

    static {
        ShareFileItemConstructorProxy.setAccessor(ShareFileItem::new);
    }

    /**
     * Creates an instance of file or directory reference information about a specific Share.
     *
     * @param name Name of the file or the directory.
     * @param isDirectory A boolean set to true if the reference is a directory, false if the reference is a file.
     * @param fileSize Size of a file. Pass {@code null} if the reference is a directory.
     */
    public ShareFileItem(String name, boolean isDirectory, Long fileSize) {
        this(name, isDirectory, null, null, null, null, fileSize, null, null, null, null, null);
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
    public ShareFileItem(String name, boolean isDirectory, String id, ShareFileItemProperties properties,
        EnumSet<NtfsFileAttributes> fileAttributes, String permissionKey, Long fileSize) {
        this(name, isDirectory, id, properties, fileAttributes, permissionKey, fileSize, null, null, null, null, null);
    }

    private ShareFileItem(String name, boolean isDirectory, String id, ShareFileItemProperties properties,
        EnumSet<NtfsFileAttributes> fileAttributes, String permissionKey, Long fileSize, Long linkCount,
        NfsFileType fileType, String linkText, Long deviceMajor, Long deviceMinor) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.id = id;
        this.properties = properties;
        this.fileAttributes = fileAttributes;
        this.permissionKey = permissionKey;
        this.fileSize = fileSize;
        this.linkCount = linkCount;
        this.fileType = fileType;
        this.linkText = linkText;
        this.deviceMajor = deviceMajor;
        this.deviceMinor = deviceMinor;
    }

    /**
     * Gets the name of the file or the directory.
     *
     * @return Name of the file or the directory.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a boolean set to true if the reference is a directory, false if the reference is a file.
     *
     * @return True if the reference is a directory, or false if the reference is a file.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Gets the size of a file, {@code null} if the reference is a directory.
     *
     * @return Size of a file, {@code null} if the reference is a directory.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the ID of the file or directory.
     *
     * @return ID of the file or directory.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the properties of the file or directory.
     *
     * @return Properties of the file or directory.
     */
    public ShareFileItemProperties getProperties() {
        return properties;
    }

    /**
     * Gets the NTFS attributes of the file or directory.
     *
     * @return NTFS attributes of the file or directory.
     */
    public EnumSet<NtfsFileAttributes> getFileAttributes() {
        return fileAttributes;
    }

    /**
     * Gets the permission key of the file or directory.
     *
     * @return Permission key of the file or directory.
     */
    public String getPermissionKey() {
        return permissionKey;
    }

    /**
     * Gets the number of hard links to the file or directory.
     *
     * @return The number of hard links to the file or directory.
     */
    public Long getLinkCount() {
        return linkCount;
    }

    /**
     * Gets the type of the file or directory.
     *
     * @return The type of the file or directory.
     */
    public NfsFileType getFileType() {
        return fileType;
    }

    /**
     * Gets the target path of the symbolic link.
     *
     * @return The target path of the symbolic link.
     */
    public String getLinkText() {
        return linkText;
    }

    /**
     * Gets the major device number.
     *
     * @return The major device number.
     */
    public Long getDeviceMajor() {
        return deviceMajor;
    }

    /**
     * Gets the minor device number.
     *
     * @return The minor device number.
     */
    public Long getDeviceMinor() {
        return deviceMinor;
    }
}
