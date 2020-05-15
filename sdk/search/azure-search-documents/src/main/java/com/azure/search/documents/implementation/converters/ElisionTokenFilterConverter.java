package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ElisionTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ElisionTokenFilter} and
 * {@link ElisionTokenFilter} mismatch.
 */
public final class ElisionTokenFilterConverter {
    public static ElisionTokenFilter convert(com.azure.search.documents.models.ElisionTokenFilter obj) {
        return DefaultConverter.convert(obj, ElisionTokenFilter.class);
    }

    public static com.azure.search.documents.models.ElisionTokenFilter convert(ElisionTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ElisionTokenFilter.class);
    }
}
