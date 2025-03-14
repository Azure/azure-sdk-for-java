// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.geo;

import io.clientcore.core.utils.ExpandableEnum;

import java.util.Objects;

/**
 * <p>Represents the type of GeoJSON object.</p>
 *
 * <p>This class encapsulates the type of GeoJSON object. It provides constants for the different types of
 * GeoJSON objects, such as {@link #POINT}, {@link #MULTI_POINT}, {@link #POLYGON}, {@link #MULTI_POLYGON},
 * {@link #LINE_STRING}, {@link #MULTI_LINE_STRING}, and {@link #GEOMETRY_COLLECTION}.</p>
 *
 * <p>This class is useful when you want to work with GeoJSON objects and need to specify or check the type of
 * GeoJSON object.</p>
 *
 * @see ExpandableEnum
 */
public final class GeoObjectType implements ExpandableEnum<String> {
    private final String value;

    private GeoObjectType(String value) {
        this.value = value;
    }

    /**
     * GeoJSON point.
     */
    public static final GeoObjectType POINT = new GeoObjectType("Point");

    /**
     * GeoJSON multi-point.
     */
    public static final GeoObjectType MULTI_POINT = new GeoObjectType("MultiPoint");

    /**
     * GeoJSON polygon.
     */
    public static final GeoObjectType POLYGON = new GeoObjectType("Polygon");

    /**
     * GeoJSON multi-polygon.
     */
    public static final GeoObjectType MULTI_POLYGON = new GeoObjectType("MultiPolygon");

    /**
     * GeoJSON line string.
     */
    public static final GeoObjectType LINE_STRING = new GeoObjectType("LineString");

    /**
     * GeoJSON multi-line string.
     */
    public static final GeoObjectType MULTI_LINE_STRING = new GeoObjectType("MultiLineString");

    /**
     * GeoJSON geometry collection.
     */
    public static final GeoObjectType GEOMETRY_COLLECTION = new GeoObjectType("GeometryCollection");

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoObjectType)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoObjectType other = (GeoObjectType) obj;
        return Objects.equals(value, other.value);
    }
}
