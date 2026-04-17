// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CONTENT_VALIDATION_MODE_KEY;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.ParallelTransferOptions;
import reactor.core.publisher.Mono;

/**
 * Determines the content validation mode string to pass in the pipeline context for upload operations.
 * Callers put the returned value under {@link StructuredMessageConstants#CONTENT_VALIDATION_MODE_KEY}.
 * <p>
 * Single-shot: use CRC64 header when length &lt; 4 MiB, otherwise structured message.
 * Chunked (multi-shot): always use structured message.
 */
public final class ContentValidationModeResolver {

    private ContentValidationModeResolver() {
    }

    public static final String CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE
        = "Individual MD5 option and checksum algorithm option bag are both used. Only one form of transactional content validation may be used.";

    /**
     * Progress reporting counts bytes on the wire; transfer validation (CRC64/AUTO) may use structured messages, so the
     * two cannot be combined. Use {@link ContentValidationAlgorithm#NONE} or null, or omit the progress listener.
     */
    public static final String PROGRESS_CONFLICTS_TRANSFER_CONTENT_VALIDATION_MESSAGE
        = "Progress reporting cannot be combined with ContentValidationAlgorithm.CRC64 or ContentValidationAlgorithm.AUTO. "
            + "Set ContentValidationAlgorithm to NONE or null, or remove the progress listener.";

    /**
     * Resolves content validation mode and adds it to the Azure {@link Context} when non-null.
     *
     * @param context The request context; {@code null} is treated as {@link Context#NONE}.
     * @param algorithm The transfer validation checksum algorithm.
     * @param contentLength The upload length in bytes.
     * @param chunkedUpload Whether this request is part of a multi-shot upload.
     * @return The context, with {@link StructuredMessageConstants#CONTENT_VALIDATION_MODE_KEY} set when applicable.
     */
    public static Context addContentValidationMode(Context context, ContentValidationAlgorithm algorithm,
        long contentLength, boolean chunkedUpload) {
        Context baseContext = context == null ? Context.NONE : context;
        String mode
            = chunkedUpload ? getModeForChunkedUpload(algorithm) : getModeForSingleShotUpload(algorithm, contentLength);
        return mode == null ? baseContext : baseContext.addData(CONTENT_VALIDATION_MODE_KEY, mode);
    }

    /**
     * Resolves content validation mode and propagates it on the Reactor context for {@code mono} when non-null.
     *
     * @param mono The reactive sequence to augment.
     * @param algorithm The transfer validation checksum algorithm.
     * @param contentLength The upload length in bytes.
     * @param chunkedUpload Whether this request is part of a multi-shot upload.
     * @param <T> The type of the elements in the reactive sequence.
     * @return {@code mono}, possibly augmented with Reactor context writes.
     */
    public static <T> Mono<T> addContentValidationMode(Mono<T> mono, ContentValidationAlgorithm algorithm,
        long contentLength, boolean chunkedUpload) {
        String mode
            = chunkedUpload ? getModeForChunkedUpload(algorithm) : getModeForSingleShotUpload(algorithm, contentLength);
        if (mode == null) {
            return mono;
        }
        return mono.contextWrite(FluxUtil.toReactorContext(new Context(CONTENT_VALIDATION_MODE_KEY, mode)));
    }

    /**
     * Mode for a single-shot upload. Use CRC64 header when length is less than 4MB, otherwise structured
     * message.
     */
    private static String getModeForSingleShotUpload(ContentValidationAlgorithm algorithm, long length) {
        if (isCrc64OrAuto(algorithm)) {
            return length < MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER
                ? USE_CRC64_CHECKSUM_HEADER_CONTEXT
                : USE_STRUCTURED_MESSAGE_CONTEXT;
        }
        return null;
    }

    /**
     * Mode for a chunked (multi-shot) upload. Always use structured message.
     */
    private static String getModeForChunkedUpload(ContentValidationAlgorithm algorithm) {
        if (isCrc64OrAuto(algorithm)) {
            return USE_STRUCTURED_MESSAGE_CONTEXT;
        }
        return null;
    }

