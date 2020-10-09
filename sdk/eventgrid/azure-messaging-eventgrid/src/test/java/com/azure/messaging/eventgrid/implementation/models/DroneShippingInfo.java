// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "shippingType")
@JsonTypeName("Drone")
public class DroneShippingInfo extends ShippingInfo {
    @JsonProperty(value = "droneId", access = JsonProperty.Access.WRITE_ONLY)
    private String droneId;

    /**
     * @return the drone id.
     */
    public String getDroneId() {
        return this.droneId;
    }
}
