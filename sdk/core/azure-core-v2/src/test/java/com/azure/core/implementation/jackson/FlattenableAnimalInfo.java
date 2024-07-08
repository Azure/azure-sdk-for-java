// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlattenableAnimalInfo {
    @JsonProperty(value = "home")
    private String home;

    @JsonProperty(value = "animal", required = true)
    private AnimalWithTypeIdContainingDot animal;

    public String home() {
        return this.home;
    }

    public FlattenableAnimalInfo withHome(String home) {
        this.home = home;
        return this;
    }

    public AnimalWithTypeIdContainingDot animal() {
        return this.animal;
    }

    public FlattenableAnimalInfo withAnimal(AnimalWithTypeIdContainingDot animal) {
        this.animal = animal;
        return this;
    }

}