    /**
     * Validates transactional checksum options. Throws if {@code contentMd5} and a non-null
     * {@code contentValidationAlgorithm} are both set.
     * <p>
     * Async clients typically wrap the call in {@code try}/{@code catch} and return
     * {@code com.azure.core.util.FluxUtil.monoError(logger, ex)} so the failure remains a deferred reactive error.
     *
     * @param contentMd5 Caller-provided transactional MD5, if any.
     * @param contentValidationAlgorithm Transfer validation checksum algorithm from options.
     * @throws IllegalArgumentException if options conflict.
     */
    public static void validateTransactionalChecksumOptions(byte[] contentMd5,
        ContentValidationAlgorithm contentValidationAlgorithm) {
        if (contentMd5 != null && contentValidationAlgorithm != null) {
            throw new IllegalArgumentException(CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE);
        }
    }

    /**
     * Validates transactional checksum options when MD5 may be SDK-computed. Throws if {@code computeMd5} and a
     * non-none {@code contentValidationAlgorithm} are both active.
     *
     * @param computeMd5 Whether the SDK will compute transactional MD5.
     * @param contentValidationAlgorithm Transfer validation checksum algorithm from options.
     * @throws IllegalArgumentException if options conflict.
     */
    public static void validateTransactionalChecksumOptions(boolean computeMd5,
        ContentValidationAlgorithm contentValidationAlgorithm) {
        if (computeMd5 && contentValidationAlgorithm != null) {
            throw new IllegalArgumentException(CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE);
        }
    }

    /**
     * @return {@code true} when {@code contentValidationAlgorithm} enables CRC64 or AUTO transfer validation (not
     * {@code null} and not {@link ContentValidationAlgorithm#NONE}).
     */
    public static boolean isContentValidationAlgorithmPresent(ContentValidationAlgorithm contentValidationAlgorithm) {
        return contentValidationAlgorithm != null && contentValidationAlgorithm != ContentValidationAlgorithm.NONE;
    }

    /**
     * @return {@code true} when {@code algorithm} is {@link ContentValidationAlgorithm#CRC64} or
     * {@link ContentValidationAlgorithm#AUTO}. Upload and download structured-message validation use this rule.
     */
    public static boolean isCrc64OrAuto(ContentValidationAlgorithm algorithm) {
        return algorithm == ContentValidationAlgorithm.CRC64 || algorithm == ContentValidationAlgorithm.AUTO;
    }

    /**
     * Validates that parallel transfer progress reporting is not combined with CRC64/AUTO content validation.
     *
     * @param parallelTransferOptions May be {@code null}.
     * @param contentValidationAlgorithm Transfer validation algorithm from options.
     * @throws IllegalArgumentException if a progress listener is set and {@link #isContentValidationAlgorithmPresent} is true.
     */
    public static void validateProgressWithContentValidation(ParallelTransferOptions parallelTransferOptions,
        ContentValidationAlgorithm contentValidationAlgorithm) {
        if (parallelTransferOptions == null) {
            return;
        }
        validateProgressWithContentValidation(parallelTransferOptions.getProgressListener(),
            contentValidationAlgorithm);
    }

    /**
     * Validates that progress reporting is not combined with CRC64/AUTO content validation.
     *
     * @param progressListener Progress listener from {@link ParallelTransferOptions} or equivalent; may be null.
     * @param contentValidationAlgorithm Transfer validation algorithm from options.
     * @throws IllegalArgumentException if {@code progressListener} is non-null and {@link #isContentValidationAlgorithmPresent}
     * is true.
     */
    public static void validateProgressWithContentValidation(ProgressListener progressListener,
        ContentValidationAlgorithm contentValidationAlgorithm) {
        if (progressListener != null && isContentValidationAlgorithmPresent(contentValidationAlgorithm)) {
            throw new IllegalArgumentException(PROGRESS_CONFLICTS_TRANSFER_CONTENT_VALIDATION_MESSAGE);
        }
    }
}
