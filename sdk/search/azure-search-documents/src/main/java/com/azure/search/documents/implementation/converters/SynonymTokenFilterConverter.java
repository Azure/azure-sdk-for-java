package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SynonymTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SynonymTokenFilter} and
 * {@link SynonymTokenFilter} mismatch.
 */
public final class SynonymTokenFilterConverter {
    public static SynonymTokenFilter convert(com.azure.search.documents.models.SynonymTokenFilter obj) {
        return DefaultConverter.convert(obj, SynonymTokenFilter.class);
    }

    public static com.azure.search.documents.models.SynonymTokenFilter convert(SynonymTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SynonymTokenFilter.class);
    }
}
