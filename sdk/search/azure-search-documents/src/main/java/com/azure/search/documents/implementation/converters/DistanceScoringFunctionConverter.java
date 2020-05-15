package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DistanceScoringFunction;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DistanceScoringFunction} and
 * {@link DistanceScoringFunction} mismatch.
 */
public final class DistanceScoringFunctionConverter {
    public static DistanceScoringFunction convert(com.azure.search.documents.models.DistanceScoringFunction obj) {
        return DefaultConverter.convert(obj, DistanceScoringFunction.class);
    }

    public static com.azure.search.documents.models.DistanceScoringFunction convert(DistanceScoringFunction obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DistanceScoringFunction.class);
    }
}
