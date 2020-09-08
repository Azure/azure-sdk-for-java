// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.implementation.serialization.WritableProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

@Fluent
@JsonInclude(Include.NON_NULL)
public class CustomDigitalTwin {

    @JsonProperty(value = "$dtId", required = true)
    private String id;

    @JsonProperty(value = "$etag", required = true)
    private String etag;

    @JsonProperty(value = "$metadata", required = true)
    private CustomDigitalTwinMetadata metadata;

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

    public String getEtag() {
        return etag;
    }

    public CustomDigitalTwin setEtag(String etag) {
        this.etag = etag;
        return this;
    }

    public CustomDigitalTwinMetadata getMetadata() {
        return metadata;
    }

    public CustomDigitalTwin setMetadata(CustomDigitalTwinMetadata metadata) {
        this.metadata = metadata;
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

    @JsonProperty(value = "$model", required = true)
    private String modelId;

    public String getModelId() {
        return modelId;
    }

    public Metadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }
}

@Fluent
@JsonInclude(Include.NON_NULL)
class CustomDigitalTwinMetadata extends Metadata {

    @JsonProperty(value = "AverageTemperature")
    private WritableProperty averageTemperature;

    public WritableProperty getAverageTemperature() {
        return averageTemperature;
    }

    @JsonProperty(value = "TemperatureUnit")
    private WritableProperty temperatureUnit;

    public CustomDigitalTwinMetadata setAverageTemperature(WritableProperty averageTemperature) {
        this.averageTemperature = averageTemperature;
        return this;
    }
}
