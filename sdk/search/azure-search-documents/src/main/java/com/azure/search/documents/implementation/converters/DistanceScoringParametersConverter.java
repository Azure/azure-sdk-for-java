package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DistanceScoringParameters;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DistanceScoringParameters} and
 * {@link DistanceScoringParameters} mismatch.
 */
public final class DistanceScoringParametersConverter {
    public static DistanceScoringParameters convert(com.azure.search.documents.models.DistanceScoringParameters obj) {
        return DefaultConverter.convert(obj, DistanceScoringParameters.class);
    }

    public static com.azure.search.documents.models.DistanceScoringParameters convert(DistanceScoringParameters obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DistanceScoringParameters.class);
    }
}
