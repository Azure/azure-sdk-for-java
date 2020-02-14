package com.azure.storage.blob.quickquery.models;

public class BlobQuickQueryProgress {

    private long bytesScanned;
    private long totalBytes;

    public BlobQuickQueryProgress(long bytesScanned, long totalBytes) {
        this.bytesScanned = bytesScanned;
        this.totalBytes = totalBytes;
    }

    public long getBytesScanned() {
        return bytesScanned;
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}
