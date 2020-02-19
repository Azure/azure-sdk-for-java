// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.azure.search.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class ModelWithPrimitiveCollections {

    @JsonProperty(value = "Key")
    private String key;

    @JsonProperty(value = "Bools")
    private Boolean[] bools;

    @JsonProperty(value = "Dates")
    private OffsetDateTime[] dates;

    @JsonProperty(value = "Doubles")
    private Double[] doubles;

    @JsonProperty(value = "Ints")
    private int[] ints;

    @JsonProperty(value = "Longs")
    private Long[] longs;

    @JsonProperty(value = "Points")
    private GeoPoint[] points;

    @JsonProperty(value = "Strings")
    private String[] strings;

    public String key() {
        return this.key;
    }

    public ModelWithPrimitiveCollections key(String key) {
        this.key = key;
        return this;
    }

    public ModelWithPrimitiveCollections bools(Boolean[] bools) {
        this.bools = bools;
        return this;
    }

    public ModelWithPrimitiveCollections dates(OffsetDateTime[] dates) {
        this.dates = dates;
        return this;
    }

    public ModelWithPrimitiveCollections doubles(Double[] doubles) {
        this.doubles = doubles;
        return this;
    }

    public ModelWithPrimitiveCollections ints(int[] ints) {
        this.ints = ints;
        return this;
    }

    public ModelWithPrimitiveCollections longs(Long[] longs) {
        this.longs = longs;
        return this;
    }

    public ModelWithPrimitiveCollections points(GeoPoint[] points) {
        this.points = points;
        return this;
    }

    public ModelWithPrimitiveCollections strings(String[] strings) {
        this.strings = strings;
        return this;
    }
}
