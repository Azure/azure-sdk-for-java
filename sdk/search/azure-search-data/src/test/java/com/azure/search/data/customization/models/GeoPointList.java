package com.azure.search.data.customization.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class GeoPointList {
    @JsonProperty(value = "field")
    public List<GeoPoint> field;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoPointList)) return false;
        GeoPointList that = (GeoPointList) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }

    public GeoPointList field(List<GeoPoint> points) {
        field = points;
        return this;
    }
}
