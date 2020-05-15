package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.EdgeNGramTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.EdgeNGramTokenFilter} and
 * {@link EdgeNGramTokenFilter} mismatch.
 */
public final class EdgeNGramTokenFilterConverter {
    public static EdgeNGramTokenFilter convert(com.azure.search.documents.models.EdgeNGramTokenFilter obj) {
        return DefaultConverter.convert(obj, EdgeNGramTokenFilter.class);
    }

    public static com.azure.search.documents.models.EdgeNGramTokenFilter convert(EdgeNGramTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.EdgeNGramTokenFilter.class);
    }
}
