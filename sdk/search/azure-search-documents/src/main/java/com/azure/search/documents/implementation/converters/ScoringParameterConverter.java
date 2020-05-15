package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ScoringParameter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ScoringParameter} and
 * {@link ScoringParameter} mismatch.
 */
public final class ScoringParameterConverter {
    public static ScoringParameter convert(com.azure.search.documents.models.ScoringParameter obj) {
        return DefaultConverter.convert(obj, ScoringParameter.class);
    }

    public static com.azure.search.documents.models.ScoringParameter convert(ScoringParameter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ScoringParameter.class);
    }
}
