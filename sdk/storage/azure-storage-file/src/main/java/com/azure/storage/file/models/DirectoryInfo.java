// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.storage.file.FileSmbProperties;

import java.time.OffsetDateTime;

/**
 * Contains information about a Directory in the storage File service.
 */
public final class DirectoryInfo {
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
    public DirectoryInfo(final String eTag, final OffsetDateTime lastModified, final FileSmbProperties smbProperties) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.smbProperties = smbProperties;
    }

    /**
     * @return The entity tag that corresponds to the directory.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return The last time the share was modified.
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return The SMB Properties of the directory.
     */
    public FileSmbProperties smbProperties() {
        return smbProperties;
    }
}
