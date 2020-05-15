package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.StopAnalyzer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.StopAnalyzer} and
 * {@link StopAnalyzer} mismatch.
 */
public final class StopAnalyzerConverter {
    public static StopAnalyzer convert(com.azure.search.documents.models.StopAnalyzer obj) {
        return DefaultConverter.convert(obj, StopAnalyzer.class);
    }

    public static com.azure.search.documents.models.StopAnalyzer convert(StopAnalyzer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.StopAnalyzer.class);
    }
}
