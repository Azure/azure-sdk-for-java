package com.azure.storage.blob.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Constants;
import com.azure.storage.common.Utility;

public class ParallelTransferOptions {
    private final ClientLogger logger = new ClientLogger(ParallelTransferOptions.class);

    static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    static final int BLOB_MAX_BLOCK_SIZE = 100 * Constants.MB;

    static final int BLOB_DEFAULT_NUMBER_OF_PARALLEL_TRANSFERS = 2;

    private int blockSize;
    private int parallelTransfers;

    /**
     * Creates a new {@link ParallelTransferOptions} with default parameters applied
     */
    public ParallelTransferOptions() {
        this.blockSize = BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        this.parallelTransfers = BLOB_DEFAULT_NUMBER_OF_PARALLEL_TRANSFERS;
    }

    /**
     * Gets the block size or the size of a chunk to transfer at a time.
     * @return The block size.
     */
    public int getBlockSize() {
        return this.blockSize;
    }

    /**
     * Gets the number of parallel transfers being used for an upload/download operation.
     * @return The number of parallel transfers.
     */
    public int getParallelTransfers() {
        return this.parallelTransfers;
    }

    /**
     * Sets the block size or the size of a chunk to transfer at a time.
     * @param blockSize The block size.
     * @return The updated ParallelTransferOptions object.
     */
    public ParallelTransferOptions setBlockSize(int blockSize) {
        Utility.assertInBounds("blockSize", blockSize, 0, BLOB_MAX_BLOCK_SIZE);
        this.blockSize = blockSize;
        return this;
    }

    /**
     * Sets the number of parallel transfers being used for an upload/download operation.
     * @param parallelTransfers The number of parallel transfers.
     * @return The updated ParallelTransferOptions object.
     */
    public ParallelTransferOptions setParallelTransfers(int parallelTransfers) {
        Utility.assertInBounds("parallelTransfers", parallelTransfers, 2, Integer.MAX_VALUE);
        this.parallelTransfers = parallelTransfers;
        return this;
    }
}
