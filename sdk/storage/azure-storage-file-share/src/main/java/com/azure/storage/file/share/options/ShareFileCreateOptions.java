// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareRequestConditions;
import java.util.Map;

/**
 * Extended options that may be passed when creating a share.
 */
@Fluent
public class ShareFileCreateOptions {

    private final long size;
    private ShareFileHttpHeaders httpHeaders;
    private FileSmbProperties smbProperties;
    private String filePermission;
    private Map<String, String> metadata;
    private ShareRequestConditions requestConditions;

    /**
     * @param size Specifies the maximum size for the file share.
     */
    public ShareFileCreateOptions(long size) {
        this.size = size;
    }

    /**
     * @return Specifies the maximum size for the file share.
     */
    public long getSize() {
        return this.size;
    }

    /**
     * @return the file's http headers.
     */
    public ShareFileHttpHeaders getShareFileHttpHeaders() {
        return httpHeaders;
    }

    /**
     * Sets the file's http headers.
     * @param headers the http headers.
     * @return the updated options.
     */
    public ShareFileCreateOptions setShareFileHttpHeaders(ShareFileHttpHeaders headers) {
        httpHeaders = headers;
        return this;
    }

    /**
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
    public ShareFileCreateOptions setFilePermission(String filePermissionKey) {
        this.filePermission = filePermissionKey;
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
    public ShareFileCreateOptions setSmbProperties(FileSmbProperties smbProperties) {
        this.smbProperties = smbProperties;
        return this;
    }

    /**
     * @return Metadata to associate with the share
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata Metadata to associate with the share. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public ShareFileCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
    public ShareFileCreateOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
