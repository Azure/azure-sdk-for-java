// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.util.Map;

/**
 * Extended options that may be passed when copying a share.
 */
@Fluent
public class ShareFileCopyOptions {

    private FileSmbProperties smbProperties;
    private String filePermission;
    private PermissionCopyModeType permissionCopyModeType;
    private Boolean ignoreReadOnly;
    private Boolean setArchiveAttribute;
    private Map<String, String> metadata;
    private ShareRequestConditions destinationRequestConditions;
    private CopyableFileSmbPropertiesList smbPropertiesToCopy;

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
    public ShareFileCopyOptions setFilePermission(String filePermissionKey) {
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
    public ShareFileCopyOptions setSmbProperties(FileSmbProperties smbProperties) {
        this.smbProperties = smbProperties;
        return this;
    }

    /**
     * @return the option to copy file security descriptor from source file or to set it using the value which is
     * defined by the header value of FilePermission or FilePermissionKey.
     */
    public PermissionCopyModeType getPermissionCopyModeType() {
        return permissionCopyModeType;
    }

    /**
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
     * @return Optional boolean specifying to overwrite the target file if it already exists and has read-only attribute set.
     */
    public Boolean isIgnoreReadOnly() {
        return ignoreReadOnly;
    }

    /**
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
     * @return Optional boolean Specifying to set archive attribute on a target file. True means archive attribute will
     * be set on a target file despite attribute overrides or a source file state.
     */
    public Boolean getSetArchiveAttribute() {
        return setArchiveAttribute;
    }

    /**
     *
     * @param archiveAttribute Optional boolean Specifying to set archive attribute on a target file. True means archive
     * attribute will be set on a target file despite attribute overrides or a source file state.
     * @return The updated options.
     */
    public ShareFileCopyOptions setSetArchiveAttribute(Boolean archiveAttribute) {
        setArchiveAttribute = archiveAttribute;
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
     * @return SMB properties to copy from the source file.
     */
    public CopyableFileSmbPropertiesList getSmbPropertiesToCopy() {
        return smbPropertiesToCopy;
    }

    /**
     * @param smbProperties list of SMB properties to copy from the source file.
     * @return The updated options.
     */
    public ShareFileCopyOptions setSmbPropertiesToCopy(CopyableFileSmbPropertiesList smbProperties) {
        smbPropertiesToCopy = smbProperties;
        return this;
    }
}
