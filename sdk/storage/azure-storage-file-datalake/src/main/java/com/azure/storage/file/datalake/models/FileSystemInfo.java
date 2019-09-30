package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;

public class FileSystemInfo {

    private final String eTag;
    private final OffsetDateTime lastModified;
    private final boolean hierarchicalNamespaceEnabled;

    public FileSystemInfo(String eTag, OffsetDateTime lastModified, boolean hierarchicalNamespaceEnabled) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.hierarchicalNamespaceEnabled = hierarchicalNamespaceEnabled;
    }

    public String getETag() {
        return eTag;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    // TODO (gapra) : Is this the right name for a boolean getter in our guidelines?
    public boolean isHierarchicalNamespaceEnabled() {
        return hierarchicalNamespaceEnabled;
    }

}
