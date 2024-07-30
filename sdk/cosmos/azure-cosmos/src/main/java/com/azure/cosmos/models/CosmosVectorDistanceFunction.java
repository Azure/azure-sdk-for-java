// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Distance Function for the embeddings in the Cosmos DB database service.
 */
public enum CosmosVectorDistanceFunction {
    /**
     * Represents the euclidean distance function.
     */
    EUCLIDEAN("euclidean"),

    /**
     * Represents the cosine distance function.
     */
    COSINE("cosine"),

    /**
     * Represents the dot product distance function.
     */
    DOT_PRODUCT("dotproduct");

    private final String overWireValue;

    CosmosVectorDistanceFunction(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.overWireValue;
    }

    /**
     * Method to retrieve the enum constant by its overWireValue.
     * @param value the overWire value of the enum constant
     * @return the matching CosmosVectorDataType
     * @throws IllegalArgumentException if no matching enum constant is found
     */
    public static CosmosVectorDistanceFunction fromString(String value) {
        return Arrays.stream(CosmosVectorDistanceFunction.values())
            .filter(vectorDistanceFunction -> vectorDistanceFunction.toString().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format(
                "Invalid distance function with value {%s} for the vector embedding policy.", value )));
    }
}
