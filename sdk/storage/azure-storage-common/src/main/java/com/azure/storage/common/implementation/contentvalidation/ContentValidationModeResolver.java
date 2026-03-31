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
 * Single-part: use CRC64 header when length &lt; 4MB, otherwise structured message.
 * Chunked (multi-part): always use structured message.
 */
public final class ContentValidationModeResolver {

    private ContentValidationModeResolver() {
    }

    public static final String CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE
        = "Individual MD5 option and checksum algorithm option bag are both used. Only one form of transactional content validation may be used.";

    /**
     * Mode for a single-part upload. Use CRC64 header when length is less than 4MB, otherwise structured
     * message.
     *
     * @param algorithm The request checksum algorithm (e.g. from upload options).
     * @param length The upload length in bytes.
     * @return The mode string for context, or null if no content validation should be applied.
     */
    public static String getModeForSinglePartUpload(StorageChecksumAlgorithm algorithm, long length) {
        if (algorithm == StorageChecksumAlgorithm.CRC64) {
            return length < MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER
                ? USE_CRC64_CHECKSUM_HEADER_CONTEXT
                : USE_STRUCTURED_MESSAGE_CONTEXT;
        }
        return null;
    }

    /**
     * Mode for a chunked (multi-part) upload. Always use structured message.
     *
     * @param algorithm The request checksum algorithm (e.g. from upload options).
     * @return The mode string for context, or null if no content validation should be applied.
     */
    public static String getModeForChunkedUpload(StorageChecksumAlgorithm algorithm) {
        if (algorithm == StorageChecksumAlgorithm.CRC64) {
            return USE_STRUCTURED_MESSAGE_CONTEXT;
        }
        return null;
    }

    /**
     * Resolves content validation mode and adds it to the Azure {@link Context} when non-null.
     *
     * @param context The request context; {@code null} is treated as {@link Context#NONE}.
     * @param algorithm The request checksum algorithm (e.g. from upload options).
     * @param contentLength The upload length in bytes; ignored when {@code chunkedUpload} is {@code true}.
     * @param chunkedUpload {@code true} to use {@link #getModeForChunkedUpload}; {@code false} for
     * {@link #getModeForSinglePartUpload}.
     * @return The context, with {@link StructuredMessageConstants#CONTENT_VALIDATION_MODE_KEY} set when applicable.
     */
    public static Context addContentValidationMode(Context context, StorageChecksumAlgorithm algorithm,
        long contentLength, boolean chunkedUpload) {
        Context base = context == null ? Context.NONE : context;
        String mode
            = chunkedUpload ? getModeForChunkedUpload(algorithm) : getModeForSinglePartUpload(algorithm, contentLength);
        return mode == null ? base : base.addData(CONTENT_VALIDATION_MODE_KEY, mode);
    }

    /**
     * Resolves content validation mode and propagates it on the Reactor context for {@code mono} when non-null.
     *
     * @param mono The reactive sequence to augment.
     * @param algorithm The request checksum algorithm (e.g. from upload options).
     * @param contentLength The upload length in bytes; ignored when {@code chunkedUpload} is {@code true}.
     * @param chunkedUpload {@code true} to use {@link #getModeForChunkedUpload}; {@code false} for
     * {@link #getModeForSinglePartUpload}.
     * @param <T> The element type of {@code mono}.
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
     * Checks if callers configured mutually exclusive transactional content validation options.
     * <p>
     * If both {@code contentMd5} and {@code transferValidationChecksumAlgorithm} are specified (and the
     * algorithm is not {@link StorageChecksumAlgorithm#NONE}), Azure only accepts one form of validation.
     *
     * @param contentMd5 The MD5 hash (when provided) for the request.
     * @param transferValidationChecksumAlgorithm The transfer validation checksum algorithm (when provided)
     * for the request.
     * @return {@code true} if both {@code contentMd5} and a non-{@link StorageChecksumAlgorithm#NONE}
     * {@code transferValidationChecksumAlgorithm} are specified, otherwise {@code false}.
     */
    public static boolean hasConflictingTransactionalContentValidation(byte[] contentMd5,
        StorageChecksumAlgorithm transferValidationChecksumAlgorithm) {
        if (contentMd5 != null
            && transferValidationChecksumAlgorithm != null
            && transferValidationChecksumAlgorithm != StorageChecksumAlgorithm.NONE) {
            return true;
        }
        return false;
    }

    /**
     * Whether SDK-computed MD5 ({@code computeMd5}) conflicts with a non-null transfer validation checksum algorithm.
     *
     * @param computeMd5 Whether the SDK will compute and send transactional MD5.
     * @param transferValidationChecksumAlgorithm The transfer validation checksum algorithm from options.
     * @return {@code true} if both are active.
     */
    public static boolean hasConflictingTransactionalContentValidation(boolean computeMd5,
        StorageChecksumAlgorithm transferValidationChecksumAlgorithm) {
        return computeMd5 && transferValidationChecksumAlgorithm != null;
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
        if (hasConflictingTransactionalContentValidation(contentMd5, transferValidationChecksumAlgorithm)) {
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
        if (hasConflictingTransactionalContentValidation(computeMd5, transferValidationChecksumAlgorithm)) {
            throw new IllegalArgumentException(CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE);
        }
    }
}
