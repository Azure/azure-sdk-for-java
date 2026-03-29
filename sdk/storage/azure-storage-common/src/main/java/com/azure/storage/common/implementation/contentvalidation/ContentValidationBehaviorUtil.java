// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;

import com.azure.storage.common.StorageChecksumAlgorithm;

/**
 * Determines the content validation behavior string to pass in the pipeline context for upload operations.
 * Callers put the returned value under {@link StructuredMessageConstants#CONTENT_VALIDATION_BEHAVIOR_KEY}.
 * <p>
 * Single-part: use CRC64 header when length &lt; 4MB, otherwise structured message.
 * Chunked (multi-part): always use structured message.
 */
public final class ContentValidationBehaviorUtil {

    private ContentValidationBehaviorUtil() {
    }

    public static final String CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE
        = "Individual MD5 option and checksum algorithm option bag are both used. Only one form of transactional content validation may be used.";

    /**
     * Behavior for a single-part upload. Use CRC64 header when length is less than 4MB, otherwise structured
     * message.
     *
     * @param algorithm The request checksum algorithm (e.g. from upload options).
     * @param length The upload length in bytes.
     * @return The behavior string for context, or null if no content validation should be applied.
     */
    public static String getBehaviorForSinglePartUpload(StorageChecksumAlgorithm algorithm, long length) {
        if (algorithm == null
            || algorithm == StorageChecksumAlgorithm.NONE
            || algorithm == StorageChecksumAlgorithm.MD5) {
            return null;
        }
        if (algorithm != StorageChecksumAlgorithm.CRC64 && algorithm != StorageChecksumAlgorithm.AUTO) {
            return null;
        }
        return length < MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER
            ? USE_CRC64_CHECKSUM_HEADER_CONTEXT
            : USE_STRUCTURED_MESSAGE_CONTEXT;
    }

    /**
     * Behavior for a chunked (multi-part) upload. Always use structured message.
     *
     * @param algorithm The request checksum algorithm (e.g. from upload options).
     * @return The behavior string for context, or null if no content validation should be applied.
     */
    public static String getBehaviorForChunkedUpload(StorageChecksumAlgorithm algorithm) {
        if (algorithm == null
            || algorithm == StorageChecksumAlgorithm.NONE
            || algorithm == StorageChecksumAlgorithm.MD5) {
            return null;
        }
        if (algorithm != StorageChecksumAlgorithm.CRC64 && algorithm != StorageChecksumAlgorithm.AUTO) {
            return null;
        }
        return USE_STRUCTURED_MESSAGE_CONTEXT;
    }

    /**
     * Checks if callers configured mutually exclusive transactional content validation options.
     * <p>
     * If both {@code contentMd5} and {@code requestChecksumAlgorithm} are specified (and the algorithm is not
     * {@link StorageChecksumAlgorithm#NONE}), Azure only accepts one form of validation.
     *
     * @param contentMd5 The MD5 hash (when provided) for the request.
     * @param requestChecksumAlgorithm The request checksum algorithm (when provided) for the request.
     * @return {@code true} if both {@code contentMd5} and a non-{@link StorageChecksumAlgorithm#NONE}
     * {@code requestChecksumAlgorithm} are specified, otherwise {@code false}.
     */
    public static boolean hasConflictingTransactionalContentValidation(byte[] contentMd5,
        StorageChecksumAlgorithm requestChecksumAlgorithm) {
        if (contentMd5 != null
            && requestChecksumAlgorithm != null
            && requestChecksumAlgorithm != StorageChecksumAlgorithm.NONE) {
            return true;
        }
        return false;
    }
}
