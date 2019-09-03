// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization.models;

import com.azure.core.implementation.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Fluent
public class GeoPoint {
    public static final String TYPE = "Point";

    @JsonProperty
    private List<Double> coordinates;

    @JsonProperty("crs")
    private CoordinateSystem coordinateSystem;

    public List<Double> coordinates() {
        return this.coordinates;
    }

    public GeoPoint coordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public CoordinateSystem coordinateSystem() {
        return this.coordinateSystem;
    }

    public GeoPoint coordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        return this;
    }

    public static GeoPoint create(double latitude, double longitude) {
        return new GeoPoint().coordinates(Arrays.asList(latitude, longitude));
    }

    public static GeoPoint createWithDefaultCrs(double latitude, double longitude) {
        return create(latitude, longitude).coordinateSystem(CoordinateSystem.create());
    }

    public Map createObjectMap() {
        Map geoPointMap = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .convertValue(this, Map.class);
        geoPointMap.put("type", TYPE);
        return geoPointMap;
    }

    /**
     * Ensures that the GeoPoint values are valid for the Geography Point type in Search Service.
     *
     * @return true if valid, false if invalid
     */
    public boolean validate() {
        return coordinates != null && coordinates.size() == 2
            && coordinates.get(0) >= -180.0 && coordinates.get(0) <= 180.0
            && coordinates.get(1) >= -90.0 && coordinates.get(1) <= 90.0
            && (coordinateSystem == null || coordinateSystem.validate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GeoPoint other = (GeoPoint) o;
        if (!this.validate() || !other.validate())
            return false;
        return Objects.equals(coordinates.get(0), other.coordinates.get(0)) &&
            Objects.equals(coordinates.get(1), other.coordinates.get(1)) &&
            Objects.equals(coordinateSystem, other.coordinateSystem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, coordinateSystem);
    }

}
