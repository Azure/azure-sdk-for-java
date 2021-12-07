// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.models.CopyFileSmbInfo;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when renaming a file or directory.
 */
@Fluent
public class ShareFileRenameOptions {

    private final String destinationPath;
    private Boolean replaceIfExists;
    private Boolean ignoreReadOnly;
    private ShareRequestConditions sourceRequestConditions;
    private ShareRequestConditions destinationRequestConditions;
    private String filePermission;
    private String filePermissionKey;
    private CopyFileSmbInfo smbInfo;
    private ShareFileHttpHeaders httpHeaders;

    /**
     * Creates a {@code ShareFileRenameOptions} object.
     */
    public ShareFileRenameOptions(String destinationPath) {
        StorageImplUtils.assertNotNull("destinationPath", destinationPath);
        this.destinationPath = destinationPath;
    }

    /**
     * @return The path to which the file should be renamed.
     */
    public String getDestinationPath() {
        return destinationPath;
    }

    /**
     * @return A boolean value which, if the destination file already exists, determines whether this
     * request will overwrite the file or not. If true, the rename will succeed and will overwrite the destination file.
     * If not provided or if false and the destination file does exist, the request will not overwrite the destination
     * file. If provided and the destination file doesn’t exist, the rename will succeed.
     */
    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    /**
     * @param replaceIfExists A boolean value which, if the destination file already exists, determines whether this
     * request will overwrite the file or not. If true, the rename will succeed and will overwrite the destination file.
     * If not provided or if false and the destination file does exist, the request will not overwrite the destination
     * file. If provided and the destination file doesn’t exist, the rename will succeed.
     * @return The updated options.
     */
    public ShareFileRenameOptions setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
        return this;
    }

    /**
     * @return A boolean value that specifies whether the ReadOnly attribute on a preexisting destination file should be
     * respected. If true, the rename will succeed, otherwise, a previous file at the destination with the ReadOnly
     * attribute set will cause the rename to fail.
     */
    public Boolean getIgnoreReadOnly() {
        return ignoreReadOnly;
    }

    /**
     * @param ignoreReadOnly A boolean value that specifies whether the ReadOnly attribute on a preexisting destination
     * file should be respected. If true, the rename will succeed, otherwise, a previous file at the destination with
     * the ReadOnly attribute set will cause the rename to fail.
     * @return The updated options.
     */
    public ShareFileRenameOptions setIgnoreReadOnly(Boolean ignoreReadOnly) {
        this.ignoreReadOnly = ignoreReadOnly;
        return this;
    }

    /**
     * @return Source request conditions. This parameter is only applicable if the source is a file.
     */
    public ShareRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * @param sourceRequestConditions Source request conditions. This parameter is only applicable if the source is a
     * file.
     * @return The updated options.
     */
    public ShareFileRenameOptions setSourceRequestConditions(ShareRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * @return The destination request conditions.
     */
    public ShareRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * @param destinationRequestConditions The destination request conditions.
     * @return The updated options.
     */
    public ShareFileRenameOptions setDestinationRequestConditions(ShareRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * @return Optional file permission to set on the destination file or directory.
     */
    public String getFilePermission() {
        return filePermission;
    }

    /**
     * @param filePermission Optional file permission to set on the destination file or directory.
     * @return The updated options.
     */
    public ShareFileRenameOptions setFilePermission(String filePermission) {
        this.filePermission = filePermission;
        return this;
    }

    /**
     * @return
     */
    public String getFilePermissionKey() {
        return filePermissionKey;
    }

    /**
     * @param
     * @return The updated options.
     */
    public ShareFileRenameOptions setFilePermissionKey(String filePermissionKey) {
        this.filePermissionKey = filePermissionKey;
        return this;
    }

    /**
     * @return
     */
    // TODO: Wrapper class or move to another package
    public CopyFileSmbInfo getSmbInfo() {
        return smbInfo;
    }

    /**
     * @param
     * @return The updated options.
     */
    public ShareFileRenameOptions setSmbInfo(CopyFileSmbInfo smbInfo) {
        this.smbInfo = smbInfo;
        return this;
    }

    /**
     * @return
     */
    public ShareFileHttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * @param
     * @return The updated options.
     */
    public ShareFileRenameOptions setHttpHeaders(ShareFileHttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }
}
