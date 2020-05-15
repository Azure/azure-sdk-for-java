package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.BM25Similarity;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.BM25Similarity} and
 * {@link BM25Similarity} mismatch.
 */
public final class BM25SimilarityConverter {
    public static BM25Similarity convert(com.azure.search.documents.models.BM25Similarity obj) {
        return DefaultConverter.convert(obj, BM25Similarity.class);
    }

    public static com.azure.search.documents.models.BM25Similarity convert(BM25Similarity obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.BM25Similarity.class);
    }
}
