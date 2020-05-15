package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LuceneStandardAnalyzer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LuceneStandardAnalyzer} and
 * {@link LuceneStandardAnalyzer} mismatch.
 */
public final class LuceneStandardAnalyzerConverter {
    public static LuceneStandardAnalyzer convert(com.azure.search.documents.models.LuceneStandardAnalyzer obj) {
        return DefaultConverter.convert(obj, LuceneStandardAnalyzer.class);
    }

    public static com.azure.search.documents.models.LuceneStandardAnalyzer convert(LuceneStandardAnalyzer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LuceneStandardAnalyzer.class);
    }
}
