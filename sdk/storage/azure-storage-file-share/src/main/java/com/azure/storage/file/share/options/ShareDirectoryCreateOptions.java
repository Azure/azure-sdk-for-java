// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.FileSmbProperties;

import java.util.Map;

/**
 * Extended options that may be passed when creating a share directory.
 */
@Fluent
public class ShareDirectoryCreateOptions {
    private FileSmbProperties smbProperties;
    private String filePermission;
    private Map<String, String> metadata;

    /**
     * Creates a new instance of {@link ShareDirectoryCreateOptions}.
     */
    public ShareDirectoryCreateOptions() {
    }

    /**
     * Gets the file permission key.
     *
     * @return The file's permission key.
     */
    public String getFilePermission() {
        return filePermission;
    }

    /**
     * Sets the file permission key.
     *
     * @param filePermissionKey The file permission key.
     * @return the updated options.
     */
    public ShareDirectoryCreateOptions setFilePermission(String filePermissionKey) {
        this.filePermission = filePermissionKey;
        return this;
    }

    /**
     * Gets the SMB properties to set on the destination directory.
     *
     * @return Optional SMB properties to set on the destination file or directory. The only properties that are
     * considered are file attributes, file creation time, file last write time, and file permission key. The rest are
     * ignored.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }

    /**
     * Sets the SMB properties to set on the destination directory.
     *
     * @param smbProperties Optional SMB properties to set on the destination file or directory. The only properties
     * that are  considered are file attributes, file creation time, file last write time, and file permission key. The
     * rest are ignored.
     * @return The updated options.
     */
    public ShareDirectoryCreateOptions setSmbProperties(FileSmbProperties smbProperties) {
        this.smbProperties = smbProperties;
        return this;
    }

    /**
     * Gets the metadata to associate with the directory.
     *
     * @return Metadata to associate with the directory.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata to associate with the directory.
     *
     * @param metadata Metadata to associate with the directory. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public ShareDirectoryCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
}
