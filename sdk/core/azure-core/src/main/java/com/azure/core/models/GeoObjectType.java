// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Represents the type of a GeoJSON object.
 */
public final class GeoObjectType extends ExpandableStringEnum<GeoObjectType> {
    /**
     * GeoJSON point.
     */
    public static final GeoObjectType POINT = fromString("Point");

    /**
     * GeoJSON multi-point.
     */
    public static final GeoObjectType MULTI_POINT = fromString("MultiPoint");

    /**
     * GeoJSON polygon.
     */
    public static final GeoObjectType POLYGON = fromString("Polygon");

    /**
     * GeoJSON multi-polygon.
     */
    public static final GeoObjectType MULTI_POLYGON = fromString("MultiPolygon");

    /**
     * GeoJSON line string.
     */
    public static final GeoObjectType LINE_STRING = fromString("LineString");

    /**
     * GeoJSON multi-line string.
     */
    public static final GeoObjectType MULTI_LINE_STRING = fromString("MultiLineString");

    /**
     * GeoJSON geometry collection.
     */
    public static final GeoObjectType GEOMETRY_COLLECTION = fromString("GeometryCollection");

    /**
     * Creates or gets a GeoObjectType from its string representation.
     *
     * @param name Name of the GeoObjectType.
     * @return The corresponding GeoObjectType.
     */
    @JsonCreator
    public static GeoObjectType fromString(String name) {
        return fromString(name, GeoObjectType.class);
    }

    /**
     * Gets all known GeoObjectType values.
     *
     * @return All known GeoObjectType values.
     */
    public static Collection<GeoObjectType> values() {
        return values(GeoObjectType.class);
    }
}
