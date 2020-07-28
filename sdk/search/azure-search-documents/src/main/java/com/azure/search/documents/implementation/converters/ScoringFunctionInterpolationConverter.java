// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation} and
 * {@link ScoringFunctionInterpolation}.
 */
public final class ScoringFunctionInterpolationConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ScoringFunctionInterpolationConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation} to enum
     * {@link ScoringFunctionInterpolation}.
     */
    public static ScoringFunctionInterpolation map(com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case LINEAR:
                return ScoringFunctionInterpolation.LINEAR;
            case CONSTANT:
                return ScoringFunctionInterpolation.CONSTANT;
            case QUADRATIC:
                return ScoringFunctionInterpolation.QUADRATIC;
            case LOGARITHMIC:
                return ScoringFunctionInterpolation.LOGARITHMIC;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link ScoringFunctionInterpolation} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation map(ScoringFunctionInterpolation obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case LINEAR:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation.LINEAR;
            case CONSTANT:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation.CONSTANT;
            case QUADRATIC:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation.QUADRATIC;
            case LOGARITHMIC:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionInterpolation.LOGARITHMIC;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private ScoringFunctionInterpolationConverter() {
    }
}
