// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Properties {
    @JsonProperty(value = "unit")
    private Unit unit = new Unit();

    @JsonProperty
    private Location location = new Location();

    @JsonGetter
    public Unit getUnit() {
        return unit;
    }

    @JsonSetter
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @JsonGetter
    public Location getLocation() {
        return location;
    }

    @JsonSetter
    public void setLocation(Location location) {
        this.location = location;
    }
}
