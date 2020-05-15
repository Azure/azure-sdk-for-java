package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.KeywordTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.KeywordTokenizer} and
 * {@link KeywordTokenizer} mismatch.
 */
public final class KeywordTokenizerConverter {
    public static KeywordTokenizer convert(com.azure.search.documents.models.KeywordTokenizer obj) {
        return DefaultConverter.convert(obj, KeywordTokenizer.class);
    }

    public static com.azure.search.documents.models.KeywordTokenizer convert(KeywordTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.KeywordTokenizer.class);
    }
}
