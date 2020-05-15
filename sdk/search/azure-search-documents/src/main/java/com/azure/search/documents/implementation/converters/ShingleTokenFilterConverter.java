package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ShingleTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ShingleTokenFilter} and
 * {@link ShingleTokenFilter} mismatch.
 */
public final class ShingleTokenFilterConverter {
    public static ShingleTokenFilter convert(com.azure.search.documents.models.ShingleTokenFilter obj) {
        return DefaultConverter.convert(obj, ShingleTokenFilter.class);
    }

    public static com.azure.search.documents.models.ShingleTokenFilter convert(ShingleTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ShingleTokenFilter.class);
    }
}
