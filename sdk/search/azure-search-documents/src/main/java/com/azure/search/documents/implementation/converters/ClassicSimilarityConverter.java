package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ClassicSimilarity;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ClassicSimilarity} and
 * {@link ClassicSimilarity} mismatch.
 */
public final class ClassicSimilarityConverter {
    public static ClassicSimilarity convert(com.azure.search.documents.models.ClassicSimilarity obj) {
        return DefaultConverter.convert(obj, ClassicSimilarity.class);
    }

    public static com.azure.search.documents.models.ClassicSimilarity convert(ClassicSimilarity obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ClassicSimilarity.class);
    }
}
