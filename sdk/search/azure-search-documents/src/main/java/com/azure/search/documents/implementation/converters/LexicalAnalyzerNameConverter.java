package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LexicalAnalyzerName;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LexicalAnalyzerName} and
 * {@link LexicalAnalyzerName} mismatch.
 */
public final class LexicalAnalyzerNameConverter {
    public static LexicalAnalyzerName convert(com.azure.search.documents.models.LexicalAnalyzerName obj) {
        return DefaultConverter.convert(obj, LexicalAnalyzerName.class);
    }

    public static com.azure.search.documents.models.LexicalAnalyzerName convert(LexicalAnalyzerName obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LexicalAnalyzerName.class);
    }
}
