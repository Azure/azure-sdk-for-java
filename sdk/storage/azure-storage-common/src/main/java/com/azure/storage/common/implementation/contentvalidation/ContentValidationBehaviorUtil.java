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
}
