// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.annotation.Immutable;

/**
 * This class contains the configuration that is used to parallelize data transfer operations.
 */
@Immutable
public abstract class StorageParallelTransferOptions {
    private final int numberOfBuffers;
    private final int bufferSize;

    /**
     * Creates a new {@link StorageParallelTransferOptions}.
     *
     * @param numberOfBuffers The number of buffers used to parallelize the data transfer operation.
     * @param bufferSize The size of each buffer in bytes.
     */
    public StorageParallelTransferOptions(int numberOfBuffers, int bufferSize) {
        this.numberOfBuffers = numberOfBuffers;
        this.bufferSize = bufferSize;
    }

    /**
     * @return The number of buffered used for a transfer operation.
     */
    public int getNumberOfBuffers() {
        return numberOfBuffers;
    }

    /**
     * @return The size of each buffer in bytes.
     */
    public int getBufferSize() {
        return bufferSize;
    }
}
