// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.serialization;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Fluent
@JsonInclude(Include.NON_NULL)
public class CustomDigitalTwin {

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID, required = true)
    private String id;

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG, required = true)
    private String etag;

    @JsonProperty(value = "AverageTemperature")
    private int averageTemperature;

    @JsonProperty(value = "TemperatureUnit")
    private String temperatureUnit;

    public String getId() {
        return id;
    }

    public CustomDigitalTwin setId(String id) {
        this.id = id;
        return this;
    }

    public String getETag() {
        return etag;
    }

    public CustomDigitalTwin setETag(String etag) {
        this.etag = etag;
        return this;
    }

    public int getAverageTemperature() {
        return averageTemperature;
    }

    public CustomDigitalTwin setAverageTemperature(int averageTemperature) {
        this.averageTemperature = averageTemperature;
        return this;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public CustomDigitalTwin setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
        return this;
    }
}

@Fluent
@JsonInclude(Include.NON_NULL)
class Metadata {

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.METADATA_MODEL, required = true)
    private String modelId;

    public String getModelId() {
        return modelId;
    }

    public Metadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }
}
