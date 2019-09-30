package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;

public class PathInfo {

    private final String eTag;
    private final OffsetDateTime lastModified;

    public PathInfo(String eTag, OffsetDateTime lastModified) {
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    public String getETag() {
        return eTag;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }
}
