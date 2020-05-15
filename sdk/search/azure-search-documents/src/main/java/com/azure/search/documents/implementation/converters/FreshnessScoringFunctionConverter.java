package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.FreshnessScoringFunction;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.FreshnessScoringFunction} and
 * {@link FreshnessScoringFunction} mismatch.
 */
public final class FreshnessScoringFunctionConverter {
    public static FreshnessScoringFunction convert(com.azure.search.documents.models.FreshnessScoringFunction obj) {
        return DefaultConverter.convert(obj, FreshnessScoringFunction.class);
    }

    public static com.azure.search.documents.models.FreshnessScoringFunction convert(FreshnessScoringFunction obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.FreshnessScoringFunction.class);
    }
}
