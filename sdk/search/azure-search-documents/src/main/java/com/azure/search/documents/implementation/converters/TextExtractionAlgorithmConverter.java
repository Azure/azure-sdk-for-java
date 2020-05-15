package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TextExtractionAlgorithm;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TextExtractionAlgorithm} and
 * {@link TextExtractionAlgorithm} mismatch.
 */
public final class TextExtractionAlgorithmConverter {
    public static TextExtractionAlgorithm convert(com.azure.search.documents.models.TextExtractionAlgorithm obj) {
        return DefaultConverter.convert(obj, TextExtractionAlgorithm.class);
    }

    public static com.azure.search.documents.models.TextExtractionAlgorithm convert(TextExtractionAlgorithm obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TextExtractionAlgorithm.class);
    }
}
