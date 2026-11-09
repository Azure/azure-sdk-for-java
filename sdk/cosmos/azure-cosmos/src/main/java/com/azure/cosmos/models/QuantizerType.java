// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines quantizer types for vector index specifications in the Azure Cosmos DB service.
 */
public enum QuantizerType {
    /**
     * Represents a product quantizer type.
     */
    PRODUCT("product"),

    /**
     * Represents a spherical quantizer type.
     */
    SPHERICAL("spherical");


    QuantizerType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @JsonValue
    @Override
    public String toString() {
        return this.overWireValue;
    }
}

