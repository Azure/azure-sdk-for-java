// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "shippingType")
@JsonTypeName("Rocket")
public class RocketShippingInfo extends ShippingInfo {
    @JsonProperty(value = "rocketNumber", access = JsonProperty.Access.WRITE_ONLY)
    private String rocketNumber;

    /**
     * @return the rocket number.
     */
    public String getRocketNumber() {
        return this.rocketNumber;
    }
}
