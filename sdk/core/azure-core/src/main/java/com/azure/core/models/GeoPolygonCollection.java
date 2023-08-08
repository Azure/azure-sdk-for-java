// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a collection of {@link GeoPolygon GeoPolygons}.
 */
@Immutable
public final class GeoPolygonCollection extends GeoObject {
    private final List<GeoPolygon> polygons;

    /**
     * Constructs a {@link GeoPolygonCollection}.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @throws NullPointerException If {@code polygons} is {@code null}.
     */
    public GeoPolygonCollection(List<GeoPolygon> polygons) {
        this(polygons, null, null);
    }

    /**
     * Constructs a {@link GeoPolygonCollection}.
     *
     * @param polygons The polygons that define the multi-polygon.
     * @param boundingBox Bounding box for the multi-polygon.
     * @param customProperties Additional properties of the multi-polygon.
     * @throws NullPointerException If {@code polygons} is {@code null}.
     */
    public GeoPolygonCollection(List<GeoPolygon> polygons, GeoBoundingBox boundingBox,
        Map<String, Object> customProperties) {
        super(boundingBox, customProperties);

        Objects.requireNonNull(polygons, "'polygons' cannot be null.");
        this.polygons = Collections.unmodifiableList(new ArrayList<>(polygons));
    }

    /**
     * Unmodifiable representation of the {@link GeoPolygon geometric polygons} representing this multi-polygon.
     *
     * @return An unmodifiable representation of the {@link GeoPolygon geometric polygons} representing this
     * multi-polygon.
     */
    public List<GeoPolygon> getPolygons() {
        return polygons;
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this multi-polygon.
     *
     * @return An unmodifiable representation of the {@link GeoPosition geometric positions} representing this
     * multi-polygon.
     */
    GeoArray<GeoArray<GeoArray<GeoPosition>>> getCoordinates() {
        return new GeoArray<>(this);
    }

    @Override
    public GeoObjectType getType() {
        return GeoObjectType.MULTI_POLYGON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(polygons, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPolygonCollection)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPolygonCollection other = (GeoPolygonCollection) obj;

        return super.equals(obj) && Objects.equals(polygons, other.polygons);
    }
}
