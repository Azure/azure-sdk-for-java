package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.StopwordsTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.StopwordsTokenFilter} and
 * {@link StopwordsTokenFilter} mismatch.
 */
public final class StopwordsTokenFilterConverter {
    public static StopwordsTokenFilter convert(com.azure.search.documents.models.StopwordsTokenFilter obj) {
        return DefaultConverter.convert(obj, StopwordsTokenFilter.class);
    }

    public static com.azure.search.documents.models.StopwordsTokenFilter convert(StopwordsTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.StopwordsTokenFilter.class);
    }
}
