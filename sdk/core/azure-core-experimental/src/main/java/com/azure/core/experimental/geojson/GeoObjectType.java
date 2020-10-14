// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

/**
 * Represents the type of a GeoJSON object.
 */
public enum GeoObjectType {
    /**
     * GeoJSON point.
     */
    POINT,

    /**
     * GeoJSON multi-point.
     */
    MULTI_POINT,

    /**
     * GeoJSON polygon.
     */
    POLYGON,

    /**
     * GeoJSON multi-polygon.
     */
    MULTI_POLYGON,

    /**
     * GeoJSON line string.
     */
    LINE_STRING,

    /**
     * GeoJSON multi-line string.
     */
    MULTI_LINE_STRING,

    /**
     * GeoJSON geometry collection.
     */
    GEOMETRY_COLLECTION
}
