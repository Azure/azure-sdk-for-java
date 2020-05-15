package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.NGramTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.NGramTokenizer} and
 * {@link NGramTokenizer} mismatch.
 */
public final class NGramTokenizerConverter {
    public static NGramTokenizer convert(com.azure.search.documents.models.NGramTokenizer obj) {
        return DefaultConverter.convert(obj, NGramTokenizer.class);
    }

    public static com.azure.search.documents.models.NGramTokenizer convert(NGramTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.NGramTokenizer.class);
    }
}
