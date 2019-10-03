// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.common.Constants;
import com.azure.storage.common.Utility;

public class ParallelTransferOptions {

    private static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_BLOCK_SIZE = 100 * Constants.MB;

    private static final int BLOB_DEFAULT_NUMBER_OF_PARALLEL_TRANSFERS = 8;

    private int blockSize;
    private int numBuffers;

    /**
     * Creates a new {@link ParallelTransferOptions} with default parameters applied.
     * blockSize = 4MB
     * numBuffers = 8
     */
    public ParallelTransferOptions() {
        this.blockSize = BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        this.numBuffers = BLOB_DEFAULT_NUMBER_OF_PARALLEL_TRANSFERS;
    }

    /**
     * Gets the block size (chunk size) to transfer at a time.
     * @return The block size.
     */
    public int getBlockSize() {
        return this.blockSize;
    }

    /**
     * Gets the number of buffers being used for a transfer operation.
     * @return The number of buffers.
     */
    public int getNumBuffers() {
        return this.numBuffers;
    }

    /**
     * Sets the block size or the size of a chunk to transfer at a time.
     * @param blockSize The block size.
     * For upload, The block size is the size of each block that will be staged. This value also determines the size
     * that each buffer used by this method will be and determines the number of requests that need to be made. The
     * amount of memory consumed by this method may be up to blockSize * numBuffers. If block size is large, upload
     * will make fewer network calls, but each individual call will send more data and will therefore take longer.
     * @return The updated ParallelTransferOptions object.
     * @throws IllegalArgumentException when block size is less than 0 or greater than max blob block size (10MB).
     */
    public ParallelTransferOptions setBlockSize(int blockSize) {
        Utility.assertInBounds("blockSize", blockSize, 0, BLOB_MAX_BLOCK_SIZE);
        this.blockSize = blockSize;
        return this;
    }

    /**
     * Sets the number of buffers being used for an upload/download operation.
     * @param numBuffers The number of buffers.
     * For upload, The number of buffers is the maximum number of buffers this method should allocate.
     * Must be at least two. Typically, the larger the number of buffers, the more parallel, and thus faster, the
     * upload portion  of this operation will be. The amount of memory consumed by this method may be up to
     * blockSize * numBuffers.
     * @return The updated ParallelTransferOptions object.
     * @throws IllegalArgumentException when numBuffers is less than 2.
     */
    public ParallelTransferOptions setNumBuffers(int numBuffers) {
        Utility.assertInBounds("numBuffers", numBuffers, 2, Integer.MAX_VALUE);
        this.numBuffers = numBuffers;
        return this;
    }
}
