// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

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

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
