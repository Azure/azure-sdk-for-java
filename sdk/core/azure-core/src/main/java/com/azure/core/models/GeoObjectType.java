// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

/**
 * Represents the type of a GeoJSON object.
 */
public enum GeoObjectType {
    /**
     * GeoJSON point.
     */
    POINT("Point"),

    /**
     * GeoJSON multi-point.
     */
    MULTI_POINT("MultiPoint"),

    /**
     * GeoJSON polygon.
     */
    POLYGON("Polygon"),

    /**
     * GeoJSON multi-polygon.
     */
    MULTI_POLYGON("MultiPolygon"),

    /**
     * GeoJSON line string.
     */
    LINE_STRING("LineString"),

    /**
     * GeoJSON multi-line string.
     */
    MULTI_LINE_STRING("MultiLineString"),

    /**
     * GeoJSON geometry collection.
     */
    GEOMETRY_COLLECTION("GeometryCollection");

    private final String jsonType;

    GeoObjectType(String jsonType) {
        this.jsonType = jsonType;
    }

    /**
     * The GeoJSON type represented by the {@link GeoObjectType}.
     *
     * @return The GeoJSON type.
     */
    public String getJsonType() {
        return jsonType;
    }
}
