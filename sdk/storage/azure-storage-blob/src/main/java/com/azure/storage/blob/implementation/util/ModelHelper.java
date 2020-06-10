// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class provides helper methods for common model patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class ModelHelper {

    /**
     * Indicates the default size above which the upload will be broken into blocks and parallelized.
     */
    private static final long BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE = 256L * Constants.MB;

    /**
     * Determines whether or not the passed authority is IP style, that is, it is of the format {@code <host>:<port>}.
     *
     * @param authority The authority of a URL.
     * @throws MalformedURLException If the authority is malformed.
     * @return Whether the authority is IP style.
     */
    public static boolean determineAuthorityIsIpStyle(String authority) throws MalformedURLException {
        return new URL("http://" +  authority).getPort() != -1;
    }

    /**
     * Fills in default values for a ParallelTransferOptions where no value has been set. This will construct a new
     * object for safety.
     *
     * @param other The options to fill in defaults.
     * @return An object with defaults filled in for null values in the original.
     */
    public static ParallelTransferOptions populateAndApplyDefaults(ParallelTransferOptions other) {
        other = other == null ? new ParallelTransferOptions() : other;

        Long blockSize = other.getBlockSizeLong();
        if (blockSize == null) {
            blockSize = (long) BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        }

        Integer maxConcurrency = other.getMaxConcurrency();
        if (maxConcurrency == null) {
            maxConcurrency = BlobAsyncClient.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
        }

        Long maxSingleUploadSize = other.getMaxSingleUploadSizeLong();
        if (maxSingleUploadSize == null) {
            maxSingleUploadSize = BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE;
        }

        return new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressReceiver(other.getProgressReceiver())
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }

    /**
     * Transforms a blob type into a common type.
     * @param blobOptions {@link ParallelTransferOptions}
     * @return {@link com.azure.storage.common.ParallelTransferOptions}
     */
    public static com.azure.storage.common.ParallelTransferOptions wrapBlobOptions(
        ParallelTransferOptions blobOptions) {
        Long blockSize = blobOptions.getBlockSizeLong();
        Integer maxConcurrency = blobOptions.getMaxConcurrency();
        com.azure.storage.common.ProgressReceiver wrappedReceiver = blobOptions.getProgressReceiver() == null
            ? null
            : blobOptions.getProgressReceiver()::reportProgress;
        Long maxSingleUploadSize = blobOptions.getMaxSingleUploadSizeLong();

        return new com.azure.storage.common.ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressReceiver(wrappedReceiver)
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }
}
