// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.v2.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonFlatten
public class AnimalShelter {
    @JsonProperty(value = "properties.description")
    private String description;

    @JsonProperty(value = "properties.animalsInfo", required = true)
    private List<FlattenableAnimalInfo> animalsInfo;

    public String description() {
        return this.description;
    }

    public AnimalShelter withDescription(String description) {
        this.description = description;
        return this;
    }

    public List<FlattenableAnimalInfo> animalsInfo() {
        return this.animalsInfo;
    }

    public AnimalShelter withAnimalsInfo(List<FlattenableAnimalInfo> animalsInfo) {
        this.animalsInfo = animalsInfo;
        return this;
    }
}
