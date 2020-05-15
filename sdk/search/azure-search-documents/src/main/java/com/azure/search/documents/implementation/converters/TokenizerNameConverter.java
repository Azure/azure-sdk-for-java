package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TokenizerName;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TokenizerName} and
 * {@link TokenizerName} mismatch.
 */
public final class TokenizerNameConverter {
    public static TokenizerName convert(com.azure.search.documents.models.TokenizerName obj) {
        return DefaultConverter.convert(obj, TokenizerName.class);
    }

    public static com.azure.search.documents.models.TokenizerName convert(TokenizerName obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TokenizerName.class);
    }
}
