// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Data types for the embeddings in Cosmos DB database service.
 */
public enum CosmosVectorDataType {
    /**
     * Represents a int8 data type.
     */
    INT8("int8"),

    /**
     * Represents a uint8 data type.
     */
    UINT8("uint8"),

    /**
     * Represents a float16 data type.
     */
    FLOAT16("float16"),

    /**
     * Represents a float32 data type.
     */
    FLOAT32("float32");

    private final String overWireValue;

    CosmosVectorDataType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
