// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.core.models.GeoPoint;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
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
        this.bools = CoreUtils.clone(bools);
        return this;
    }

    public Boolean[] bools() {
        return CoreUtils.clone(bools);
    }

    public ModelWithPrimitiveCollections dates(OffsetDateTime[] dates) {
        this.dates = CoreUtils.clone(dates);
        return this;
    }

    public OffsetDateTime[] dates() {
        return CoreUtils.clone(dates);
    }

    public ModelWithPrimitiveCollections doubles(Double[] doubles) {
        this.doubles = CoreUtils.clone(doubles);
        return this;
    }

    public Double[] doubles() {
        return CoreUtils.clone(doubles);
    }

    public ModelWithPrimitiveCollections ints(int[] ints) {
        this.ints = CoreUtils.clone(ints);
        return this;
    }

    public int[] ints() {
        return CoreUtils.clone(ints);
    }

    public ModelWithPrimitiveCollections longs(Long[] longs) {
        this.longs = CoreUtils.clone(longs);
        return this;
    }

    public Long[] longs() {
        return CoreUtils.clone(longs);
    }

    public ModelWithPrimitiveCollections points(GeoPoint[] points) {
        this.points = CoreUtils.clone(points);
        return this;
    }

    public GeoPoint[] points() {
        return CoreUtils.clone(points);
    }

    public ModelWithPrimitiveCollections strings(String[] strings) {
        this.strings = CoreUtils.clone(strings);
        return this;
    }

    public String[] strings() {
        return CoreUtils.clone(strings);
    }
}
