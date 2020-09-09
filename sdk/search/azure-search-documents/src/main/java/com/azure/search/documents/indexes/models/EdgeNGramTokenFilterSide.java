// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for EdgeNGramTokenFilterSide.
 */
public enum EdgeNGramTokenFilterSide {
    /**
     * Enum value front.
     */
    FRONT("front"),

    /**
     * Enum value back.
     */
    BACK("back");

    /**
     * The actual serialized value for a EdgeNGramTokenFilterSide instance.
     */
    private final String value;

    EdgeNGramTokenFilterSide(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
