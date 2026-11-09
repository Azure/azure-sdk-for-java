// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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
    public static CosmosVectorDataType fromString(String value) {
        return Arrays.stream(CosmosVectorDataType.values())
            .filter(vectorDataType -> vectorDataType.toString().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format(
                "Invalid vector data type with value {%s} for the vector embedding policy.", value)));
    }
}
