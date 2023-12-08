// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class FutureTemperatureArguments {

    @JsonProperty(value = "date")
    private String date;

    @JsonProperty(value = "locationName")
    private String locationName;

    @JsonProperty(value = "unit")
    private String unit;

    @JsonCreator
    public FutureTemperatureArguments(@JsonProperty(value = "locationName") String locationName,
                                      @JsonProperty(value = "date") String date,
                                      @JsonProperty(value = "unit") String unit) {
        this.locationName = locationName;
        this.date = date;
        this.unit = unit;
    }

    @JsonGetter
    public String getDate() {
        return date;
    }

    @JsonSetter
    public void setDate(String date) {
        this.date = date;
    }

    @JsonGetter
    public String getLocationName() {
        return locationName;
    }

    @JsonSetter
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @JsonGetter
    public String getUnit() {
        return unit;
    }

    @JsonSetter
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
