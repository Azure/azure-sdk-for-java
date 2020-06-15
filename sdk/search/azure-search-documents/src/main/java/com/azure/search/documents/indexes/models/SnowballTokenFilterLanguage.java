// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SnowballTokenFilterLanguage.
 */
public enum SnowballTokenFilterLanguage {
    /**
     * Enum value armenian.
     */
    ARMENIAN("armenian"),

    /**
     * Enum value basque.
     */
    BASQUE("basque"),

    /**
     * Enum value catalan.
     */
    CATALAN("catalan"),

    /**
     * Enum value danish.
     */
    DANISH("danish"),

    /**
     * Enum value dutch.
     */
    DUTCH("dutch"),

    /**
     * Enum value english.
     */
    ENGLISH("english"),

    /**
     * Enum value finnish.
     */
    FINNISH("finnish"),

    /**
     * Enum value french.
     */
    FRENCH("french"),

    /**
     * Enum value german.
     */
    GERMAN("german"),

    /**
     * Enum value german2.
     */
    GERMAN2("german2"),

    /**
     * Enum value hungarian.
     */
    HUNGARIAN("hungarian"),

    /**
     * Enum value italian.
     */
    ITALIAN("italian"),

    /**
     * Enum value kp.
     */
    KP("kp"),

    /**
     * Enum value lovins.
     */
    LOVINS("lovins"),

    /**
     * Enum value norwegian.
     */
    NORWEGIAN("norwegian"),

    /**
     * Enum value porter.
     */
    PORTER("porter"),

    /**
     * Enum value portuguese.
     */
    PORTUGUESE("portuguese"),

    /**
     * Enum value romanian.
     */
    ROMANIAN("romanian"),

    /**
     * Enum value russian.
     */
    RUSSIAN("russian"),

    /**
     * Enum value spanish.
     */
    SPANISH("spanish"),

    /**
     * Enum value swedish.
     */
    SWEDISH("swedish"),

    /**
     * Enum value turkish.
     */
    TURKISH("turkish");

    /**
     * The actual serialized value for a SnowballTokenFilterLanguage instance.
     */
    private final String value;

    SnowballTokenFilterLanguage(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
