// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.util.Map;

/**
 * Extended options that may be passed when renaming a file or directory.
 */
@Fluent
public final class ShareFileRenameOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ShareFileRenameOptions.class);

    private final String destinationPath;
    private Boolean replaceIfExists;
    private Boolean ignoreReadOnly;
    private ShareRequestConditions sourceRequestConditions;
    private ShareRequestConditions destinationRequestConditions;
    private String filePermission;
    private FileSmbProperties smbProperties;
    private Map<String, String> metadata;
    private String contentType;
    private FilePermissionFormat filePermissionFormat;

    /**
     * Creates a {@code ShareFileRenameOptions} object.
     *
     * @param destinationPath Relative path from the share to rename the file to.
     * @throws NullPointerException If {@code destinationPath} is null.
     */
    public ShareFileRenameOptions(String destinationPath) {
        StorageImplUtils.assertNotNull("destinationPath", destinationPath);
        this.destinationPath = destinationPath;
    }

    /**
     * Gets the path to which the file should be renamed.
     *
     * @return The path to which the file should be renamed.
     */
    public String getDestinationPath() {
        return destinationPath;
    }

    /**
     * Gets a boolean value which, if the destination file already exists, determines whether this request will
     * overwrite the file or not.
     *
     * @return A boolean value which, if the destination file already exists, determines whether this
     * request will overwrite the file or not. If true, the rename will succeed and will overwrite the destination file.
     * If not provided or if false and the destination file does exist, the request will not overwrite the destination
     * file. If provided and the destination file doesn’t exist, the rename will succeed.
     */
    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    /**
     * Sets a boolean value which, if the destination file already exists, determines whether this request will
     * overwrite the file or not.
     *
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
     * Gets a boolean value that specifies whether the ReadOnly attribute on a preexisting destination file should be
     * respected.
     *
     * @return A boolean value that specifies whether the ReadOnly attribute on a preexisting destination file should be
     * respected. If true, the rename will succeed, otherwise, a previous file at the destination with the ReadOnly
     * attribute set will cause the rename to fail.
     */
    public Boolean isIgnoreReadOnly() {
        return ignoreReadOnly;
    }

    /**
     * Sets a boolean value that specifies whether the ReadOnly attribute on a preexisting destination file should be
     * respected.
     *
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
     * Gets the source request conditions. This parameter is only applicable if the source is a file.
     *
     * @return Source request conditions. This parameter is only applicable if the source is a file.
     */
    public ShareRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * Sets the source request conditions. This parameter is only applicable if the source is a file.
     *
     * @param sourceRequestConditions Source request conditions. This parameter is only applicable if the source is a
     * file.
     * @return The updated options.
     */
    public ShareFileRenameOptions setSourceRequestConditions(ShareRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * Gets the destination request conditions.
     *
     * @return The destination request conditions.
     */
    public ShareRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * Sets the destination request conditions.
     *
     * @param destinationRequestConditions The destination request conditions.
     * @return The updated options.
     */
    public ShareFileRenameOptions setDestinationRequestConditions(ShareRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * Gets the optional file permission to set on the destination file or directory. The value in SmbProperties will be
     * ignored.
     *
     * @return Optional file permission to set on the destination file or directory. The value in SmbProperties will be
     * ignored.
     */
    public String getFilePermission() {
        return filePermission;
    }

    /**
     * Sets the optional file permission to set on the destination file or directory. The value in SmbProperties will be
     * ignored.
     *
     * @param filePermission Optional file permission to set on the destination file or directory. The value in
     * SmbProperties will be ignored.
     * @return The updated options.
     */
    public ShareFileRenameOptions setFilePermission(String filePermission) {
        this.filePermission = filePermission;
        return this;
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
    public ShareFileRenameOptions setSmbProperties(FileSmbProperties smbProperties) {
        this.smbProperties = smbProperties;
        return this;
    }

    /**
     * Gets the metadata to associate with the renamed file.
     *
     * @return The metadata to associate with the renamed file.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Sets the metadata to associate with the renamed file.
     *
     * @param metadata The metadata to associate with the renamed file.
     * @return The updated options.
     */
    public ShareFileRenameOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the content type.
     *
     * @return The content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the content type.
     * @return The updated options.
     */
    public ShareFileRenameOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the file permission format.
     *
     * @return The file permission format.
     */
    public FilePermissionFormat getFilePermissionFormat() {
        return filePermissionFormat;
    }

    /**
     * Sets the file permission format.
     *
     * @param filePermissionFormat the file permission format.
     * @return The updated options.
     */
    public ShareFileRenameOptions setFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        this.filePermissionFormat = filePermissionFormat;
        return this;
    }
}
