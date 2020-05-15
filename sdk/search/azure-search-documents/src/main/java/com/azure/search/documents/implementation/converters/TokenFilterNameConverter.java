package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TokenFilterName;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TokenFilterName} and
 * {@link TokenFilterName} mismatch.
 */
public final class TokenFilterNameConverter {
    public static TokenFilterName convert(com.azure.search.documents.models.TokenFilterName obj) {
        return DefaultConverter.convert(obj, TokenFilterName.class);
    }

    public static com.azure.search.documents.models.TokenFilterName convert(TokenFilterName obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TokenFilterName.class);
    }
}
