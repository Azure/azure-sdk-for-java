// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for AutocompleteMode.
 */
public enum AutocompleteMode {
    /**
     * Enum value oneTerm.
     */
    ONE_TERM("oneTerm"),

    /**
     * Enum value twoTerms.
     */
    TWO_TERMS("twoTerms"),

    /**
     * Enum value oneTermWithContext.
     */
    ONE_TERM_WITH_CONTEXT("oneTermWithContext");

    /**
     * The actual serialized value for a AutocompleteMode instance.
     */
    private final String value;

    AutocompleteMode(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
