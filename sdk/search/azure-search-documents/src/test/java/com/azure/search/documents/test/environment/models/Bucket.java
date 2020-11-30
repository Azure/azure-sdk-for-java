// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

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
}
