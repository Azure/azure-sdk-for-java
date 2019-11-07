// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;

/**
 * {@code PathInfo} contains basic information about a path that is returned by the service after certain
 * operations.
 */
public class PathInfo {

    private final String eTag;
    private final OffsetDateTime lastModified;

    /**
     * Constructs a {@link PathInfo}
     * @param eTag ETag of the path.
     * @param lastModified Datetime when the path was last modified.
     */
    public PathInfo(String eTag, OffsetDateTime lastModified) {
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    /**
     * Get the eTag property: The eTag property.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Get the last modified property: The last modified property.
     *
     * @return the time when the file was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }
}
