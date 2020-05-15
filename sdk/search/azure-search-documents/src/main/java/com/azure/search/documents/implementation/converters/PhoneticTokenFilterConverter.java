package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.PhoneticTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.PhoneticTokenFilter} and
 * {@link PhoneticTokenFilter} mismatch.
 */
public final class PhoneticTokenFilterConverter {
    public static PhoneticTokenFilter convert(com.azure.search.documents.models.PhoneticTokenFilter obj) {
        return DefaultConverter.convert(obj, PhoneticTokenFilter.class);
    }

    public static com.azure.search.documents.models.PhoneticTokenFilter convert(PhoneticTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.PhoneticTokenFilter.class);
    }
}
