package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.PatternAnalyzer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.PatternAnalyzer} and
 * {@link PatternAnalyzer} mismatch.
 */
public final class PatternAnalyzerConverter {
    public static PatternAnalyzer convert(com.azure.search.documents.models.PatternAnalyzer obj) {
        return DefaultConverter.convert(obj, PatternAnalyzer.class);
    }

    public static com.azure.search.documents.models.PatternAnalyzer convert(PatternAnalyzer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.PatternAnalyzer.class);
    }
}
