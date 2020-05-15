package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.CharFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.CharFilter} and
 * {@link CharFilter} mismatch.
 */
public final class CharFilterConverter {
    public static CharFilter convert(com.azure.search.documents.models.CharFilter obj) {
        return DefaultConverter.convert(obj, CharFilter.class);
    }

    public static com.azure.search.documents.models.CharFilter convert(CharFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.CharFilter.class);
    }
}
