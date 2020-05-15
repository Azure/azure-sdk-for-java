package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.EdgeNGramTokenFilterSide;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.EdgeNGramTokenFilterSide} and
 * {@link EdgeNGramTokenFilterSide} mismatch.
 */
public final class EdgeNGramTokenFilterSideConverter {
    public static EdgeNGramTokenFilterSide convert(com.azure.search.documents.models.EdgeNGramTokenFilterSide obj) {
        return DefaultConverter.convert(obj, EdgeNGramTokenFilterSide.class);
    }

    public static com.azure.search.documents.models.EdgeNGramTokenFilterSide convert(EdgeNGramTokenFilterSide obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.EdgeNGramTokenFilterSide.class);
    }
}
