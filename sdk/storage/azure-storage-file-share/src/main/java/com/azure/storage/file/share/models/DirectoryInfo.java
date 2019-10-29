// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.file.share.ShareFileSmbProperties;

import java.time.OffsetDateTime;

/**
 * Contains information about a Directory in the storage File service.
 */
@Immutable
public final class DirectoryInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final ShareFileSmbProperties smbProperties;

    /**
     * Creates an instance of information about a specific Directory.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param smbProperties The SMB properties of the directory.
     */
    public DirectoryInfo(final String eTag, final OffsetDateTime lastModified,
                         final ShareFileSmbProperties smbProperties) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.smbProperties = smbProperties;
    }

    /**
     * @return The entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return The last time the share was modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return The SMB Properties of the directory.
     */
    public ShareFileSmbProperties getSmbProperties() {
        return smbProperties;
    }
}
