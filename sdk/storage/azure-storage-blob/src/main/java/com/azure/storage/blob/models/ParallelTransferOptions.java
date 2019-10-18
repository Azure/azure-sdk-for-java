// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.Utility;

import static com.azure.storage.blob.BlobAsyncClient.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
import static com.azure.storage.blob.BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;

public final class ParallelTransferOptions {

    private static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;

    private Integer blockSize;
    private Integer numBuffers;
    private ProgressReceiver progressReceiver;

    /**
     * Creates a new {@link ParallelTransferOptions} with default parameters applied.
     */
    public ParallelTransferOptions() {
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
    public ParallelTransferOptions setBlockSize(Integer blockSize) {
        if (blockSize != null) {
            Utility.assertInBounds("blockSize", blockSize, 0, BLOB_MAX_UPLOAD_BLOCK_SIZE);
        }
        this.blockSize = blockSize;
        return this;
    }

    /**
     * Sets the number of buffers being used for an upload/download operation.
     * @param numBuffers The number of buffers.
     * For buffered upload only, the number of buffers is the maximum number of buffers this method should allocate.
     * Must be at least two. Typically, the larger the number of buffers, the more parallel, and thus faster, the
     * upload portion  of this operation will be. The amount of memory consumed by this method may be up to
     * blockSize * numBuffers.
     * @return The updated ParallelTransferOptions object.
     * @throws IllegalArgumentException when numBuffers is less than 2.
     */
    public ParallelTransferOptions setNumBuffers(Integer numBuffers) {
        if (numBuffers != null) {
            Utility.assertInBounds("numBuffers", numBuffers, 2, Integer.MAX_VALUE);
        }
        this.numBuffers = numBuffers;
        return this;
    }

    /**
     * Sets the progress receiver for parallel reporting.
     * @param progressReceiver The progress receiver.
     * @return The updated ParallelTransferOptions object.
     */
    public ParallelTransferOptions setProgressReceiver(ProgressReceiver progressReceiver) {
        this.progressReceiver = progressReceiver;
        return this;
    }

    /**
     * RESERVED FOR INTERNAL USE.
     */
    public void populateAndApplyDefaults(ParallelTransferOptions other) {
        if (other == null) {
            other = new ParallelTransferOptions();
        }
        this.setBlockSize(other.getBlockSize() == null
            ? Integer.valueOf(BLOB_DEFAULT_UPLOAD_BLOCK_SIZE) : other.getBlockSize());
        this.setNumBuffers(other.getNumBuffers() == null
            ? Integer.valueOf(BLOB_DEFAULT_NUMBER_OF_BUFFERS) : other.getNumBuffers());
        this.setProgressReceiver(other.getProgressReceiver());
    }
}
