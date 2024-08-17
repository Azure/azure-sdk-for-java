// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.PathExpiryOptions;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.DataLakeAclChangeFailedException;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides helper methods for common model patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class ModelHelper {
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);

    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    private static final long MAX_APPEND_FILE_BYTES = 4000L * Constants.MB;

    /**
     * Indicates the default size above which the upload will be broken into blocks and parallelized.
     */
    public static final long FILE_DEFAULT_MAX_SINGLE_UPLOAD_SIZE = 100L * Constants.MB;

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
            .setProgressListener(other.getProgressListener())
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

    /**
     * Grab the proper {@link PathExpiryOptions} based on the options set in {@link DataLakePathCreateOptions}.
     *
     * @param options {@link DataLakePathCreateOptions}
     * @param pathResourceType {@link PathResourceType}
     * @return {@link PathExpiryOptions}
     * @throws IllegalArgumentException if the options are invalid for the pathResourceType
     */
    public static PathExpiryOptions setFieldsIfNull(DataLakePathCreateOptions options, PathResourceType pathResourceType) {
        if (pathResourceType == PathResourceType.DIRECTORY) {
            if (options.getProposedLeaseId() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("ProposedLeaseId does not apply to directories."));
            }
            if (options.getLeaseDuration() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("LeaseDuration does not apply to directories."));
            }
            if (options.getScheduleDeletionOptions() != null && options.getScheduleDeletionOptions().getTimeToExpire() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("TimeToExpire does not apply to directories."));
            }
            if (options.getScheduleDeletionOptions() != null && options.getScheduleDeletionOptions().getExpiresOn() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("ExpiresOn does not apply to directories."));
            }
        }
        if (options.getScheduleDeletionOptions() == null) {
            return null;
        }
        if (options.getScheduleDeletionOptions().getTimeToExpire() != null && options.getScheduleDeletionOptions().getExpiresOn() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("TimeToExpire and ExpiresOn both cannot be set."));
        }
        if (options.getScheduleDeletionOptions().getTimeToExpire() != null) {
            return PathExpiryOptions.RELATIVE_TO_NOW;
        } else if (options.getScheduleDeletionOptions().getExpiresOn() != null) {
            return PathExpiryOptions.ABSOLUTE;
        }
        return null;
    }

    /**
     * Converts the metadata into a string of format "key1=value1, key2=value2" and Base64 encodes the values.
     *
     * @param metadata The metadata.
     *
     * @return The metadata represented as a String.
     * @throws IllegalArgumentException If the metadata contains invalid characters.
     */
    public static String buildMetadataString(Map<String, String> metadata) {
        if (!CoreUtils.isNullOrEmpty(metadata)) {
            StringBuilder sb = new StringBuilder();
            boolean firstMetadata = true;
            for (final Map.Entry<String, String> entry : metadata.entrySet()) {
                if (Objects.isNull(entry.getKey()) || entry.getKey().isEmpty()) {
                    throw new IllegalArgumentException("The key for one of the metadata key-value pairs is null, "
                        + "empty, or whitespace.");
                } else if (Objects.isNull(entry.getValue()) || entry.getValue().isEmpty()) {
                    throw new IllegalArgumentException("The value for one of the metadata key-value pairs is null, "
                        + "empty, or whitespace.");
                }

                /*
                The service has an internal base64 decode when metadata is copied from ADLS to Storage, so getMetadata
                will work as normal. Doing this encoding for the customers preserves the existing behavior of
                metadata.
                 */
                if (!firstMetadata) {
                    sb.append(',');
                }

                sb.append(entry.getKey())
                    .append('=')
                    .append(new String(Base64.getEncoder().encode(entry.getValue().getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8));
                firstMetadata = false;
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}
