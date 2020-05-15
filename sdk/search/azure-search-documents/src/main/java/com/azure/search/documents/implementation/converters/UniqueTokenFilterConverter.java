package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.UniqueTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.UniqueTokenFilter} and
 * {@link UniqueTokenFilter} mismatch.
 */
public final class UniqueTokenFilterConverter {
    public static UniqueTokenFilter convert(com.azure.search.documents.models.UniqueTokenFilter obj) {
        return DefaultConverter.convert(obj, UniqueTokenFilter.class);
    }

    public static com.azure.search.documents.models.UniqueTokenFilter convert(UniqueTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.UniqueTokenFilter.class);
    }
}
