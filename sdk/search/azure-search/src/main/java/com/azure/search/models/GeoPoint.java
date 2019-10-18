// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @JsonProperty
    public String getType() {
        return TYPE;
    }

    public static GeoPoint create(double latitude, double longitude) {
        return new GeoPoint().setCoordinates(Arrays.asList(longitude, latitude));
    }

    public static GeoPoint create(double latitude, double longitude, CoordinateSystem coordinateSystem) {
        return create(latitude, longitude).setCoordinateSystem(coordinateSystem);
    }

    /**
     * Ensures that the GeoPoint values are valid for the Geography Point type in Search Service.
     *
     * @return true if valid, false if invalid
     */
    public boolean isValid() {
        return coordinates != null && coordinates.size() == 2
            && coordinates.get(0) >= -180.0 && coordinates.get(0) <= 180.0
            && coordinates.get(1) >= -90.0 && coordinates.get(1) <= 90.0
            && (coordinateSystem == null || coordinateSystem.isValid());
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, coordinateSystem);
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public GeoPoint setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public GeoPoint setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        return this;
    }
}
