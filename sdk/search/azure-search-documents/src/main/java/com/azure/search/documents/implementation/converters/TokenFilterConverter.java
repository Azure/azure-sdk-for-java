package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TokenFilter} and
 * {@link TokenFilter} mismatch.
 */
public final class TokenFilterConverter {
    public static TokenFilter convert(com.azure.search.documents.models.TokenFilter obj) {
        return DefaultConverter.convert(obj, TokenFilter.class);
    }

    public static com.azure.search.documents.models.TokenFilter convert(TokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TokenFilter.class);
    }
}
