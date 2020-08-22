// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.serialization.DigitalTwinMetadata;
import com.azure.digitaltwins.core.serialization.WritableProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Fluent
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomDigitalTwin {

    @JsonProperty(value = "$dtId", required = true)
    @Getter @Setter
    private String id;

    @JsonProperty(value = "$etag", required = true)
    @Getter @Setter
    private String etag;

    @JsonProperty(value = "$metadata", required = true)
    @Getter @Setter
    private CustomDigitalTwinMetadata metadata;

    @JsonProperty(value = "AverageTemperature")
    @Getter @Setter
    private int averageTemperature;

    @JsonProperty(value = "TemperatureUnit")
    @Getter @Setter
    private String temperatureUnit;
}

@Fluent
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Metadata {

    @JsonProperty(value = "$model", required = true)
    @Getter @Setter
    private String modelId;
}

@Fluent
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomDigitalTwinMetadata extends Metadata {

    @JsonProperty(value = "AverageTemperature")
    @Getter @Setter
    private WritableProperty averageTemperature;

    @JsonProperty(value = "TemperatureUnit")
    @Getter @Setter
    private WritableProperty temperatureUnit;
}
