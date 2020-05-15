package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.EdgeNGramTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.EdgeNGramTokenizer} and
 * {@link EdgeNGramTokenizer} mismatch.
 */
public final class EdgeNGramTokenizerConverter {
    public static EdgeNGramTokenizer convert(com.azure.search.documents.models.EdgeNGramTokenizer obj) {
        return DefaultConverter.convert(obj, EdgeNGramTokenizer.class);
    }

    public static com.azure.search.documents.models.EdgeNGramTokenizer convert(EdgeNGramTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.EdgeNGramTokenizer.class);
    }
}
