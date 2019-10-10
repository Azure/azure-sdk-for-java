// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.azure.search.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModelWithPrimitiveCollections)) {
            return false;
        }
        ModelWithPrimitiveCollections that = (ModelWithPrimitiveCollections) o;
        return Objects.equals(key, that.key)
            && Arrays.equals(bools, that.bools)
            && Arrays.equals(dates, that.dates)
            && Arrays.equals(doubles, that.doubles)
            && Arrays.equals(ints, that.ints)
            && Arrays.equals(longs, that.longs)
            && Arrays.equals(points, that.points)
            && Arrays.equals(strings, that.strings);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key);
        result = 31 * result + Arrays.hashCode(bools);
        result = 31 * result + Arrays.hashCode(dates);
        result = 31 * result + Arrays.hashCode(doubles);
        result = 31 * result + Arrays.hashCode(ints);
        result = 31 * result + Arrays.hashCode(longs);
        result = 31 * result + Arrays.hashCode(points);
        result = 31 * result + Arrays.hashCode(strings);
        return result;
    }
}
