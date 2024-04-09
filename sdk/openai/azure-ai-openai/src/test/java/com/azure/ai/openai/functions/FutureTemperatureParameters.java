// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class FutureTemperatureParameters {

    @JsonProperty(value = "type")
    private String type = "object";

    @JsonProperty(value = "properties")
    private FutureTemperatureProperties properties = new FutureTemperatureProperties();

    @JsonCreator
    public FutureTemperatureParameters(
            @JsonProperty(value = "type")
            String type,
            @JsonProperty(value = "properties")
            FutureTemperatureProperties properties
    ) {
        this.type = type;
        this.properties = properties;
    }

    @JsonCreator
    public FutureTemperatureParameters() {}

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter
    public FutureTemperatureProperties getProperties() {
        return properties;
    }

    @JsonSetter
    public void setProperties(FutureTemperatureProperties properties) {
        this.properties = properties;
    }
}
