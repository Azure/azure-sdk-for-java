// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.StorageParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * This class contains configuration used to parallelize data transfer operations.
 */
@Immutable
public class BlobParallelTransferOptions extends StorageParallelTransferOptions {
    public static final int BLOB_DEFAULT_BUFFER_SIZE = 4 * Constants.MB;
    public static final int BLOB_MAX_BUFFER_SIZE = 100 * Constants.MB;

    public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = 8;

    private final ProgressReceiver progressReceiver;

    /**
     * Creates a new {@link BlobParallelTransferOptions} with default parameters applied. {@code numberOfBuffers} will
     * be set to {@link #BLOB_DEFAULT_NUMBER_OF_BUFFERS}, {@code bufferSize} will be set to {@link
     * #BLOB_DEFAULT_BUFFER_SIZE} and {@code progressReceiver} will be set to {@code null}.
     */
    public BlobParallelTransferOptions() {
        this(BLOB_DEFAULT_NUMBER_OF_BUFFERS, BLOB_DEFAULT_BUFFER_SIZE, null);
    }

    /**
     * Create a new {@link BlobParallelTransferOptions} with the specified number of buffers of the given size.
     *
     * <p>
     * The {@code numberOfBuffers} determines how parallelized the operation will be and the {@code bufferSize}
     * determines how many requests will be sent during the operation. A larger buffer count will allow the operation to
     * parallelize more while larger buffers reduce the number of request sent, the memory consumed may be up to
     * {@code numberOfBuffers} * {@code bufferSize}.
     * </p>
     *
     * @param numberOfBuffers The number of buffers used to parallelize the data transfer operation, if {@code null}
     * {@link #BLOB_DEFAULT_NUMBER_OF_BUFFERS} will be used.
     * @param bufferSize The size of each buffer in bytes, if {@code null} {@link #BLOB_DEFAULT_BUFFER_SIZE} will be
     * used.
     * @throws IllegalArgumentException If {@code numberOfBuffers} is less than {@code 2} or {@code bufferSize} is less
     * than {@code 0} or greater than {@link BlockBlobAsyncClient#MAX_STAGE_BLOCK_BYTES} ({@code 100MB}).
     */
    public BlobParallelTransferOptions(Integer numberOfBuffers, Integer bufferSize) {
        this(numberOfBuffers, bufferSize, null);
    }

    /**
     * Creates a new {@link BlobParallelTransferOptions} with the specified number of buffers of the given size and a
     * {@link ProgressReceiver} used to report the status of the transfer operation.
     *
     * <p>
     * The {@code numberOfBuffers} determines how parallelized the operation will be and the {@code bufferSize}
     * determines how many requests will be sent during the operation. A larger buffer count will allow the operation to
     * parallelize more while larger buffers reduce the number of request sent, the memory consumed may be up to
     * {@code numberOfBuffers} * {@code bufferSize}.
     * </p>
     *
     * @param numberOfBuffers The number of buffers used to parallelize the data transfer operation, if {@code null}
     * {@link #BLOB_DEFAULT_NUMBER_OF_BUFFERS} will be used.
     * @param bufferSize The size of each buffer in bytes, if {@code null} {@link #BLOB_DEFAULT_BUFFER_SIZE} will be
     * used.
     * @param progressReceiver The {@link ProgressReceiver} used to report the status of the transfer operation.
     * @throws IllegalArgumentException If {@code numberOfBuffers} is less than {@code 2} or {@code bufferSize} is less
     * than or equal {@code 0} or greater than {@link #BLOB_MAX_BUFFER_SIZE}.
     */
    public BlobParallelTransferOptions(Integer numberOfBuffers, Integer bufferSize, ProgressReceiver progressReceiver) {
        super(numberOfBuffers == null ? BLOB_DEFAULT_NUMBER_OF_BUFFERS : numberOfBuffers,
            bufferSize == null ? BLOB_DEFAULT_BUFFER_SIZE : bufferSize);
        StorageImplUtils.assertInBounds("numberOfBuffers", super.getNumberOfBuffers(), 2, Integer.MAX_VALUE);
        StorageImplUtils.assertInBounds("bufferSize", super.getBufferSize(), 0, BLOB_MAX_BUFFER_SIZE);
        this.progressReceiver = progressReceiver;
    }

    /**
     * Gets the {@link ProgressReceiver} used to report parallel transfer progress.
     *
     * @return the {@link ProgressReceiver} used to report progress.
     */
    public ProgressReceiver getProgressReceiver() {
        return this.progressReceiver;
    }
}
