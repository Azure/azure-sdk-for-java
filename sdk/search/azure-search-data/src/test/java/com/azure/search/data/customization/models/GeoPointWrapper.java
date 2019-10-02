package com.azure.search.data.customization.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class GeoPointWrapper {

    @JsonProperty(value = "field")
    public GeoPoint field;

    public GeoPointWrapper field(GeoPoint geoPoint) {
        field = geoPoint;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoPointWrapper)) return false;
        GeoPointWrapper that = (GeoPointWrapper) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }
}
