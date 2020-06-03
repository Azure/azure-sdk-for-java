// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ScoringFunctionAggregation.
 */
public enum ScoringFunctionAggregation {
    /**
     * Enum value sum.
     */
    SUM("sum"),

    /**
     * Enum value average.
     */
    AVERAGE("average"),

    /**
     * Enum value minimum.
     */
    MINIMUM("minimum"),

    /**
     * Enum value maximum.
     */
    MAXIMUM("maximum"),

    /**
     * Enum value firstMatching.
     */
    FIRST_MATCHING("firstMatching");

    /**
     * The actual serialized value for a ScoringFunctionAggregation instance.
     */
    private final String value;

    ScoringFunctionAggregation(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
