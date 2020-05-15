package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TagScoringParameters;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TagScoringParameters} and
 * {@link TagScoringParameters} mismatch.
 */
public final class TagScoringParametersConverter {
    public static TagScoringParameters convert(com.azure.search.documents.models.TagScoringParameters obj) {
        return DefaultConverter.convert(obj, TagScoringParameters.class);
    }

    public static com.azure.search.documents.models.TagScoringParameters convert(TagScoringParameters obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TagScoringParameters.class);
    }
}
