// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import java.util.Objects;

/**
 * Represents a geometric bounding box.
 */
public final class GeometryBoundingBox {
    private final double west;
    private final double south;
    private final double east;
    private final double north;

    private final Double minAltitude;
    private final Double maxAltitude;

    /**
     * Constructs a bounding box.
     *
     * @param west West longitudinal boundary.
     * @param south South latitudinal boundary.
     * @param east East longitudinal boundary.
     * @param north North latitudinal boundary.
     */
    public GeometryBoundingBox(double west, double south, double east, double north) {
        this(west, south, east, north, null, null);
    }

    /**
     * Constructs a bounding box.
     *
     * @param west West longitudinal boundary.
     * @param south South latitudinal boundary.
     * @param east East longitudinal boundary.
     * @param north North latitudinal boundary.
     * @param minAltitude Minimum altitude boundary.
     * @param maxAltitude Maximum altitude boundary.
     */
    public GeometryBoundingBox(double west, double south, double east, double north, Double minAltitude,
        Double maxAltitude) {
        this.west = west;
        this.south = south;
        this.east = east;
        this.north = north;
        this.minAltitude = minAltitude;
        this.maxAltitude = maxAltitude;
    }

    /**
     * The west longitudinal boundary of the bounding box.
     *
     * @return The west longitudinal boundary.
     */
    public double getWest() {
        return west;
    }

    /**
     * The south latitudinal boundary of the bounding box.
     *
     * @return The south latitudinal boundary.
     */
    public double getSouth() {
        return south;
    }

    /**
     * The east longitudinal boundary of the bounding box.
     *
     * @return The east longitudinal boundary.
     */
    public double getEast() {
        return east;
    }

    /**
     * The north latitudinal boundary of the bounding box.
     *
     * @return The north latitudinal boundary.
     */
    public double getNorth() {
        return north;
    }

    /**
     * The minimum altitude boundary of the bounding box.
     *
     * @return The minimum altitude boundary.
     */
    public Double getMinAltitude() {
        return minAltitude;
    }

    /**
     * The maximum altitude boundary of the bounding box.
     *
     * @return The maximum altitude boundary.
     */
    public Double getMaxAltitude() {
        return maxAltitude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(west, south, east, north, minAltitude, maxAltitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeometryBoundingBox)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeometryBoundingBox other = (GeometryBoundingBox) obj;
        return west == other.west
            && south == other.south
            && east == other.east
            && north == other.north
            && Objects.equals(minAltitude, other.minAltitude)
            && Objects.equals(maxAltitude, other.maxAltitude);
    }
}
