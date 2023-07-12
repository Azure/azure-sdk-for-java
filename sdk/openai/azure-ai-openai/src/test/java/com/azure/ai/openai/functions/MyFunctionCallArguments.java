// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class MyFunctionCallArguments {
    @JsonProperty(value = "unit")
    private String unit;

    @JsonProperty(value = "location")
    private String location;

    @JsonGetter
    public String getUnit() {
        return unit;
    }

    @JsonSetter
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @JsonGetter
    public String getLocation() {
        return location;
    }

    @JsonSetter
    public void setLocation(String location) {
        this.location = location;
    }
}
