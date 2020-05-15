package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MagnitudeScoringFunction;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MagnitudeScoringFunction} and
 * {@link MagnitudeScoringFunction} mismatch.
 */
public final class MagnitudeScoringFunctionConverter {
    public static MagnitudeScoringFunction convert(com.azure.search.documents.models.MagnitudeScoringFunction obj) {
        return DefaultConverter.convert(obj, MagnitudeScoringFunction.class);
    }

    public static com.azure.search.documents.models.MagnitudeScoringFunction convert(MagnitudeScoringFunction obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.MagnitudeScoringFunction.class);
    }
}
