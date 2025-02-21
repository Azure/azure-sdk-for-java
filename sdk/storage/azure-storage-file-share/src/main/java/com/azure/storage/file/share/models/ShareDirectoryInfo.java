// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.file.share.FileSmbProperties;

import java.time.OffsetDateTime;

/**
 * Contains information about a Directory in the storage File service.
 */
@Immutable
public final class ShareDirectoryInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final FileSmbProperties smbProperties;

    /**
     * Creates an instance of information about a specific Directory.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param smbProperties The SMB properties of the directory.
     */
    public ShareDirectoryInfo(String eTag, OffsetDateTime lastModified, FileSmbProperties smbProperties) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.smbProperties = smbProperties;
    }

    /**
     * Gets the entity tag that corresponds to the directory.
     *
     * @return The entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the last time the directory was modified.
     *
     * @return The last time the share was modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the SMB properties of the directory.
     *
     * @return The SMB Properties of the directory.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }
}
