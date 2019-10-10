// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NonNullableModel)) {
            return false;
        }
        NonNullableModel that = (NonNullableModel) o;
        return rating == that.rating
            && count == that.count
            && isEnabled == that.isEnabled
            && Double.compare(that.ratio, ratio) == 0
            && key.equals(that.key)
            && ((startDate == null && that.startDate == null) || (startDate.equals(that.startDate)))
            && ((endDate == null && that.endDate == null) || (endDate.equals(that.endDate)))
            && ((topLevelBucket == null && that.topLevelBucket == null) || (topLevelBucket.equals(that.topLevelBucket)))
            && Arrays.equals(buckets, that.buckets);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key, rating, count, isEnabled, ratio, startDate, endDate, topLevelBucket);
        result = 31 * result + Arrays.hashCode(buckets);
        return result;
    }
}
