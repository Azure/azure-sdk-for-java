// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.Constants.ContentDownloader;
import com.azure.core.annotation.Fluent;

import java.util.Locale;

/**
 * This class contains configuration used to parallelize data transfer operations. Note that not all values are used
 * by every method which accepts this type. Please refer to the javadoc on specific methods for these cases.
 */
@Fluent
public final class ParallelDownloadOptions {
    private static final String PARAMETER_NOT_IN_RANGE = "The value of the parameter '%s' should be between %s and %s.";

    private Long blockSize;
    private Integer maxConcurrency;
    private ProgressReceiver progressReceiver;

    /**
     * Creates a new {@link ParallelDownloadOptions} with default parameters applied.
     */
    public ParallelDownloadOptions() {
    }

    /**
     * Gets the block size (chunk size) to transfer at a time.
     * @return The block size.
     */
    public long getBlockSize() {
        return blockSize == null ? ContentDownloader.DEFAULT_BUFFER_SIZE : blockSize;
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
     * @return The ParallelDownloadOptions object itself.
     */
    public ParallelDownloadOptions setBlockSize(Long blockSize) {
        if (blockSize != null) {
            assertInBounds("blockSize", blockSize, 1, Long.MAX_VALUE);
        }
        this.blockSize = blockSize;
        return this;
    }

    /**
     * Gets the Progress receiver for parallel reporting
     * @return The progress reporter
     */
    public ProgressReceiver getProgressReceiver() {
        return progressReceiver;
    }

    /**
     * Sets the {@link ProgressReceiver}.
     *
     * @param progressReceiver The {@link ProgressReceiver}.
     * @return The ParallelDownloadOptions object itself.
     */
    public ParallelDownloadOptions setProgressReceiver(ProgressReceiver progressReceiver) {
        this.progressReceiver = progressReceiver;
        return this;
    }

    /**
     * Gets the maximum number of parallel requests that will be issued at any given time.
     * @return The max concurrency value.
     */
    public int getMaxConcurrency() {
        return maxConcurrency == null ? ContentDownloader.DEFAULT_CONCURRENT_TRANSFERS_COUNT : maxConcurrency;
    }

    /**
     * @param maxConcurrency The maximum number of parallel requests that will be issued at any given time as a part of
     * a single parallel transfer. This value applies per api. For example, if two calls to uploadFromFile are made at
     * the same time, and each specifies a maxConcurrency of 5, there may be up to 10 outstanding, concurrent requests,
     * up to 5 for each of the upload operations. For buffered uploads only, the maximum number of buffers to be
     * allocated as part of the transfer will be {@code maxConcurrency + 1}. In those cases, memory will be allocated
     * lazily as needed. The amount of memory consumed by methods which buffer may be up to blockSize * maxConcurrency.
     * In general, upload methods which do not accept a length parameter must perform some buffering.
     * @return The ParallelDownloadOptions object itself.
     */
    public ParallelDownloadOptions setMaxConcurrency(Integer maxConcurrency) {
        if (maxConcurrency != null) {
            assertInBounds("numBuffers", maxConcurrency, 1, Integer.MAX_VALUE);
        }
        this.maxConcurrency = maxConcurrency;
        return this;
    }

    /**
     * Asserts that the specified number is in the valid range. The range is inclusive.
     *
     * @param param Name of the parameter
     * @param value Value of the parameter
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @throws IllegalArgumentException If {@code value} is less than {@code min} or {@code value} is greater than
     * {@code max}.
     */
    public static void assertInBounds(final String param, final long value, final long min, final long max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                PARAMETER_NOT_IN_RANGE, param, min, max));
        }
    }
}
