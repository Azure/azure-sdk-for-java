// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.models;

import com.typespec.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Represents the type of GeoJSON object.
 */
public final class GeoObjectType extends ExpandableStringEnum<GeoObjectType> {
    /**
     * Creates a new instance of {@link GeoObjectType} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link GeoObjectType} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public GeoObjectType() {
    }

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
