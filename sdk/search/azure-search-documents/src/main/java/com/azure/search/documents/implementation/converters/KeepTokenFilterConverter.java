package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.KeepTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.KeepTokenFilter} and
 * {@link KeepTokenFilter} mismatch.
 */
public final class KeepTokenFilterConverter {
    public static KeepTokenFilter convert(com.azure.search.documents.models.KeepTokenFilter obj) {
        return DefaultConverter.convert(obj, KeepTokenFilter.class);
    }

    public static com.azure.search.documents.models.KeepTokenFilter convert(KeepTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.KeepTokenFilter.class);
    }
}
