package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TagScoringFunction;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TagScoringFunction} and
 * {@link TagScoringFunction} mismatch.
 */
public final class TagScoringFunctionConverter {
    public static TagScoringFunction convert(com.azure.search.documents.models.TagScoringFunction obj) {
        return DefaultConverter.convert(obj, TagScoringFunction.class);
    }

    public static com.azure.search.documents.models.TagScoringFunction convert(TagScoringFunction obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TagScoringFunction.class);
    }
}
