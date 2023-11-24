// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class FutureTemperatureProperties {
    @JsonProperty
    private StringField date = new StringField();

    @JsonProperty
    private StringField locationName = new StringField();

    @JsonProperty
    private StringField unit = new StringField();

    @JsonGetter
    public StringField getDate() {
        return date;
    }

    @JsonSetter
    public void setDate(StringField date) {
        this.date = date;
    }

    @JsonGetter
    public StringField getLocationName() {
        return locationName;
    }

    @JsonSetter
    public void setLocationName(StringField locationName) {
        this.locationName = locationName;
    }

    @JsonGetter
    public StringField getUnit() {
        return unit;
    }

    @JsonSetter
    public void setUnit(StringField unit) {
        this.unit = unit;
    }
}
