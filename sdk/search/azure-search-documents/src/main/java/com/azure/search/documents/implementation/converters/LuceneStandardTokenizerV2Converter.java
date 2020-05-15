package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LuceneStandardTokenizerV2} and
 * {@link LuceneStandardTokenizerV2} mismatch.
 */
public final class LuceneStandardTokenizerV2Converter {
    public static LuceneStandardTokenizerV2 convert(com.azure.search.documents.models.LuceneStandardTokenizerV2 obj) {
        return DefaultConverter.convert(obj, LuceneStandardTokenizerV2.class);
    }

    public static com.azure.search.documents.models.LuceneStandardTokenizerV2 convert(LuceneStandardTokenizerV2 obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LuceneStandardTokenizerV2.class);
    }
}
