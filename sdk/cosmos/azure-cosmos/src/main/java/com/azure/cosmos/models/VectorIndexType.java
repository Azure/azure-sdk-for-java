// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Defines the index type of vector index specification in the Azure Cosmos DB service.
 */
public enum VectorIndexType {
    /**
     * Represents a flat vector index type.
     */
    FLAT("Flat"),

    /**
     * Represents a quantized flat vector index type.
     */
    QUANTIZED_FLAT("QuantizedFlat"),

    /**
     * Represents a disk ANN vector index type.
     */
    DISK_ANN("DiskANN");


    private final String overWireValue;

    VectorIndexType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
