package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ScoringFunctionInterpolation;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ScoringFunctionInterpolation} and
 * {@link ScoringFunctionInterpolation} mismatch.
 */
public final class ScoringFunctionInterpolationConverter {
    public static ScoringFunctionInterpolation convert(com.azure.search.documents.models.ScoringFunctionInterpolation obj) {
        return DefaultConverter.convert(obj, ScoringFunctionInterpolation.class);
    }

    public static com.azure.search.documents.models.ScoringFunctionInterpolation convert(ScoringFunctionInterpolation obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ScoringFunctionInterpolation.class);
    }
}
