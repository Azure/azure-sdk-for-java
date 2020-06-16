// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.TokenCharacterKind;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TokenCharacterKind} and
 * {@link TokenCharacterKind}.
 */
public final class TokenCharacterKindConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TokenCharacterKindConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.TokenCharacterKind} to enum
     * {@link TokenCharacterKind}.
     */
    public static TokenCharacterKind map(com.azure.search.documents.indexes.implementation.models.TokenCharacterKind obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case LETTER:
                return TokenCharacterKind.LETTER;
            case DIGIT:
                return TokenCharacterKind.DIGIT;
            case WHITESPACE:
                return TokenCharacterKind.WHITESPACE;
            case PUNCTUATION:
                return TokenCharacterKind.PUNCTUATION;
            case SYMBOL:
                return TokenCharacterKind.SYMBOL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link TokenCharacterKind} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.TokenCharacterKind}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TokenCharacterKind map(TokenCharacterKind obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case LETTER:
                return com.azure.search.documents.indexes.implementation.models.TokenCharacterKind.LETTER;
            case DIGIT:
                return com.azure.search.documents.indexes.implementation.models.TokenCharacterKind.DIGIT;
            case WHITESPACE:
                return com.azure.search.documents.indexes.implementation.models.TokenCharacterKind.WHITESPACE;
            case PUNCTUATION:
                return com.azure.search.documents.indexes.implementation.models.TokenCharacterKind.PUNCTUATION;
            case SYMBOL:
                return com.azure.search.documents.indexes.implementation.models.TokenCharacterKind.SYMBOL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private TokenCharacterKindConverter() {
    }
}
