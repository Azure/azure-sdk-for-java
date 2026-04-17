// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.Context;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.ContentValidationModeResolver;

/**
 * Centralizes download content validation decisions based on {@link ContentValidationAlgorithm}.
 * <p>
 * Mirrors the pattern established by {@link ContentValidationModeResolver} for uploads.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class DownloadValidationUtils {

    private DownloadValidationUtils() {
    }

    /**
     * Whether the algorithm requires structured message decoding (CRC64 / AUTO).
     */
    public static boolean isStructuredMessageAlgorithm(ContentValidationAlgorithm algorithm) {
        return ContentValidationModeResolver.isCrc64OrAuto(algorithm);
    }

    /**
     * Resolves the effective algorithm, defaulting null to NONE.
     */
    public static ContentValidationAlgorithm resolveAlgorithm(ContentValidationAlgorithm algorithm) {
        return algorithm != null ? algorithm : ContentValidationAlgorithm.NONE;
    }

    /**
     * Adds structured message decoding context key when CRC64/AUTO validation is active.
     *
     * @param context The base context to augment. Null is treated as {@link Context#NONE}.
     * @param algorithm The resolved checksum algorithm.
     * @return The augmented context.
     */
    public static Context applyStructuredMessageContext(Context context, ContentValidationAlgorithm algorithm) {
        Context base = context == null ? Context.NONE : context;
        if (!isStructuredMessageAlgorithm(algorithm)) {
            return base;
        }
        return base.addData(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true);
    }
}
