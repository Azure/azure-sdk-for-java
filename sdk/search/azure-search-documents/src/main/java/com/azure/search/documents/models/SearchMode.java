// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SearchMode.
 */
public enum SearchMode {
    /**
     * Enum value any.
     */
    ANY("any"),

    /**
     * Enum value all.
     */
    ALL("all");

    /**
     * The actual serialized value for a SearchMode instance.
     */
    private final String value;

    SearchMode(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
