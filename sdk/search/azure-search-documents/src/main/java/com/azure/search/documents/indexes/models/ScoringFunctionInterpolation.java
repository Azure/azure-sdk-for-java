// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ScoringFunctionInterpolation.
 */
public enum ScoringFunctionInterpolation {
    /**
     * Enum value linear.
     */
    LINEAR("linear"),

    /**
     * Enum value constant.
     */
    CONSTANT("constant"),

    /**
     * Enum value quadratic.
     */
    QUADRATIC("quadratic"),

    /**
     * Enum value logarithmic.
     */
    LOGARITHMIC("logarithmic");

    /**
     * The actual serialized value for a ScoringFunctionInterpolation instance.
     */
    private final String value;

    ScoringFunctionInterpolation(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
