// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Environment {
    @JsonGetter
    public String getDayOfMonth() {
        return dayOfMonth;
    }

    @JsonSetter
    public Environment setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    @JsonGetter
    public String getMonthOfYear() {
        return monthOfYear;
    }

    @JsonSetter
    public Environment setMonthOfYear(String monthOfYear) {
        this.monthOfYear = monthOfYear;
        return this;
    }

    @JsonGetter
    public String getWeather() {
        return weather;
    }

    @JsonSetter
    public Environment setWeather(String weather) {
        this.weather = weather;
        return this;
    }

    @JsonProperty
    String dayOfMonth;
    @JsonProperty
    String monthOfYear;
    @JsonProperty
    String weather;
}
