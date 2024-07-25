// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareRequestConditions;


/**
 * Extended options that may be passed when setting properties of a file.
 */
@Fluent
public class ShareFileSetPropertiesOptions {
    private final long size;
    private ShareFileHttpHeaders httpHeaders;
    private FileSmbProperties smbProperties;
    private ShareFilePermission filePermissions;
    private ShareRequestConditions requestConditions;

    /**
     * @param size Specifies the size for the file share.
     */
    public ShareFileSetPropertiesOptions(long size) {
        this.size = size;
    }

    /**
     * @return Gets the size for the file share.
     */
    public long getSize() {
        return this.size;
    }

    /**
     * @return the file's http headers.
     */
    public ShareFileHttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * Sets the file's http headers.
     * @param httpHeaders the http headers.
     * @return the updated options.
     */
    public ShareFileSetPropertiesOptions setHttpHeaders(ShareFileHttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

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
    public ShareFileSetPropertiesOptions setSmbProperties(FileSmbProperties smbProperties) {
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
    public ShareFileSetPropertiesOptions setFilePermissions(ShareFilePermission filePermissions) {
        this.filePermissions = filePermissions;
        return this;
    }

    /**
     * Gets the {@link ShareRequestConditions}.
     *
     * @return {@link ShareRequestConditions}
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link ShareRequestConditions}.
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return The updated options.
     */
    public ShareFileSetPropertiesOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
