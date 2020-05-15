package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.CustomAnalyzer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.CustomAnalyzer} and
 * {@link CustomAnalyzer} mismatch.
 */
public final class CustomAnalyzerConverter {
    public static CustomAnalyzer convert(com.azure.search.documents.models.CustomAnalyzer obj) {
        return DefaultConverter.convert(obj, CustomAnalyzer.class);
    }

    public static com.azure.search.documents.models.CustomAnalyzer convert(CustomAnalyzer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.CustomAnalyzer.class);
    }
}
