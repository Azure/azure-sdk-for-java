// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

/**
 * Defines the target data type of an index path specification in the Azure Cosmos DB service.
 */
public enum SpatialType {
    /**
     * Represent a point data type.
     */
    POINT("Point"),

    /**
     * Represent a line string data type.
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

    SpatialType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}

