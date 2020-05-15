package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LexicalTokenizerName;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LexicalTokenizerName} and
 * {@link LexicalTokenizerName} mismatch.
 */
public final class LexicalTokenizerNameConverter {
    public static LexicalTokenizerName convert(com.azure.search.documents.models.LexicalTokenizerName obj) {
        return DefaultConverter.convert(obj, LexicalTokenizerName.class);
    }

    public static com.azure.search.documents.models.LexicalTokenizerName convert(LexicalTokenizerName obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LexicalTokenizerName.class);
    }
}
