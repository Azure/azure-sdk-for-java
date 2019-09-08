package com.azure.search.data.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Bucket {

    @JsonProperty(value = "BucketName")
    public String bucketName;

    @JsonProperty(value = "Count")
    public int count;

    public Bucket bucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public Bucket count(int count) {
        this.count = count;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bucket)) return false;
        Bucket bucket = (Bucket) o;
        return count == bucket.count &&
            Objects.equals(bucketName, bucket.bucketName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketName, count);
    }
}
