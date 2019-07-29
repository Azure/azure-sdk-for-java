// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

/**
 * Contains information about a File in the storage File service.
 */
public final class FileInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final Boolean isServerEncrypted;

    /**
     * Creates an instance of information about a specific Directory.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param isServerEncrypted The value of this header is set to true if the directory metadata is completely encrypted using the specified algorithm. Otherwise, the value is set to false.
     */
    public FileInfo(final String eTag, final OffsetDateTime lastModified, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
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
     * @return The value of this header is true if the directory metadata is completely encrypted using the specified algorithm. Otherwise, the value is false.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
