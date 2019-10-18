// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.models.PathCreateHeaders;
import com.azure.storage.file.datalake.implementation.models.PathFlushDataHeaders;
import com.azure.storage.file.datalake.implementation.models.PathSetAccessControlHeaders;

import java.time.OffsetDateTime;

public class PathInfo {

    private final String eTag;
    private final OffsetDateTime lastModified;

    public PathInfo(String eTag, OffsetDateTime lastModified) {
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    public PathInfo(PathCreateHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
    }

    public PathInfo(PathFlushDataHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
    }

    public PathInfo(PathSetAccessControlHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
    }

    public String getETag() {
        return eTag;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }
}
