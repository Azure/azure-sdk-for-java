// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public final class Person {
    @JsonProperty
    private String name;

    @JsonSetter
    public Person setName(String name) {
        this.name = name;
        return this;
    }

    @JsonGetter
    public String getName() {
        return name;
    }
}
