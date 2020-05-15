package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LimitTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LimitTokenFilter} and
 * {@link LimitTokenFilter} mismatch.
 */
public final class LimitTokenFilterConverter {
    public static LimitTokenFilter convert(com.azure.search.documents.models.LimitTokenFilter obj) {
        return DefaultConverter.convert(obj, LimitTokenFilter.class);
    }

    public static com.azure.search.documents.models.LimitTokenFilter convert(LimitTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LimitTokenFilter.class);
    }
}
