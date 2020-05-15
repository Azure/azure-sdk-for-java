package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LengthTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LengthTokenFilter} and
 * {@link LengthTokenFilter} mismatch.
 */
public final class LengthTokenFilterConverter {
    public static LengthTokenFilter convert(com.azure.search.documents.models.LengthTokenFilter obj) {
        return DefaultConverter.convert(obj, LengthTokenFilter.class);
    }

    public static com.azure.search.documents.models.LengthTokenFilter convert(LengthTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LengthTokenFilter.class);
    }
}
