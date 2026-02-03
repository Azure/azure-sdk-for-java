// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.storage.file.share.implementation.accesshelpers.FilePosixPropertiesHelper;

/**
 * NFS properties.
 * Note that these properties only apply to files or directories in premium NFS file accounts.
 */
public final class FilePosixProperties {

    private String fileMode;
    private String owner;
    private String group;
    private final NfsFileType fileType;
    private final Long linkCount;

    /**
     * Default constructor
     */
    public FilePosixProperties() {
        // Non user-settable properties
        fileType = null;
        linkCount = null;
    }

    /**
     * Gets the file mode permissions.
     *
     * @return the file mode permissions.
     */
    public String getFileMode() {
        return fileMode;
    }

    /**
     * Gets the owner user identifier (UID).
     *
     * @return the owner user identifier (UID).
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the owner group identifier (GID).
     *
     * @return the owner group identifier (GID).
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the file type.
     *
     * @return the file type.
     */
    public NfsFileType getFileType() {
        return fileType;
    }

    /**
     * Gets the link count of the file or directory.
     *
     * @return the link count of the file or directory.
     */
    public Long getLinkCount() {
        return linkCount;
    }

    /**
     * Optional. Version 2025-05-05 and newer.
     * The mode permissions to be set on the file or directory. This can be in either symbolic or octal notation.
     *
     * <p>For more information, see this
     * <a href="https://aka.ms/fileModeWikipedia">Wikipedia page</a>.</p>
     *
     * @param fileMode The mode permissions to be set on the file or directory.
     * @return the FilePosixProperties object itself.
     */
    public FilePosixProperties setFileMode(String fileMode) {
        this.fileMode = fileMode;
        return this;
    }

    /**
     * Optional. The owner user identifier (UID) to be set on the file or directory. The default value is 0 (root).
     *
     * @param owner the owner user identifier (UID) to set.
     * @return the FilePosixProperties object itself.
     */
    public FilePosixProperties setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Optional. The owner group identifier (GID) to be set on the file or directory. The default value is 0 (root group).
     *
     * @param group the owner group identifier (GID) to set.
     * @return the FilePosixProperties object itself.
     */
    public FilePosixProperties setGroup(String group) {
        this.group = group;
        return this;
    }

    /**
     * Creates a new FilePosixProperties object from HttpHeaders.
     *
     * @param httpHeaders The headers to construct FilePosixProperties from.
     */
    FilePosixProperties(HttpHeaders httpHeaders) {
        String tempFileMode = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-mode"));
        String tempOwner = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-owner"));
        String tempGroup = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-group"));
        String tempFileType = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-file-file-type"));
        String tempLinkCount = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-link-count"));

        Long linkCountValue;
        try {
            linkCountValue = tempLinkCount == null ? null : Long.valueOf(tempLinkCount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Unable to convert value of header x-ms-link-count \"" + tempLinkCount + "\" to Long. ", e);
        }

        this.fileMode = tempFileMode;
        this.owner = tempOwner;
        this.group = tempGroup;
        this.fileType = NfsFileType.fromString(tempFileType);
        this.linkCount = linkCountValue;
    }

    static {
        FilePosixPropertiesHelper.setAccessor(FilePosixProperties::new);
    }
}
