// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for PhoneticEncoder.
 */
public enum PhoneticEncoder {
    /**
     * Enum value metaphone.
     */
    METAPHONE("metaphone"),

    /**
     * Enum value doubleMetaphone.
     */
    DOUBLE_METAPHONE("doubleMetaphone"),

    /**
     * Enum value soundex.
     */
    SOUNDEX("soundex"),

    /**
     * Enum value refinedSoundex.
     */
    REFINED_SOUNDEX("refinedSoundex"),

    /**
     * Enum value caverphone1.
     */
    CAVERPHONE1("caverphone1"),

    /**
     * Enum value caverphone2.
     */
    CAVERPHONE2("caverphone2"),

    /**
     * Enum value cologne.
     */
    COLOGNE("cologne"),

    /**
     * Enum value nysiis.
     */
    NYSIIS("nysiis"),

    /**
     * Enum value koelnerPhonetik.
     */
    KOELNER_PHONETIK("koelnerPhonetik"),

    /**
     * Enum value haasePhonetik.
     */
    HAASE_PHONETIK("haasePhonetik"),

    /**
     * Enum value beiderMorse.
     */
    BEIDER_MORSE("beiderMorse");

    /**
     * The actual serialized value for a PhoneticEncoder instance.
     */
    private final String value;

    PhoneticEncoder(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
