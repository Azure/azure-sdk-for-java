// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.annotation.Fluent;
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
    private final Integer maxSingleUploadSize;

    /**
     * Creates a new {@link ParallelTransferOptions} with default parameters applied.
     *
     * @param blockSize The block size.
     * For upload, The block size is the size of each block that will be staged. This value also determines the number
     * of requests that need to be made. This parameter also determines the size that each buffer uses when buffering
     * is required and consequently amount of memory consumed by such methods may be up to blockSize * numBuffers.
     * For download to file, the block size is the size of each data chunk returned from the service.
     * For both applications, If block size is large, upload will make fewer network calls, but each
     * individual call will send more data and will therefore take longer.
     * @param numBuffers For buffered upload only, the number of buffers is the maximum number of buffers this method
     * should allocate. Memory will be allocated lazily as needed. Must be at least two. Typically, the larger the
     * number of buffers, the more parallel, and thus faster, the upload portion  of this operation will be.
     * The amount of memory consumed by methods using this value may be up to blockSize * numBuffers.
     * @param progressReceiver {@link ProgressReceiver}
     * @param maxSingleUploadSize If the size of the data is less than or equal to this value, it will be uploaded in a
     * single put rather than broken up into chunks. If the data is uploaded in a single shot, the block size will be
     * ignored. Some constraints to consider are that more requests cost more, but several small or mid-sized requests
     * may sometimes perform better. In the case of buffered upload, up to this amount of data may be buffered before
     * any data is sent. Must be greater than 0. May be null to accept default behavior, which is the maximum value the
     * service accepts for uploading in a single requests, which varies depending on the service.
     */
    public ParallelTransferOptions(Integer blockSize, Integer numBuffers, ProgressReceiver progressReceiver,
        Integer maxSingleUploadSize) {
        this.blockSize = blockSize;
        if (numBuffers != null) {
            StorageImplUtils.assertInBounds("numBuffers", numBuffers, 2, Integer.MAX_VALUE);
        }
        this.numBuffers = numBuffers;
        this.progressReceiver = progressReceiver;
        this.maxSingleUploadSize = maxSingleUploadSize;
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
     * @return The progress reporter
     */
    public ProgressReceiver getProgressReceiver() {
        return this.progressReceiver;
    }

    /**
     * Gets the value above which the upload will be broken into blocks and parallelized.
     * @return The threshold value.
     */
    public Integer getMaxSingleUploadSize() {
        return this.maxSingleUploadSize;
    }
}
