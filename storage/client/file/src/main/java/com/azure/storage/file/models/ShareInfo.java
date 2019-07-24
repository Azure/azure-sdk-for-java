// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

/**
 * Contains information about a Share in the storage File service.
 */
public final class ShareInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;

    /**
     * Creates an instance of information about a specific Share.
     *
     * @param eTag Entity tag that corresponds to the share
     * @param lastModified Last time the share was modified
     */
    public ShareInfo(String eTag, OffsetDateTime lastModified) {
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    /**
     * @return the entity tag that corresponds to the share
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return the last time the share was modified
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }
}
