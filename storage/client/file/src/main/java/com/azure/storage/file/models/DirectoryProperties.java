// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contains properties information about a Directory in the storage File service.
 */
public final class DirectoryProperties {
    private final Map<String, String> metadata;
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final boolean isServerEncrypted;

    /**
     * Creates an instance of properties information about a specific Directory.
     *
     * @param metadata A set of name-value pairs that contain metadata for the directory.
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param isServerEncrypted  The value of this header is set to true if the directory metadata is completely encrypted using the specified algorithm. Otherwise, the value is set to false.
     */
    public DirectoryProperties(final Map<String, String> metadata, final String eTag, final OffsetDateTime lastModified, final boolean isServerEncrypted) {
        this.metadata = metadata;
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * @return A set of name-value pairs that contain metadata for the directory.
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * @return Entity tag that corresponds to the directory.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return Entity tag that corresponds to the directory.
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return The value of this header is true if the directory metadata is completely encrypted using the specified algorithm. Otherwise, the value is false.
     */
    public boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
