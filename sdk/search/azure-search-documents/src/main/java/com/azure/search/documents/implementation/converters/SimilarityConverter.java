package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.Similarity;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.Similarity} and
 * {@link Similarity} mismatch.
 */
public final class SimilarityConverter {
    public static Similarity convert(com.azure.search.documents.models.Similarity obj) {
        return DefaultConverter.convert(obj, Similarity.class);
    }

    public static com.azure.search.documents.models.Similarity convert(Similarity obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.Similarity.class);
    }
}
