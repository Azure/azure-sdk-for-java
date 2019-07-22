// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

/**
 * Contains information about a Directory in the storage File service.
 */
public final class DirectoryInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;

    /**
     * Creates an instance of information about a specific Directory.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     */
    public DirectoryInfo(final String eTag, final OffsetDateTime lastModified) {
        this.eTag = eTag;
        this.lastModified = lastModified;
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
}
