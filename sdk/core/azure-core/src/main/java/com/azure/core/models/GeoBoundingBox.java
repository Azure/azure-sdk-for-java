// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents a geometric bounding box.
 */
@Immutable
public final class GeoBoundingBox {
    private final ClientLogger logger = new ClientLogger(GeoBoundingBox.class);

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
    public GeoBoundingBox(double west, double south, double east, double north) {
        this(west, south, east, north, null, null, null);
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
    public GeoBoundingBox(double west, double south, double east, double north, double minAltitude,
        double maxAltitude) {
        this(west, south, east, north, minAltitude, maxAltitude, null);
    }

    /*
     * This constructor allows the one above to require both min altitude and max altitude to be non-null.
     */
    private GeoBoundingBox(double west, double south, double east, double north, Double minAltitude,
        Double maxAltitude, String ignored) {
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
        if (!(obj instanceof GeoBoundingBox)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoBoundingBox other = (GeoBoundingBox) obj;
        return Double.compare(west, other.west) == 0
            && Double.compare(south, other.south) == 0
            && Double.compare(east, other.east) == 0
            && Double.compare(north, other.north) == 0
            && Objects.equals(minAltitude, other.minAltitude)
            && Objects.equals(maxAltitude, other.maxAltitude);
    }

    /**
     * Accesses the coordinates of the {@link GeoBoundingBox} as if it were in a JSON representation.
     *
     * @param i Index to access.
     * @return The double value of the index.
     * @throws IndexOutOfBoundsException If the {@link GeoBoundingBox} doesn't have altitude coordinates and {@code i}
     * is greater than {@code 3} or {@link GeoBoundingBox} has altitude coordinates and {@code i} is greater than
     */
    double get(int i) {
        if (minAltitude != null && maxAltitude != null) {
            switch (i) {
                case 0:
                    return west;
                case 1:
                    return south;
                case 2:
                    return minAltitude;
                case 3:
                    return east;
                case 4:
                    return north;
                case 5:
                    return maxAltitude;
                default:
                    throw logger.logExceptionAsWarning(new IndexOutOfBoundsException("Index out of range: " + i));
            }
        } else {
            switch (i) {
                case 0:
                    return west;
                case 1:
                    return south;
                case 2:
                    return east;
                case 3:
                    return north;
                default:
                    throw logger.logExceptionAsWarning(new IndexOutOfBoundsException("Index out of range: " + i));
            }
        }
    }

    @Override
    public String toString() {
        return (minAltitude != null && maxAltitude != null)
            ? String.format("[%s, %s, %s, %s, %s, %s]", west, south, minAltitude, east, north, maxAltitude)
            : String.format("[%s, %s, %s, %s]", west, south, east, north);
    }
}
