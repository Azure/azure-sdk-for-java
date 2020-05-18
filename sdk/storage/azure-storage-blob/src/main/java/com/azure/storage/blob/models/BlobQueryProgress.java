// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines a blob query error that can be returned on parsing a blob query request.
 */
public class BlobQueryProgress {

    private final long bytesScanned;
    private final long totalBytes;

    /**
     * Creates a new BlobQueryProgress object.
     * @param bytesScanned The number of bytes scanned so far.
     * @param totalBytes The total number of bytes in the blob.
     */
    public BlobQueryProgress(long bytesScanned, long totalBytes) {
        this.bytesScanned = bytesScanned;
        this.totalBytes = totalBytes;
    }

    /**
     * @return The number of bytes scanned so far.
     */
    public long getBytesScanned() {
        return bytesScanned;
    }

    /**
     * @return The total number of bytes in the blob.
     */
    public long getTotalBytes() {
        return totalBytes;
    }

}
