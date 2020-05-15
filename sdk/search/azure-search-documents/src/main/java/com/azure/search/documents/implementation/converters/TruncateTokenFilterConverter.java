package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TruncateTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TruncateTokenFilter} and
 * {@link TruncateTokenFilter} mismatch.
 */
public final class TruncateTokenFilterConverter {
    public static TruncateTokenFilter convert(com.azure.search.documents.models.TruncateTokenFilter obj) {
        return DefaultConverter.convert(obj, TruncateTokenFilter.class);
    }

    public static com.azure.search.documents.models.TruncateTokenFilter convert(TruncateTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TruncateTokenFilter.class);
    }
}
