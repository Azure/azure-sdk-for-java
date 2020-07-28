// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation} and
 * {@link ScoringFunctionAggregation}.
 */
public final class ScoringFunctionAggregationConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ScoringFunctionAggregationConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation} to enum
     * {@link ScoringFunctionAggregation}.
     */
    public static ScoringFunctionAggregation map(com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case SUM:
                return ScoringFunctionAggregation.SUM;
            case AVERAGE:
                return ScoringFunctionAggregation.AVERAGE;
            case MINIMUM:
                return ScoringFunctionAggregation.MINIMUM;
            case MAXIMUM:
                return ScoringFunctionAggregation.MAXIMUM;
            case FIRST_MATCHING:
                return ScoringFunctionAggregation.FIRST_MATCHING;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link ScoringFunctionAggregation} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation map(ScoringFunctionAggregation obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case SUM:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation.SUM;
            case AVERAGE:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation.AVERAGE;
            case MINIMUM:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation.MINIMUM;
            case MAXIMUM:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation.MAXIMUM;
            case FIRST_MATCHING:
                return com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation.FIRST_MATCHING;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private ScoringFunctionAggregationConverter() {
    }
}
