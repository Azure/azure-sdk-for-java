package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ScoringFunction;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ScoringFunction} and
 * {@link ScoringFunction} mismatch.
 */
public final class ScoringFunctionConverter {
    public static ScoringFunction convert(com.azure.search.documents.models.ScoringFunction obj) {
        return DefaultConverter.convert(obj, ScoringFunction.class);
    }

    public static com.azure.search.documents.models.ScoringFunction convert(ScoringFunction obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ScoringFunction.class);
    }
}
