package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.WordDelimiterTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.WordDelimiterTokenFilter} and
 * {@link WordDelimiterTokenFilter} mismatch.
 */
public final class WordDelimiterTokenFilterConverter {
    public static WordDelimiterTokenFilter convert(com.azure.search.documents.models.WordDelimiterTokenFilter obj) {
        return DefaultConverter.convert(obj, WordDelimiterTokenFilter.class);
    }

    public static com.azure.search.documents.models.WordDelimiterTokenFilter convert(WordDelimiterTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.WordDelimiterTokenFilter.class);
    }
}
