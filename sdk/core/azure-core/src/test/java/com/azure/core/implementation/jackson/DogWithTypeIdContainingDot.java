// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = DogWithTypeIdContainingDot.class)
@JsonTypeName("#Favourite.Pet.DogWithTypeIdContainingDot")
public class DogWithTypeIdContainingDot extends AnimalWithTypeIdContainingDot {
    @JsonProperty(value = "breed")
    private String breed;

    // Flattenable property
    @JsonProperty(value = "properties.cuteLevel")
    private Integer cuteLevel;

    public String breed() {
        return this.breed;
    }

    public DogWithTypeIdContainingDot withBreed(String audioLanguage) {
        this.breed = audioLanguage;
        return this;
    }

    public Integer cuteLevel() {
        return this.cuteLevel;
    }

    public DogWithTypeIdContainingDot withCuteLevel(Integer cuteLevel) {
        this.cuteLevel = cuteLevel;
        return this;
    }
}
