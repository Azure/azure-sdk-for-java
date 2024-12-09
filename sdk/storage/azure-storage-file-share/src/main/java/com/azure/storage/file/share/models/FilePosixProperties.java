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
public class FilePosixProperties {
    /**
     * Optional. Version TBD and newer. The mode permissions to be set on the file or directory. This can be in
     * either symbolic or octal notation.
     *
     * <p>For more information, see this
     * <a href="https://aka.ms/fileModeWikipedia">Wikipedia page</a>.</p>
     */
    private String fileMode;

    /**
     * Optional. The owner user identifier (UID) to be set on the file or directory. The default value is 0 (root).
     */
    private String owner;

    /**
     * Optional. The owner group identifier (GID) to be set on the file or directory. The default value is 0 (root group).
     */
    private String group;

    /**
     * Optional, only applicable to files. The type of the file. The default value is {@link NfsFileType#REGULAR}.
     */
    private NfsFileType fileType;

    /**
     * The link count of the file or directory.
     */
    private Long linkCount;

    /**
     * Creates an instance of FilePosixProperties class.
     */
    public FilePosixProperties() {
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
     * Sets the file mode permissions. This can be in either symbolic or octal notation.
     * <p>For more information, see this
     * <a href="https://aka.ms/fileModeWikipedia">Wikipedia page</a>.</p>
     *
     * @param fileMode the file mode permissions to set.
     * @return the FilePosixProperties object itself.
     */
    public FilePosixProperties setFileMode(String fileMode) {
        this.fileMode = fileMode;
        return this;
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
     * Sets the owner user identifier (UID).
     *
     * @param owner the owner user identifier (UID) to set.
     * @return the FilePosixProperties object itself.
     */
    public FilePosixProperties setOwner(String owner) {
        this.owner = owner;
        return this;
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
     * Sets the owner group identifier (GID).
     *
     * @param group the owner group identifier (GID) to set.
     * @return the FilePosixProperties object itself.
     */
    public FilePosixProperties setGroup(String group) {
        this.group = group;
        return this;
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
     * Creates a new FilePosixProperties object from HttpHeaders
     *
     * @param httpHeaders The headers to construct FileSmbProperties from
     */
    FilePosixProperties(HttpHeaders httpHeaders) {
        this.fileMode = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-mode"));
        this.owner = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-owner"));
        this.group = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-group"));
        this.fileType = NfsFileType.fromString(httpHeaders.getValue(HttpHeaderName.fromString("x-ms-file-file-type")));
        this.linkCount = httpHeaders.getValue(HttpHeaderName.fromString("x-ms-link-count")) == null
            ? null
            : Long.valueOf(httpHeaders.getValue(HttpHeaderName.fromString("x-ms-link-count")));
    }

    static {
        FilePosixPropertiesHelper.setAccessor(new FilePosixPropertiesHelper.FilePosixPropertiesAccessor() {
            @Override
            public FilePosixProperties create(HttpHeaders httpHeaders) {
                return new FilePosixProperties(httpHeaders);
            }
        });
    }
}
