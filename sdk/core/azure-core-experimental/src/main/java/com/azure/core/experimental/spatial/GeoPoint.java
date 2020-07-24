// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.spatial;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a geometric point.
 */
public final class GeoPoint extends GeoObject {
    private final GeoPosition position;

    /**
     * Constructs a {@link GeoPoint}.
     *
     * @param longitude The longitudinal position of the point.
     * @param latitude The latitudinal position of the point.
     */
    public GeoPoint(double longitude, double latitude) {
        this(longitude, latitude, null);
    }

    /**
     * Constructs a {@link GeoPoint}.
     *
     * @param longitude The longitudinal position of the point.
     * @param latitude The latitudinal position of the point.
     * @param altitude The altitude of the point.
     */
    public GeoPoint(double longitude, double latitude, Double altitude) {
        this(new GeoPosition(longitude, latitude, altitude));
    }

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeoPosition geometric position} of the point.
     * @throws NullPointerException If {@code position} is {@code null}.
     */
    public GeoPoint(GeoPosition position) {
        this(position, null, null);
    }

    /**
     * Constructs a geometric point.
     *
     * @param position The {@link GeoPosition geometric position} of the point.
     * @param boundingBox Bounding box for the point.
     * @param properties Additional properties of the geometric point.
     * @throws NullPointerException If {@code position} is {@code null}.
     */
    public GeoPoint(GeoPosition position, GeoBoundingBox boundingBox, Map<String, Object> properties) {
        super(boundingBox, properties);

        Objects.requireNonNull(position, "'position' cannot be null.");
        this.position = position;
    }

    /**
     * The {@link GeoPosition geometric position} of the point.
     *
     * @return The {@link GeoPosition geometric position} of the point.
     */
    public GeoPosition getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPoint)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoPoint other = (GeoPoint) obj;

        return super.equals(obj) && Objects.equals(position, other.position);
    }
}
