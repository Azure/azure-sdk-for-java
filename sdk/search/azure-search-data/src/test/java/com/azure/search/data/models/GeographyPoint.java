// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class GeographyPoint {
    @JsonProperty
    private String type;

    @JsonProperty
    private List<Double> coordinates;

    public String type(){
        return this.type;
    }

    public GeographyPoint type(String type){
        this.type = type;
        return this;
    }

    public List<Double> coordinates(){
        return this.coordinates;
    }

    public GeographyPoint coordinates(List<Double> coordinates){
        this.coordinates = coordinates;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeographyPoint)) return false;
        GeographyPoint that = (GeographyPoint) o;
        return Objects.equals(type, that.type) &&
            Objects.equals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, coordinates);
    }
}
