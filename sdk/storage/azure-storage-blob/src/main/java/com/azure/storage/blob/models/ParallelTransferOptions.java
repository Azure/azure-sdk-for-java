// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * This class contains configuration used to parallelize data transfer operations. Note that not all values are used
 * by every method which accepts this type. Please refer to the javadoc on specific methods for these cases.
 */
@Fluent
public final class ParallelTransferOptions {

    private final Integer blockSize;
    private final Integer numBuffers;
    private final ProgressReceiver progressReceiver;

    /**
     * Creates a new {@link ParallelTransferOptions} with default parameters applied.
     *
     * @param blockSize The block size.
     * For upload, The block size is the size of each block that will be staged. This value also determines the number
     * of requests that need to be made. If block size is large, upload will make fewer network calls, but each
     * individual call will send more data and will therefore take longer. This parameter also determines the size
     * that each buffer uses when buffering is required and consequently amount of memory consumed by such methods may
     * be up to blockSize * numBuffers.
     * @param numBuffers For buffered upload only, the number of buffers is the maximum number of buffers this method
     * should allocate. Memory will be allocated lazily as needed. Must be at least two. Typically, the larger the
     * number of buffers, the more parallel, and thus faster, the upload portion  of this operation will be.
     * The amount of memory consumed by methods using this value may be up to blockSize * numBuffers.
     * @param progressReceiver {@link ProgressReceiver}
     */
    public ParallelTransferOptions(Integer blockSize, Integer numBuffers, ProgressReceiver progressReceiver) {
        if (blockSize != null) {
            StorageImplUtils.assertInBounds("blockSize", blockSize, 1, BlockBlobAsyncClient.MAX_STAGE_BLOCK_BYTES);
        }
        this.blockSize = blockSize;

        if (numBuffers != null) {
            StorageImplUtils.assertInBounds("numBuffers", numBuffers, 2, Integer.MAX_VALUE);
        }
        this.numBuffers = numBuffers;
        this.progressReceiver = progressReceiver;
    }

    /**
     * Gets the block size (chunk size) to transfer at a time.
     * @return The block size.
     */
    public Integer getBlockSize() {
        return this.blockSize;
    }

    /**
     * Gets the number of buffers being used for a transfer operation.
     * @return The number of buffers.
     */
    public Integer getNumBuffers() {
        return this.numBuffers;
    }

    /**
     * Gets the Progress receiver for parallel reporting
     * @return the progress reporter
     */
    public ProgressReceiver getProgressReceiver() {
        return this.progressReceiver;
    }
}
