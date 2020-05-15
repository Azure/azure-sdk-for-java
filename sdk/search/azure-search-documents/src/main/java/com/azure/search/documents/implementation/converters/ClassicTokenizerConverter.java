package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ClassicTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ClassicTokenizer} and
 * {@link ClassicTokenizer} mismatch.
 */
public final class ClassicTokenizerConverter {
    public static ClassicTokenizer convert(com.azure.search.documents.models.ClassicTokenizer obj) {
        return DefaultConverter.convert(obj, ClassicTokenizer.class);
    }

    public static com.azure.search.documents.models.ClassicTokenizer convert(ClassicTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ClassicTokenizer.class);
    }
}
