// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.util.Arrays;

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

    /**
     * Method to validate the given value is accepted index type enum constant
     * @param value the value of the index type
     * @return the true if accepted value or else false
     */
    static boolean isValidType(String value) {
        return Arrays.stream(CosmosVectorIndexType.values())
            .anyMatch(vectorIndexType -> vectorIndexType.toString().equalsIgnoreCase(value));
    }
}
