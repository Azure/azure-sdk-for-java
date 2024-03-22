// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Data types for the embeddings in Cosmos DB database service.
 */
public enum VectorDataType {
    /**
     * Represents a byte data type.
     */
    BYTE("Int8"),

    /**
     * Represents a float data type.
     */
    FLOAT("Float32");

    private final String overWireValue;

    VectorDataType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }

    public String getValue() {
        return this.overWireValue;
    }

    public boolean isEmpty() {
        return this.overWireValue.isEmpty();
    }
}
