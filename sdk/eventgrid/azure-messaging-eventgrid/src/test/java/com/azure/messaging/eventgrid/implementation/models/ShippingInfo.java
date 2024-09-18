// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "shippingType")
@JsonTypeName("JobOutput")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Drone", value = DroneShippingInfo.class),
    @JsonSubTypes.Type(name = "Rocket", value = RocketShippingInfo.class)
})
public class ShippingInfo {
    @JsonProperty(value = "shipmentId", access = JsonProperty.Access.WRITE_ONLY)
    private String shipmentId;

    /**
     * @return the shipment id.
     */
    public String getShipmentId() {
        return this.shipmentId;
    }
}
