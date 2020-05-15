package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LexicalTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LexicalTokenizer} and
 * {@link LexicalTokenizer} mismatch.
 */
public final class LexicalTokenizerConverter {
    public static LexicalTokenizer convert(com.azure.search.documents.models.LexicalTokenizer obj) {
        return DefaultConverter.convert(obj, LexicalTokenizer.class);
    }

    public static com.azure.search.documents.models.LexicalTokenizer convert(LexicalTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LexicalTokenizer.class);
    }
}
