package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.StemmerOverrideTokenFilter} and
 * {@link StemmerOverrideTokenFilter} mismatch.
 */
public final class StemmerOverrideTokenFilterConverter {
    public static StemmerOverrideTokenFilter convert(com.azure.search.documents.models.StemmerOverrideTokenFilter obj) {
        return DefaultConverter.convert(obj, StemmerOverrideTokenFilter.class);
    }

    public static com.azure.search.documents.models.StemmerOverrideTokenFilter convert(StemmerOverrideTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.StemmerOverrideTokenFilter.class);
    }
}
