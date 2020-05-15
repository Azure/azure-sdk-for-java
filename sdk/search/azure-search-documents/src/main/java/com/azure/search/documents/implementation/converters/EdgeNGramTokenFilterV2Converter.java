package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.EdgeNGramTokenFilterV2;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.EdgeNGramTokenFilterV2} and
 * {@link EdgeNGramTokenFilterV2} mismatch.
 */
public final class EdgeNGramTokenFilterV2Converter {
    public static EdgeNGramTokenFilterV2 convert(com.azure.search.documents.models.EdgeNGramTokenFilterV2 obj) {
        return DefaultConverter.convert(obj, EdgeNGramTokenFilterV2.class);
    }

    public static com.azure.search.documents.models.EdgeNGramTokenFilterV2 convert(EdgeNGramTokenFilterV2 obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.EdgeNGramTokenFilterV2.class);
    }
}
