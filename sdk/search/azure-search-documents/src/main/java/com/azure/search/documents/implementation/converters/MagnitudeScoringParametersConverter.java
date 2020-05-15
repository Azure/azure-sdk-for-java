package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MagnitudeScoringParameters;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MagnitudeScoringParameters} and
 * {@link MagnitudeScoringParameters} mismatch.
 */
public final class MagnitudeScoringParametersConverter {
    public static MagnitudeScoringParameters convert(com.azure.search.documents.models.MagnitudeScoringParameters obj) {
        return DefaultConverter.convert(obj, MagnitudeScoringParameters.class);
    }

    public static com.azure.search.documents.models.MagnitudeScoringParameters convert(MagnitudeScoringParameters obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.MagnitudeScoringParameters.class);
    }
}
