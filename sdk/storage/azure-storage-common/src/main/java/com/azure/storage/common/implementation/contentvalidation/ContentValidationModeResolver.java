// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CONTENT_VALIDATION_MODE_KEY;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.StorageChecksumAlgorithm;
import reactor.core.publisher.Mono;

/**
 * Determines the content validation mode string to pass in the pipeline context for upload operations.
 * Callers put the returned value under {@link StructuredMessageConstants#CONTENT_VALIDATION_MODE_KEY}.
 * <p>
 * Single-part: use CRC64 header when length &lt; 4 MiB, otherwise structured message.
 * Chunked (multi-part): always use structured message.
 */
public final class ContentValidationModeResolver {

    private ContentValidationModeResolver() {
    }

    public static final String CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE
        = "Individual MD5 option and checksum algorithm option bag are both used. Only one form of transactional content validation may be used.";

    /**
     * Resolves content validation mode and adds it to the Azure {@link Context} when non-null.
     *
     * @param context The request context; {@code null} is treated as {@link Context#NONE}.
     * @param algorithm The transfer validation checksum algorithm.
     * @param contentLength The upload length in bytes.
     * @param chunkedUpload Whether this request is part of a multi-part upload.
     * @return The context, with {@link StructuredMessageConstants#CONTENT_VALIDATION_MODE_KEY} set when applicable.
     */
    public static Context addContentValidationMode(Context context, StorageChecksumAlgorithm algorithm,
        long contentLength, boolean chunkedUpload) {
        Context baseContext = context == null ? Context.NONE : context;
        String mode
            = chunkedUpload ? getModeForChunkedUpload(algorithm) : getModeForSinglePartUpload(algorithm, contentLength);
        return mode == null ? baseContext : baseContext.addData(CONTENT_VALIDATION_MODE_KEY, mode);
    }

    /**
     * Resolves content validation mode and propagates it on the Reactor context for {@code mono} when non-null.
     *
     * @param mono The reactive sequence to augment.
     * @param algorithm The transfer validation checksum algorithm.
     * @param contentLength The upload length in bytes.
     * @param chunkedUpload Whether this request is part of a multi-part upload.
     * @param <T> The type of the elements in the reactive sequence.
     * @return {@code mono}, possibly augmented with Reactor context writes.
     */
    public static <T> Mono<T> addContentValidationMode(Mono<T> mono, StorageChecksumAlgorithm algorithm,
        long contentLength, boolean chunkedUpload) {
        String mode
            = chunkedUpload ? getModeForChunkedUpload(algorithm) : getModeForSinglePartUpload(algorithm, contentLength);
        if (mode == null) {
            return mono;
        }
        return mono.contextWrite(FluxUtil.toReactorContext(new Context(CONTENT_VALIDATION_MODE_KEY, mode)));
    }

    /**
     * Mode for a single-part upload. Use CRC64 header when length is less than 4MB, otherwise structured
     * message.
     */
    private static String getModeForSinglePartUpload(StorageChecksumAlgorithm algorithm, long length) {
        if (algorithm == StorageChecksumAlgorithm.CRC64 || algorithm == StorageChecksumAlgorithm.AUTO) {
            return length < MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER
                ? USE_CRC64_CHECKSUM_HEADER_CONTEXT
                : USE_STRUCTURED_MESSAGE_CONTEXT;
        }
        return null;
    }

    /**
     * Mode for a chunked (multi-part) upload. Always use structured message.
     */
    private static String getModeForChunkedUpload(StorageChecksumAlgorithm algorithm) {
        if (algorithm == StorageChecksumAlgorithm.CRC64 || algorithm == StorageChecksumAlgorithm.AUTO) {
            return USE_STRUCTURED_MESSAGE_CONTEXT;
        }
        return null;
    }

    /**
     * Validates transactional checksum options. Throws if {@code contentMd5} and a non-null
     * {@code transferValidationChecksumAlgorithm} are both set.
     * <p>
     * Async clients typically wrap the call in {@code try}/{@code catch} and return
     * {@code com.azure.core.util.FluxUtil.monoError(logger, ex)} so the failure remains a deferred reactive error.
     *
     * @param contentMd5 Caller-provided transactional MD5, if any.
     * @param transferValidationChecksumAlgorithm Transfer validation checksum algorithm from options.
     * @throws IllegalArgumentException if options conflict.
     */
    public static void validateTransactionalChecksumOptions(byte[] contentMd5,
        StorageChecksumAlgorithm transferValidationChecksumAlgorithm) {
        if (contentMd5 != null
            && transferValidationChecksumAlgorithm != null
            && transferValidationChecksumAlgorithm != StorageChecksumAlgorithm.NONE) {
            throw new IllegalArgumentException(CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE);
        }
    }

    /**
     * Validates transactional checksum options when MD5 may be SDK-computed. Throws if {@code computeMd5} and a
     * non-none {@code transferValidationChecksumAlgorithm} are both active.
     *
     * @param computeMd5 Whether the SDK will compute transactional MD5.
     * @param transferValidationChecksumAlgorithm Transfer validation checksum algorithm from options.
     * @throws IllegalArgumentException if options conflict.
     */
    public static void validateTransactionalChecksumOptions(boolean computeMd5,
        StorageChecksumAlgorithm transferValidationChecksumAlgorithm) {
        if (computeMd5 && transferValidationChecksumAlgorithm != null) {
            throw new IllegalArgumentException(CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE);
        }
    }
}
