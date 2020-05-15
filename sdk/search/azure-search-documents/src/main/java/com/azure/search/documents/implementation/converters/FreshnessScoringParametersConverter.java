package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.FreshnessScoringParameters;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.FreshnessScoringParameters} and
 * {@link FreshnessScoringParameters} mismatch.
 */
public final class FreshnessScoringParametersConverter {
    public static FreshnessScoringParameters convert(com.azure.search.documents.models.FreshnessScoringParameters obj) {
        return DefaultConverter.convert(obj, FreshnessScoringParameters.class);
    }

    public static com.azure.search.documents.models.FreshnessScoringParameters convert(FreshnessScoringParameters obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.FreshnessScoringParameters.class);
    }
}
