// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.DataLakeAclChangeFailedException;
import com.azure.storage.file.datalake.models.DataLakeStorageException;

/**
 * This class provides helper methods for common model patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class ModelHelper {

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    private static final long MAX_APPEND_FILE_BYTES = 4000L * Constants.MB;

    /**
     * Indicates the default size above which the upload will be broken into blocks and parallelized.
     */
    private static final long FILE_DEFAULT_MAX_SINGLE_UPLOAD_SIZE = 100L * Constants.MB;

    /**
     * The block size to use if none is specified in parallel operations.
     */
    private static final long FILE_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;

    /**
     * The number of buffers to use if none is specified on the buffered upload method.
     */
    private static final int FILE_DEFAULT_NUMBER_OF_BUFFERS = 8;

    /**
     * Fills in default values for a ParallelTransferOptions where no value has been set. This will construct a new
     * object for safety.
     *
     * @param other The options to fill in defaults.
     * @return An object with defaults filled in for null values in the original.
     */
    public static ParallelTransferOptions populateAndApplyDefaults(ParallelTransferOptions other) {
        other = other == null ? new ParallelTransferOptions() : other;

        // For now these two checks are useful for when we transition to
        if (other.getBlockSizeLong() != null) {
            StorageImplUtils.assertInBounds("ParallelTransferOptions.blockSize", other.getBlockSizeLong(), 1,
                MAX_APPEND_FILE_BYTES);
        }

        if (other.getMaxSingleUploadSizeLong() != null) {
            StorageImplUtils.assertInBounds("ParallelTransferOptions.maxSingleUploadSize",
                other.getMaxSingleUploadSizeLong(), 1, MAX_APPEND_FILE_BYTES);
        }

        Long blockSize = other.getBlockSizeLong();
        if (blockSize == null) {
            blockSize = FILE_DEFAULT_UPLOAD_BLOCK_SIZE;
        }

        Integer maxConcurrency = other.getMaxConcurrency();
        if (maxConcurrency == null) {
            maxConcurrency = FILE_DEFAULT_NUMBER_OF_BUFFERS;
        }

        Long maxSingleUploadSize = other.getMaxSingleUploadSizeLong();
        if (maxSingleUploadSize == null) {
            maxSingleUploadSize = FILE_DEFAULT_MAX_SINGLE_UPLOAD_SIZE;
        }

        return new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressReceiver(other.getProgressReceiver())
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }

    public static DataLakeAclChangeFailedException changeAclRequestFailed(DataLakeStorageException e,
        String continuationToken) {
        String message = String.format("An error occurred while recursively changing the access control list. See the "
            + "exception of type %s with status=%s and error code=%s for more information. You can resume changing "
            + "the access control list using continuationToken=%s after addressing the error.", e.getClass(),
            e.getStatusCode(), e.getErrorCode(), continuationToken);
        return new DataLakeAclChangeFailedException(message, e, continuationToken);
    }

    public static DataLakeAclChangeFailedException changeAclFailed(Exception e, String continuationToken) {
        String message = String.format("An error occurred while recursively changing the access control list. See the "
                + "exception of type %s for more information. You can resume changing the access control list using "
                + "continuationToken=%s after addressing the error.", e.getClass(), continuationToken);
        return new DataLakeAclChangeFailedException(message, e, continuationToken);
    }
}
