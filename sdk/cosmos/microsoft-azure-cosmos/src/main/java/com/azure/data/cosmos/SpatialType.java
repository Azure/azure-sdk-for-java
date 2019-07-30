// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

/**
 * Defines the target data type of an index path specification in the Azure Cosmos DB service.
 * 
 */
public enum SpatialType {
    /**
     * Represent a point data type.
     */
    POINT,

    /**
     * Represent a line string data type.
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

