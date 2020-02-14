package com.azure.storage.blob.quickquery.models;

public class BlobQuickQueryEnd {

    private long totalBytes;

    public BlobQuickQueryEnd(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}
