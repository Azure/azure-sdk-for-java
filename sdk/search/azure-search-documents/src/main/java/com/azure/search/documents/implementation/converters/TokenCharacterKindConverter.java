package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TokenCharacterKind;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TokenCharacterKind} and
 * {@link TokenCharacterKind} mismatch.
 */
public final class TokenCharacterKindConverter {
    public static TokenCharacterKind convert(com.azure.search.documents.models.TokenCharacterKind obj) {
        return DefaultConverter.convert(obj, TokenCharacterKind.class);
    }

    public static com.azure.search.documents.models.TokenCharacterKind convert(TokenCharacterKind obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TokenCharacterKind.class);
    }
}
