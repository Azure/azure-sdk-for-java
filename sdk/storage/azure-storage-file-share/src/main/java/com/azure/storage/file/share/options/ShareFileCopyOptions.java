// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.util.Map;

/**
 * Extended options that may be passed when copying a share.
 */
@Fluent
public final class ShareFileCopyOptions {
    private FileSmbProperties smbProperties;
    private String filePermission;
    private FilePermissionFormat filePermissionFormat;
    private PermissionCopyModeType permissionCopyModeType;
    private Boolean ignoreReadOnly;
    private Boolean setArchiveAttribute;
    private Map<String, String> metadata;
    private ShareRequestConditions destinationRequestConditions;
    private CopyableFileSmbPropertiesList smbPropertiesToCopy;

    /**
     * Creates a new instance of {@link ShareFileCopyOptions}.
     */
    public ShareFileCopyOptions() {
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
    public ShareFileCopyOptions setFilePermission(String filePermissionKey) {
        this.filePermission = filePermissionKey;
        return this;
    }

    /**
     * Gets the SMB properties to set on the destination file.
     *
     * @return Optional SMB properties to set on the destination file or directory. The only properties that are
     * considered are file attributes, file creation time, file last write time, and file permission key. The rest are
     * ignored.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }

    /**
     * Sets the SMB properties to set on the destination file.
     *
     * @param smbProperties Optional SMB properties to set on the destination file or directory. The only properties
     * that are  considered are file attributes, file creation time, file last write time, and file permission key. The
     * rest are ignored.
     * @return The updated options.
     */
    public ShareFileCopyOptions setSmbProperties(FileSmbProperties smbProperties) {
        this.smbProperties = smbProperties;
        return this;
    }

    /**
     * Gets the option to copy file security descriptor from source file or to set it using the value which is defined
     * by the header value of FilePermission or FilePermissionKey.
     *
     * @return the option to copy file security descriptor from source file or to set it using the value which is
     * defined by the header value of FilePermission or FilePermissionKey.
     */
    public PermissionCopyModeType getPermissionCopyModeType() {
        return permissionCopyModeType;
    }

    /**
     * Sets the option to copy file security descriptor from source file or to set it using the value which is defined
     * by the header value of FilePermission or FilePermissionKey.
     *
     * @param copyModeType specified option to copy file security descriptor from source file or to set it using the
     * value which is defined by the header value of FilePermission or FilePermissionKey.
     * @return The updated options.
     */
    public ShareFileCopyOptions setPermissionCopyModeType(PermissionCopyModeType copyModeType) {
        permissionCopyModeType = copyModeType;
        return this;
    }

    /**
     * Gets the optional boolean specifying to overwrite the target file if it already exists and has read-only
     * attribute set.
     *
     * @return Optional boolean specifying to overwrite the target file if it already exists and has read-only attribute
     * set.
     */
    public Boolean isIgnoreReadOnly() {
        return ignoreReadOnly;
    }

    /**
     * Sets the optional boolean specifying to overwrite the target file if it already exists and has read-only
     * attribute set.
     *
     * @param ignoreReadOnly Optional boolean specifying to overwrite the target file if it already exists and has
     * read-only attribute set.
     * @return The updated options.
     */
    public ShareFileCopyOptions setIgnoreReadOnly(Boolean ignoreReadOnly) {
        this.ignoreReadOnly = ignoreReadOnly;
        return this;
    }

    /**
     * Gets the optional boolean specifying to set archive attribute on a target file. True means archive attribute will
     * be set on a target file despite attribute overrides or a source file state.
     *
     * @return Optional boolean specifying to set archive attribute on a target file. True means archive attribute will
     * be set on a target file despite attribute overrides or a source file state.
     */
    public Boolean isArchiveAttributeSet() {
        return setArchiveAttribute;
    }

    /**
     * Sets the optional boolean specifying to set archive attribute on a target file. True means archive attribute will
     * be set on a target file despite attribute overrides or a source file state.
     *
     * @param archiveAttribute Optional boolean Specifying to set archive attribute on a target file. True means archive
     * attribute will be set on a target file despite attribute overrides or a source file state.
     * @return The updated options.
     */
    public ShareFileCopyOptions setArchiveAttribute(Boolean archiveAttribute) {
        setArchiveAttribute = archiveAttribute;
        return this;
    }

    /**
     * Gets the metadata to associate with the file.
     *
     * @return Metadata to associate with the file.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata to associate with the file.
     *
     * @param metadata Metadata to associate with the file. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public ShareFileCopyOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the {@link ShareRequestConditions}.
     *
     * @return {@link ShareRequestConditions}
     */
    public ShareRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * Sets the {@link ShareRequestConditions} to add conditions on copying the file.
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return The updated options.
     */
    public ShareFileCopyOptions setDestinationRequestConditions(ShareRequestConditions requestConditions) {
        this.destinationRequestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the SMB properties to copy from the source file.
     *
     * @return SMB properties to copy from the source file.
     */
    public CopyableFileSmbPropertiesList getSmbPropertiesToCopy() {
        return smbPropertiesToCopy;
    }

    /**
     * Sets the SMB properties to copy from the source file.
     *
     * @param smbProperties list of SMB properties to copy from the source file.
     * @return The updated options.
     */
    public ShareFileCopyOptions setSmbPropertiesToCopy(CopyableFileSmbPropertiesList smbProperties) {
        smbPropertiesToCopy = smbProperties;
        return this;
    }

    /**
     * Gets the file permission format.
     *
     * @return file permission format.
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
    public ShareFileCopyOptions setFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        this.filePermissionFormat = filePermissionFormat;
        return this;
    }
}
