package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LuceneStandardTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LuceneStandardTokenizer} and
 * {@link LuceneStandardTokenizer} mismatch.
 */
public final class LuceneStandardTokenizerConverter {
    public static LuceneStandardTokenizer convert(com.azure.search.documents.models.LuceneStandardTokenizer obj) {
        return DefaultConverter.convert(obj, LuceneStandardTokenizer.class);
    }

    public static com.azure.search.documents.models.LuceneStandardTokenizer convert(LuceneStandardTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LuceneStandardTokenizer.class);
    }
}
