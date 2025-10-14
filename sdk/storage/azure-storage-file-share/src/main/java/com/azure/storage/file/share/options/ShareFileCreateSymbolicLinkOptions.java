// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ShareRequestConditions;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * NFS only. Options that may be passed when creating a symbolic link for a file.
 */
public class ShareFileCreateSymbolicLinkOptions {
    private Map<String, String> metadata;
    private OffsetDateTime fileCreationTime;
    private OffsetDateTime fileLastWriteTime;
    private String owner;
    private String group;
    private ShareRequestConditions requestConditions;
    private final String linkText;

    /**
     * Creates a new instance of {@link ShareFileCreateSymbolicLinkOptions}.
     * @param linkText The absolute or relative path of the file to be linked to.
     */
    public ShareFileCreateSymbolicLinkOptions(String linkText) {
        this.linkText = linkText;
    }

    /**
     * Optional custom metadata to set for the symbolic link.
     *
     * @return the custom metadata for the creation of the symbolic link.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Optional custom metadata to set for the symbolic link.
     *
     * @param metadata the custom metadata for the creation of the symbolic link.
     * @return the updated options.
     */
    public ShareFileCreateSymbolicLinkOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Optional creation time of the symbolic link.
     *
     * @return the creation time of the symbolic link.
     */
    public OffsetDateTime getFileCreationTime() {
        return fileCreationTime;
    }

    /**
     * Optional creation time of the symbolic link.
     *
     * @param fileCreationTime the creation time of the symbolic link.
     * @return the updated options.
     */
    public ShareFileCreateSymbolicLinkOptions setFileCreationTime(OffsetDateTime fileCreationTime) {
        this.fileCreationTime = fileCreationTime;
        return this;
    }

    /**
     * Optional last write time of the symbolic link.
     *
     * @return the last write time of the symbolic link.
     */
    public OffsetDateTime getFileLastWriteTime() {
        return fileLastWriteTime;
    }

    /**
     * Optional last write time of the symbolic link.
     *
     * @param fileLastWriteTime the last write time of the symbolic link.
     * @return the updated options.
     */
    public ShareFileCreateSymbolicLinkOptions setFileLastWriteTime(OffsetDateTime fileLastWriteTime) {
        this.fileLastWriteTime = fileLastWriteTime;
        return this;
    }

    /**
     * Optional. The owner user identifier (UID) to be set on the symbolic link. The default value is 0 (root).
     *
     * @return the owner for the creation of the symbolic link.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Optional. The owner user identifier (UID) to be set on the symbolic link. The default value is 0 (root).
     *
     * @param owner the owner for the creation of the symbolic link.
     * @return the updated options.
     */
    public ShareFileCreateSymbolicLinkOptions setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Optional. The owner group identifier (GID) to be set on the symbolic link. The default value is 0 (root group).
     *
     * @return the group for the creation of the symbolic link.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Optional. The owner group identifier (GID) to be set on the symbolic link. The default value is 0 (root group).
     *
     * @param group the group for the creation of the symbolic link.
     * @return the updated options.
     */
    public ShareFileCreateSymbolicLinkOptions setGroup(String group) {
        this.group = group;
        return this;
    }

    /**
     * Optional {@link ShareRequestConditions} to add conditions on creating the symbolic link.
     *
     * @return the {@link ShareRequestConditions} for the creation of the symbolic link.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Optional {@link ShareRequestConditions} to add conditions on creating the symbolic link.
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return the updated options.
     */
    public ShareFileCreateSymbolicLinkOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Required. The absolute or relative path of the file to be linked to.
     *
     * @return the absolute or relative path of the file to be linked to.
     */
    public String getLinkText() {
        return linkText;
    }
}
