// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines a file query error that can be returned on parsing a file query request.
 */
public class FileQueryProgress {

    private final long bytesScanned;
    private final long totalBytes;

    /**
     * Creates a new FileQueryProgress object.
     * @param bytesScanned The number of bytes scanned so far.
     * @param totalBytes The total number of bytes in the file.
     */
    public FileQueryProgress(long bytesScanned, long totalBytes) {
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
     * @return The total number of bytes in the file.
     */
    public long getTotalBytes() {
        return totalBytes;
    }

}
