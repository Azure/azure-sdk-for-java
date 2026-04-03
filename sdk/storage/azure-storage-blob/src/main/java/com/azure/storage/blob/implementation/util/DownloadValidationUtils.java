// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.Context;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.AggregateCrcState;

import java.util.concurrent.atomic.AtomicReference;

import com.azure.storage.common.policy.DecoderState;

/**
 * Centralizes download content validation decisions based on {@link StorageChecksumAlgorithm}.
 * <p>
 * Mirrors the pattern established by {@code ContentValidationModeResolver} for uploads.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class DownloadValidationUtils {

    private DownloadValidationUtils() {
    }

    /**
     * Whether the algorithm requires structured message decoding (CRC64 / AUTO).
     */
    public static boolean isStructuredMessageAlgorithm(StorageChecksumAlgorithm algorithm) {
        return algorithm == StorageChecksumAlgorithm.CRC64 || algorithm == StorageChecksumAlgorithm.AUTO;
    }

    /**
     * Resolves the effective algorithm, defaulting null to NONE.
     */
    public static StorageChecksumAlgorithm resolveAlgorithm(StorageChecksumAlgorithm algorithm) {
        return algorithm != null ? algorithm : StorageChecksumAlgorithm.NONE;
    }

    /**
     * Adds structured message decoding context keys when CRC64/AUTO validation is active.
     *
     * @param context The base context to augment. Null is treated as {@link Context#NONE}.
     * @param algorithm The resolved checksum algorithm.
     * @param decoderStateRef Holder for decoder state, populated by the policy.
     * @param aggregateCrcState CRC aggregation state across retries.
     * @return The augmented context.
     */
    public static Context applyStructuredMessageContext(Context context, StorageChecksumAlgorithm algorithm,
        AtomicReference<DecoderState> decoderStateRef, AggregateCrcState aggregateCrcState) {
        Context base = context == null ? Context.NONE : context;
        if (!isStructuredMessageAlgorithm(algorithm)) {
            return base;
        }
        return base.addData(Constants.STRUCTURED_MESSAGE_RESPONSE_SCOPED_CONTEXT_KEY, true)
            .addData(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true)
            .addData(Constants.STRUCTURED_MESSAGE_DECODER_STATE_REF_CONTEXT_KEY, decoderStateRef)
            .addData(Constants.STRUCTURED_MESSAGE_AGGREGATE_CRC_CONTEXT_KEY, aggregateCrcState);
    }
}
