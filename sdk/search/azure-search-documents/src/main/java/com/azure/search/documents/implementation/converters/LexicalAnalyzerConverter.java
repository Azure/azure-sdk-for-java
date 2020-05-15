package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LexicalAnalyzer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LexicalAnalyzer} and
 * {@link LexicalAnalyzer} mismatch.
 */
public final class LexicalAnalyzerConverter {
    public static LexicalAnalyzer convert(com.azure.search.documents.models.LexicalAnalyzer obj) {
        return DefaultConverter.convert(obj, LexicalAnalyzer.class);
    }

    public static com.azure.search.documents.models.LexicalAnalyzer convert(LexicalAnalyzer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LexicalAnalyzer.class);
    }
}
