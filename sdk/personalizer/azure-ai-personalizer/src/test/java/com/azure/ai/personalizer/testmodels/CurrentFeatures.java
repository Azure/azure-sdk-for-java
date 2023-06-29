// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class CurrentFeatures {
    @JsonGetter
    public String getDay() {
        return day;
    }

    @JsonSetter
    public CurrentFeatures setDay(String day) {
        this.day = day;
        return this;
    }

    @JsonGetter
    public String getWeather() {
        return weather;
    }

    @JsonSetter
    public CurrentFeatures setWeather(String weather) {
        this.weather = weather;
        return this;
    }

    @JsonProperty
    private String day;

    @JsonProperty
    private String weather;
}
