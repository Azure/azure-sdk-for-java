// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Distance Function for the embeddings in the Cosmos DB database service.
 */
public enum DistanceFunction {
    /**
     * Represents the euclidean distance function.
     */
    EUCLIDEAN("EUCLIDEAN"),

    /**
     * Represents the cosine distance function.
     */
    COSINE("COSINE"),

    /**
     * Represents the dot product distance function.
     */
    DOT_PRODUCT("DOTPRODUCT");

    private final String overWireValue;

    DistanceFunction(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }

    /**
     *
     * @return value for the enum
     */
    public String getValue() {
        return this.overWireValue;
    }

    /**
     *
     * @return if the value for the enum is empty or not.
     */
    public boolean isEmpty() {
        return this.overWireValue.isEmpty();
    }
}
