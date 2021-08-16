// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents a geo position.
 */
@Immutable
public final class GeoPosition {
    private final ClientLogger logger = new ClientLogger(GeoPosition.class);

    private final double longitude;
    private final double latitude;

    private final Double altitude;

    /**
     * Constructs a geo position.
     *
     * @param longitude Longitudinal position.
     * @param latitude Latitudinal position.
     */
    public GeoPosition(double longitude, double latitude) {
        this(longitude, latitude, null);
    }

    /**
     * Constructs a geo position.
     *
     * @param longitude Longitudinal position.
     * @param latitude Latitudinal position.
     * @param altitude Altitude position.
     */
    public GeoPosition(double longitude, double latitude, Double altitude) {
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

    /**
     * Gets the number of coordinates used to compose the position.
     * <p>
     * This will return either 2 or 3 depending on whether {@link #getAltitude() altitude is set}.
     *
     * @return The number of coordinates used to compose the position.
     */
    public int count() {
        return (altitude == null) ? 2 : 3;
    }

    /**
     * Array accessor for the coordinates of this position.
     * <table>
     * <caption>Operation result mapping</caption>
     * <tr>
     * <th>Index</th>
     * <th>Result</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link #getLongitude() Longitude}</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link #getLatitude() Latitude}</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>{@link #getAltitude() Altitude} if non-null, otherwise {@link IndexOutOfBoundsException}</td>
     * </tr>
     * <tr>
     * <td>3+</td>
     * <td>{@link IndexOutOfBoundsException}</td>
     * </tr>
     * </table>
     *
     * @param index Coordinate index to access.
     * @return The coordinate value for the index.
     * @throws IndexOutOfBoundsException If {@code index} is equal to or greater than {@link #count()}.
     */
    double get(int index) {
        switch (index) {
            case 0:
                return longitude;
            case 1:
                return latitude;
            case 2:
                if (altitude == null) {
                    throw logger.logExceptionAsError(new IndexOutOfBoundsException("Index out of range: " + index));
                }

                return altitude;

            default:
                throw logger.logExceptionAsError(new IndexOutOfBoundsException("Index out of range: " + index));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, altitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPosition)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        GeoPosition other = (GeoPosition) obj;
        return Double.compare(longitude, other.longitude) == 0
            && Double.compare(latitude, other.latitude) == 0
            && Objects.equals(altitude, other.altitude);
    }

    @Override
    public String toString() {
        return (altitude != null)
            ? String.format("[%s, %s, %s]", longitude, latitude, altitude)
            : String.format("[%s, %s]", longitude, latitude);
    }
}
