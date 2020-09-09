// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CjkBigramTokenFilterScripts.
 */
public enum CjkBigramTokenFilterScripts {
    /**
     * Enum value han.
     */
    HAN("han"),

    /**
     * Enum value hiragana.
     */
    HIRAGANA("hiragana"),

    /**
     * Enum value katakana.
     */
    KATAKANA("katakana"),

    /**
     * Enum value hangul.
     */
    HANGUL("hangul");

    /**
     * The actual serialized value for a CjkBigramTokenFilterScripts instance.
     */
    private final String value;

    CjkBigramTokenFilterScripts(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
