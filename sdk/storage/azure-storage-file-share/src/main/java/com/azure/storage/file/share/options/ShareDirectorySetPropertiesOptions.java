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
    private FilePosixProperties nfsProperties;

    /**
     * @return Optional SMB properties to set on the destination file or directory. The only properties that are
     * considered are file attributes, file creation time, file last write time, and file permission key. The rest are
     * ignored.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }

    /**
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
     *  Optional properties to set on NFS files.
     *  Note that this property is only applicable to files created in NFS shares.
     *
     * @return The NFS file properties.
     */
    public FilePosixProperties getNfsProperties() {
        return nfsProperties;
    }

    /**
     *  Optional properties to set on NFS files.
     *  Note that this property is only applicable to files created in NFS shares.
     *
     * @param nfsProperties the file permission format.
     * @return The updated options.
     */
    public ShareDirectorySetPropertiesOptions setNfsProperties(FilePosixProperties nfsProperties) {
        this.nfsProperties = nfsProperties;
        return this;
    }

}
