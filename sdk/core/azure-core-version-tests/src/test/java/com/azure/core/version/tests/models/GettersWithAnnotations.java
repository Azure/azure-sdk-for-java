// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class GettersWithAnnotations {
    private int age;
    private String name;

    @JsonProperty("_age")
    public int getAge() {
        return age;
    }

    public GettersWithAnnotations setAge(int age) {
        this.age = age;
        return this;
    }

    @JsonProperty("_name")
    public String getName() {
        return name;
    }

    public GettersWithAnnotations setName(String name) {
        this.name = name;
        return this;
    }

    public void notAGetter() {
    }

    public int alsoNotAGetter(String parameter) {
        return 0;
    }
}
