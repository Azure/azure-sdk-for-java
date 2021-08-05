// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import java.util.Objects;

/**
 * Represents a geometric position.
 */
public final class GeometryPosition {
    private final double longitude;
    private final double latitude;

    private final Double altitude;

    /**
     * Constructs a geometric position.
     *
     * @param longitude Longitudinal position.
     * @param latitude Latitudinal position.
     */
    public GeometryPosition(double longitude, double latitude) {
        this(longitude, latitude, null);
    }

    /**
     * Constructs a geometric position.
     *
     * @param longitude Longitudinal position.
     * @param latitude Latitudinal position.
     * @param altitude Altitude position.
     */
    public GeometryPosition(double longitude, double latitude, Double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    /**
     * The longitudinal position of the geometric position.
     *
     * @return The longitudinal position.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * The latitudinal position of the geometric position.
     *
     * @return The latitudinal position.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * The altitude of the geometric position.
     *
     * @return The altitude.
     */
    public Double getAltitude() {
        return altitude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, altitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeometryPosition)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        GeometryPosition other = (GeometryPosition) obj;
        return longitude == other.longitude
            && latitude == other.latitude
            && Objects.equals(altitude, other.altitude);
    }
}
