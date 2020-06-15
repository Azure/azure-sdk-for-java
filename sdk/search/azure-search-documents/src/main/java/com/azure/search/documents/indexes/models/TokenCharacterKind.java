// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for TokenCharacterKind.
 */
public enum TokenCharacterKind {
    /**
     * Enum value letter.
     */
    LETTER("letter"),

    /**
     * Enum value digit.
     */
    DIGIT("digit"),

    /**
     * Enum value whitespace.
     */
    WHITESPACE("whitespace"),

    /**
     * Enum value punctuation.
     */
    PUNCTUATION("punctuation"),

    /**
     * Enum value symbol.
     */
    SYMBOL("symbol");

    /**
     * The actual serialized value for a TokenCharacterKind instance.
     */
    private final String value;

    TokenCharacterKind(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
