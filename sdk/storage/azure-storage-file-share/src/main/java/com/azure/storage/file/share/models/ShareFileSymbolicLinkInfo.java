// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import java.time.OffsetDateTime;

/**
 * Contains information about a symbolic link. Only applicable to NFS files.
 */
@Immutable
public class ShareFileSymbolicLinkInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final String linkText;

    /**
     * Creates an instance of information about a symbolic link
     *
     * @param eTag Entity tag that corresponds to the version of the symbolic link file.
     * @param lastModified Last time the symbolic link file was modified.
     * @param linkText The absolute or relative path of the symbolic link file.
     */
    public ShareFileSymbolicLinkInfo(String eTag, OffsetDateTime lastModified, String linkText) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.linkText = linkText;
    }

    /**
     * Gets the entity tag that corresponds to the version of the symbolic link file.
     *
     * @return The entity tag that corresponds to the version of the symbolic link file.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the last time the symbolic link file was modified.
     *
     * @return Gets the last time the symbolic link file was modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the absolute or relative path of the symbolic link file.
     *
     * @return Gets the absolute or relative path of the symbolic link file.
     */
    public String getLinkText() {
        return linkText;
    }
}
