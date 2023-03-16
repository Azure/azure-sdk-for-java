// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class NonNullableModel {

    @JsonProperty(value = "Key")
    private String key;

    @JsonProperty(value = "Rating")
    private int rating;

    @JsonProperty(value = "Count")
    private long count;

    @JsonProperty(value = "IsEnabled")
    private boolean isEnabled;

    @JsonProperty(value = "Ratio")
    private double ratio;

    @JsonProperty(value = "StartDate")
    private Date startDate;

    @JsonProperty(value = "EndDate")
    private Date endDate;

    @JsonProperty(value = "TopLevelBucket")
    private Bucket topLevelBucket;

    @JsonProperty(value = "Buckets")
    private Bucket[] buckets;

    public NonNullableModel key(String key) {
        this.key = key;
        return this;
    }

    public NonNullableModel rating(int rating) {
        this.rating = rating;
        return this;
    }

    public NonNullableModel count(long count) {
        this.count = count;
        return this;
    }

    public NonNullableModel isEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public NonNullableModel ratio(double ratio) {
        this.ratio = ratio;
        return this;
    }

    public NonNullableModel startDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public NonNullableModel endDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public NonNullableModel topLevelBucket(Bucket topLevelBucket) {
        this.topLevelBucket = topLevelBucket;
        return this;
    }

    public NonNullableModel buckets(Bucket[] buckets) {
        this.buckets = buckets;
        return this;
    }
}
