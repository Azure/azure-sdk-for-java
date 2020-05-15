package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SnowballTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SnowballTokenFilter} and
 * {@link SnowballTokenFilter} mismatch.
 */
public final class SnowballTokenFilterConverter {
    public static SnowballTokenFilter convert(com.azure.search.documents.models.SnowballTokenFilter obj) {
        return DefaultConverter.convert(obj, SnowballTokenFilter.class);
    }

    public static com.azure.search.documents.models.SnowballTokenFilter convert(SnowballTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SnowballTokenFilter.class);
    }
}
