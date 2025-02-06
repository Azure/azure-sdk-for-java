// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.ShareFilePermission;

/**
 * Extended options that may be passed when setting properties of a directory.
 */
@Fluent
public class ShareDirectorySetPropertiesOptions {
    private FileSmbProperties smbProperties;
    private ShareFilePermission filePermissions;
    private FilePosixProperties posixProperties;

    /**
     * Creates a new instance of {@link ShareDirectorySetPropertiesOptions}.
     */
    public ShareDirectorySetPropertiesOptions() {
    }

    /**
     * Gets the optional SMB properties to set on the destination file or directory.
     *
     * @return Optional SMB properties to set on the destination file or directory. The only properties that are
     * considered are file attributes, file creation time, file last write time, and file permission key. The rest are
     * ignored.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }

    /**
     * Sets the optional SMB properties to set on the destination file or directory.
     *
     * @param smbProperties Optional SMB properties to set on the destination file or directory. The only properties
     * that are  considered are file attributes, file creation time, file last write time, and file permission key. The
     * rest are ignored.
     * @return The updated options.
     */
    public ShareDirectorySetPropertiesOptions setSmbProperties(FileSmbProperties smbProperties) {
        this.smbProperties = smbProperties;
        return this;
    }

    /**
     * Gets the {@link ShareFilePermission}.
     *
     * @return {@link ShareFilePermission}
     */
    public ShareFilePermission getFilePermissions() {
        return filePermissions;
    }

    /**
     * Sets the {@link ShareFilePermission}.
     *
     * @param filePermissions {@link ShareFilePermission}
     * @return The updated options.
     */
    public ShareDirectorySetPropertiesOptions setFilePermissions(ShareFilePermission filePermissions) {
        this.filePermissions = filePermissions;
        return this;
    }

    /**
     *  Optional properties to set on NFS directories.
     *  Note that this property is only applicable to directories created in NFS shares.
     *
     * @return {@link FilePosixProperties}
     */
    public FilePosixProperties getPosixProperties() {
        return posixProperties;
    }

    /**
     *  Optional properties to set on NFS directories.
     *  Note that this property is only applicable to directories created in NFS shares.
     *
     * @param posixProperties {@link FilePosixProperties}
     * @return The updated options.
     */
    public ShareDirectorySetPropertiesOptions setPosixProperties(FilePosixProperties posixProperties) {
        this.posixProperties = posixProperties;
        return this;
    }

}
