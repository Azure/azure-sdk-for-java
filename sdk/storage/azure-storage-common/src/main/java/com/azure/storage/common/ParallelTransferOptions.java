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

    private Long blockSize;
    private Integer maxConcurrency;
    private ProgressReceiver progressReceiver;
    private Long maxSingleUploadSize;

    /**
     * Creates a new {@link ParallelTransferOptions} with default parameters applied.
     */
    public ParallelTransferOptions() {
    }

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
     * @param maxConcurrency The maximum number of parallel requests that will be issued at any given time as a part of
     * a single parallel transfer. This value applies per api. For example, if two calls to uploadFromFile are made at
     * the same time, and each specifies a maxConcurrency of 5, there may be up to 10 outstanding, concurrent requests,
     * up to 5 for each of the upload operations. For buffered uploads only, the maximum number of buffers to be
     * allocated as part of the transfer will be {@code maxConcurrency + 1}. In those cases, memory will be allocated
     * lazily as needed. The amount of memory consumed by methods which buffer may be up to blockSize * maxConcurrency.
     * In general, upload methods which do not accept a length parameter must perform some buffering.
     * @param progressReceiver {@link ProgressReceiver}
     * @param maxSingleUploadSize If the size of the data is less than or equal to this value, it will be uploaded in a
     * single put rather than broken up into chunks. If the data is uploaded in a single shot, the block size will be
     * ignored. Some constraints to consider are that more requests cost more, but several small or mid-sized requests
     * may sometimes perform better. In the case of buffered upload, up to this amount of data may be buffered before
     * any data is sent. Must be greater than 0. May be null to accept default behavior, which is the maximum value the
     * service accepts for uploading in a single requests, which varies depending on the service.
     * @deprecated Use fluent interface to set properties instead.
     */
    @Deprecated
    public ParallelTransferOptions(Integer blockSize, Integer maxConcurrency, ProgressReceiver progressReceiver,
        Integer maxSingleUploadSize) {
        this.setBlockSizeLong(blockSize == null ? null : Long.valueOf(blockSize));
        this.setMaxConcurrency(maxConcurrency);
        this.setProgressReceiver(progressReceiver);
        this.setMaxSingleUploadSizeLong(maxSingleUploadSize == null ? null : Long.valueOf(maxSingleUploadSize));
    }

    /**
     * Gets the block size (chunk size) to transfer at a time.
     * @return The block size.
     * @deprecated Use {@link #getBlockSizeLong()}.
     */
    @Deprecated
    public Integer getBlockSize() {
        return this.blockSize == null ? null : Math.toIntExact(this.blockSize);
    }

    /**
     * Gets the block size (chunk size) to transfer at a time.
     * @return The block size.
     */
    public Long getBlockSizeLong() {
        return this.blockSize;
    }

    /**
     * Sets the block size.
     * For upload, The block size is the size of each block that will be staged. This value also determines the number
     * of requests that need to be made. This parameter also determines the size that each buffer uses when buffering
     * is required and consequently amount of memory consumed by such methods may be up to blockSize * numBuffers.
     * For download to file, the block size is the size of each data chunk returned from the service.
     * For both applications, If block size is large, upload will make fewer network calls, but each
     * individual call will send more data and will therefore take longer.
     *
     * @param blockSize The block size.
     * @return The ParallelTransferOptions object itself.
     */
    public ParallelTransferOptions setBlockSizeLong(Long blockSize) {
        if (blockSize != null) {
            StorageImplUtils.assertInBounds("blockSize", blockSize, 1, Long.MAX_VALUE);
        }
        this.blockSize = blockSize;
        return this;
    }

    /**
     * Gets the number of buffers being used for a transfer operation.
     * @return The number of buffers.
     * @deprecated Use {@link #getMaxConcurrency()}
     */
    @Deprecated
    public Integer getNumBuffers() {
        return this.maxConcurrency;
    }

    /**
     * Gets the Progress receiver for parallel reporting
     * @return The progress reporter
     */
    public ProgressReceiver getProgressReceiver() {
        return this.progressReceiver;
    }

    /**
     * Sets the {@link ProgressReceiver}.
     *
     * @param progressReceiver The {@link ProgressReceiver}.
     * @return The ParallelTransferOptions object itself.
     */
    public ParallelTransferOptions setProgressReceiver(ProgressReceiver progressReceiver) {
        this.progressReceiver = progressReceiver;
        return this;
    }

    /**
     * Gets the value above which the upload will be broken into blocks and parallelized.
     * @return The threshold value.
     * @deprecated Use {@link #getMaxSingleUploadSizeLong()}.
     */
    @Deprecated
    public Integer getMaxSingleUploadSize() {
        return this.maxSingleUploadSize == null ? null : Math.toIntExact(this.maxSingleUploadSize);
    }

    /**
     * Gets the value above which the upload will be broken into blocks and parallelized.
     * @return The threshold value.
     */
    public Long getMaxSingleUploadSizeLong() {
        return this.maxSingleUploadSize;
    }

    /**
     * If the size of the data is less than or equal to this value, it will be uploaded in a
     * single put rather than broken up into chunks. If the data is uploaded in a single shot, the block size will be
     * ignored. Some constraints to consider are that more requests cost more, but several small or mid-sized requests
     * may sometimes perform better. In the case of buffered upload, up to this amount of data may be buffered before
     * any data is sent. Must be greater than 0. May be null to accept default behavior, which is the maximum value the
     * service accepts for uploading in a single requests, which varies depending on the service.
     *
     * @param maxSingleUploadSize The threshold value.
     * @return The ParallelTransferOptions object itself.
     */
    public ParallelTransferOptions setMaxSingleUploadSizeLong(Long maxSingleUploadSize) {
        if (maxSingleUploadSize != null) {
            StorageImplUtils.assertInBounds("maxSingleUploadSize", maxSingleUploadSize, 1, Long.MAX_VALUE);
        }
        this.maxSingleUploadSize = maxSingleUploadSize;
        return this;
    }

   /**
     * Gets the maximum number of parallel requests that will be issued at any given time.
     * @return The max concurrency value.
     */
    public Integer getMaxConcurrency() {
        return this.maxConcurrency;
    }

    /**
     * @param maxConcurrency The maximum number of parallel requests that will be issued at any given time as a part of
     * a single parallel transfer. This value applies per api. For example, if two calls to uploadFromFile are made at
     * the same time, and each specifies a maxConcurrency of 5, there may be up to 10 outstanding, concurrent requests,
     * up to 5 for each of the upload operations. For buffered uploads only, the maximum number of buffers to be
     * allocated as part of the transfer will be {@code maxConcurrency + 1}. In those cases, memory will be allocated
     * lazily as needed. The amount of memory consumed by methods which buffer may be up to blockSize * maxConcurrency.
     * In general, upload methods which do not accept a length parameter must perform some buffering.
     * @return The ParallelTransferOptions object itself.
     */
    public ParallelTransferOptions setMaxConcurrency(Integer maxConcurrency) {
        if (maxConcurrency != null) {
            StorageImplUtils.assertInBounds("numBuffers", maxConcurrency, 1, Integer.MAX_VALUE);
        }
        this.maxConcurrency = maxConcurrency;
        return this;
    }
}
