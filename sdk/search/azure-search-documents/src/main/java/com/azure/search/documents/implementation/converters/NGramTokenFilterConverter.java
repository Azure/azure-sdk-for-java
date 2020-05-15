package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.NGramTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.NGramTokenFilter} and
 * {@link NGramTokenFilter} mismatch.
 */
public final class NGramTokenFilterConverter {
    public static NGramTokenFilter convert(com.azure.search.documents.models.NGramTokenFilter obj) {
        return DefaultConverter.convert(obj, NGramTokenFilter.class);
    }

    public static com.azure.search.documents.models.NGramTokenFilter convert(NGramTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.NGramTokenFilter.class);
    }
}
