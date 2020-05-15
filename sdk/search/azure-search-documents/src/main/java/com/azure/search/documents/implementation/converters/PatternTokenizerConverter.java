package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.PatternTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.PatternTokenizer} and
 * {@link PatternTokenizer} mismatch.
 */
public final class PatternTokenizerConverter {
    public static PatternTokenizer convert(com.azure.search.documents.models.PatternTokenizer obj) {
        return DefaultConverter.convert(obj, PatternTokenizer.class);
    }

    public static com.azure.search.documents.models.PatternTokenizer convert(PatternTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.PatternTokenizer.class);
    }
}
