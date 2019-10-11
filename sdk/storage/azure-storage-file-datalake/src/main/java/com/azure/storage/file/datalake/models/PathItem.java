// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.models.PathCreateHeaders;

import java.time.OffsetDateTime;

/**
 * {@code PathItem} contains basic information about a path item that is returned by the service after certain
 * operations.
 */
public class PathItem {
    private String eTag;
    private OffsetDateTime lastModifiedTime;

    public PathItem(PathCreateHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModifiedTime = generatedHeaders.getLastModified();
    }

    /**
     * @return the eTag of the path object.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the last modified time of the path object.
     */
    public OffsetDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }
}
