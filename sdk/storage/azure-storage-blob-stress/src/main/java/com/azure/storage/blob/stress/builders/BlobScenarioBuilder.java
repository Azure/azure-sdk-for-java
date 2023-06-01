package com.azure.storage.blob.stress.builders;

import com.azure.storage.stress.StressScenarioBuilder;

public abstract class BlobScenarioBuilder extends StressScenarioBuilder {
    private String blobPrefix;
    private long blobSize;

    public String getBlobPrefix() {
        return blobPrefix;
    }

    public void setBlobPrefix(String blobPrefix) {
        this.blobPrefix = blobPrefix;
    }

    public long getBlobSize() {
        return blobSize;
    }

    public void setBlobSize(long blobSize) {
        this.blobSize = blobSize;
    }
}
