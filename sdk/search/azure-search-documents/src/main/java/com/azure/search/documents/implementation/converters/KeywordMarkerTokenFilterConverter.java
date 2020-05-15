package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.KeywordMarkerTokenFilter} and
 * {@link KeywordMarkerTokenFilter} mismatch.
 */
public final class KeywordMarkerTokenFilterConverter {
    public static KeywordMarkerTokenFilter convert(com.azure.search.documents.models.KeywordMarkerTokenFilter obj) {
        return DefaultConverter.convert(obj, KeywordMarkerTokenFilter.class);
    }

    public static com.azure.search.documents.models.KeywordMarkerTokenFilter convert(KeywordMarkerTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.KeywordMarkerTokenFilter.class);
    }
}
