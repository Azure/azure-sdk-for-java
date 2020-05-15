package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ScoringFunctionAggregation;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ScoringFunctionAggregation} and
 * {@link ScoringFunctionAggregation} mismatch.
 */
public final class ScoringFunctionAggregationConverter {
    public static ScoringFunctionAggregation convert(com.azure.search.documents.models.ScoringFunctionAggregation obj) {
        return DefaultConverter.convert(obj, ScoringFunctionAggregation.class);
    }

    public static com.azure.search.documents.models.ScoringFunctionAggregation convert(ScoringFunctionAggregation obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ScoringFunctionAggregation.class);
    }
}
