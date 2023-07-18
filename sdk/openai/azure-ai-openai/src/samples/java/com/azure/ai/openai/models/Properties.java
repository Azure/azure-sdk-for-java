// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Properties {
    @JsonProperty(value = "format")
    private Unit unit;

    @JsonProperty(value = "location")
    private Location location = new Location();

    @JsonGetter
    public Unit getUnit() {
        return unit;
    }

    @JsonSetter
    public Properties setUnit(Unit unit) {
        this.unit = unit;
        return this;
    }

    @JsonGetter
    public Location getLocation() {
        return location;
    }

    @JsonSetter
    public Properties setLocation(Location location) {
        this.location = location;
        return this;
    }
}
