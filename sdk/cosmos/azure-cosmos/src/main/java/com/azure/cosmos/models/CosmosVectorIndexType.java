// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Defines the index type of vector index specification in the Azure Cosmos DB service.
 */
public enum CosmosVectorIndexType {
    /**
     * Represents a flat vector index type.
     */
    FLAT("flat"),

    /**
     * Represents a quantized flat vector index type.
     */
    QUANTIZED_FLAT("quantizedFlat"),

    /**
     * Represents a disk ANN vector index type.
     */
    DISK_ANN("diskANN");


    private final String overWireValue;

    CosmosVectorIndexType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
