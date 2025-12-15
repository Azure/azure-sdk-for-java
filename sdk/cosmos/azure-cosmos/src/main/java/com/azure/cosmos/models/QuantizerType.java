// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Defines the quantizer type of vector index path specification in the Azure Cosmos DB service.
 */
public enum QuantizerType {
    /**
     * Represent a product quantizer type.
     */
    product("product"),

    /**
     * Represent a spherical quantizer type.
     */
    spherical("spherical");


    QuantizerType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}

