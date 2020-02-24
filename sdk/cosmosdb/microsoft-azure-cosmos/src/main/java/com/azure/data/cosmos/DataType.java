// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

/**
 * Data types in the Azure Cosmos DB database service.
 */
public enum DataType {
    /**
     * Represents a numeric data type.
     */
    NUMBER,

    /**
     * Represents a string data type.
     */
    STRING,

    /**
     * Represent a point data type.
     */
    POINT,

    /**
     * Represents a line string data type.
     */
    LINE_STRING,

    /**
     * Represent a polygon data type.
     */
    POLYGON,
    
    /**
     * Represent a multi-polygon data type.
     */
    MULTI_POLYGON;
    
    @Override
    public String toString() {
        return StringUtils.remove(WordUtils.capitalizeFully(this.name(), '_'), '_');        
    }    
}
