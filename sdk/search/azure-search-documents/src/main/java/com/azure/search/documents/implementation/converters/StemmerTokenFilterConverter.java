package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.StemmerTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.StemmerTokenFilter} and
 * {@link StemmerTokenFilter} mismatch.
 */
public final class StemmerTokenFilterConverter {
    public static StemmerTokenFilter convert(com.azure.search.documents.models.StemmerTokenFilter obj) {
        return DefaultConverter.convert(obj, StemmerTokenFilter.class);
    }

    public static com.azure.search.documents.models.StemmerTokenFilter convert(StemmerTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.StemmerTokenFilter.class);
    }
}
