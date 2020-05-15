package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ScoringProfile;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ScoringProfile} and
 * {@link ScoringProfile} mismatch.
 */
public final class ScoringProfileConverter {
    public static ScoringProfile convert(com.azure.search.documents.models.ScoringProfile obj) {
        return DefaultConverter.convert(obj, ScoringProfile.class);
    }

    public static com.azure.search.documents.models.ScoringProfile convert(ScoringProfile obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ScoringProfile.class);
    }
}
