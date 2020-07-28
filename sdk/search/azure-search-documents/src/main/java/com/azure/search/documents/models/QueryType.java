// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for QueryType.
 */
public enum QueryType {
    /**
     * Enum value simple.
     */
    SIMPLE("simple"),

    /**
     * Enum value full.
     */
    FULL("full");

    /**
     * The actual serialized value for a QueryType instance.
     */
    private final String value;

    QueryType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
