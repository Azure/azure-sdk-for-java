// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Representation of GeographyPoint as used Azure Cognitive Search.
 */
@Fluent
public final class GeoPoint {
    private static final String TYPE = "Point";

    @JsonProperty
    private List<Double> coordinates;

    @JsonProperty("crs")
    private CoordinateSystem coordinateSystem;

    private GeoPoint() {
        this.coordinateSystem = CoordinateSystem.create();
    }

    /**
     * Retrieve GeoPoint type
     * @return String type
     */
    @JsonProperty
    public String getType() {
        return TYPE;
    }

    /**
     * Create GeoPoint object from latitude and longitude
     * @param latitude latitude value of the GeographyPoint
     * @param longitude longitude value of the GeographyPoint
     * @return Add desc
     */
    public static GeoPoint create(double latitude, double longitude) {
        return new GeoPoint().setCoordinates(Arrays.asList(longitude, latitude));
    }

    /**
     * Create GeoPoint object from latitude, longitude and coordinate system
     * @param latitude latitude value of the GeographyPoint
     * @param longitude longitude value of the GeographyPoint
     * @param coordinateSystem EPSG:4326 coordination system
     * @return Add desc
     */
    public static GeoPoint create(double latitude, double longitude, CoordinateSystem coordinateSystem) {
        return create(latitude, longitude).setCoordinateSystem(coordinateSystem);
    }

    /**
     * Ensures that the GeoPoint values are valid for the Geography Point type in Azure Cognitive Search service.
     *
     * @return true if valid, false if invalid
     */
    public boolean isValid() {
        return coordinates != null && coordinates.size() == 2
            && coordinates.get(0) >= -180.0 && coordinates.get(0) <= 180.0
            && coordinates.get(1) >= -90.0 && coordinates.get(1) <= 90.0
            && (coordinateSystem == null || coordinateSystem.isValid());
    }

    /**
     * Checks equality between two Geo Points
     * @param o other GeoPoint
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoPoint other = (GeoPoint) o;
        if (!this.isValid() || !other.isValid()) {
            return false;
        }
        return Objects.equals(coordinates.get(0), other.coordinates.get(0))
            && Objects.equals(coordinates.get(1), other.coordinates.get(1))
            && Objects.equals(coordinateSystem, other.coordinateSystem);
    }

    /**
     * Returns hash code for Geo Point
     * @return int representing hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(coordinates, coordinateSystem);
    }

    /**
     * Retrieve GeoPoint string representation
     * @return String
     */
    @Override
    public String toString() {
        if (isValid()) {
            return String.format(
                Locale.US,
                "%+02f%+02f%s/",
                coordinates.get(1), coordinates.get(0), coordinateSystem.toString());
        }
        return "";
    }

    /**
     * Return latitude
     * @return value of latitude coordinate
     */
    public double getLatitude() {
        return coordinates.get(1);
    }

    /**
     * Return longitude
     * @return value of longitude coordinate
     */
    public double getLongitude() {
        return coordinates.get(0);
    }

    /**
     * Set coordinates
     * @param coordinates list of coordinates
     * @return GeoPoint updated
     */
    public GeoPoint setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    /**
     * Retrieve GeoPoint CoordinateSystem
     * @return CoordinateSystem
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set CoordinateSystem
     * @param coordinateSystem CoordinateSystem
     * @return GeoPoint updated
     */
    public GeoPoint setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        return this;
    }
}
