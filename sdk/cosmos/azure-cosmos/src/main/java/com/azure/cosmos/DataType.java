// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * Data types in the Azure Cosmos DB database service.
 */
public enum DataType {
    /**
     * Represents a numeric data type.
     */
    NUMBER("Number"),

    /**
     * Represents a string data type.
     */
    STRING("String"),

    /**
     * Represent a point data type.
     */
    POINT("Point"),

    /**
     * Represents a line string data type.
     */
    LINE_STRING("LineString"),

    /**
     * Represent a polygon data type.
     */
    POLYGON("Polygon"),

    /**
     * Represent a multi-polygon data type.
     */
    MULTI_POLYGON("MultiPolygon");

    DataType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
