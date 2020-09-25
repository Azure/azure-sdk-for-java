// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContosoItemSentEventData {
    @JsonProperty(value = "shippingInfo", access = JsonProperty.Access.WRITE_ONLY)
    private ShippingInfo shippingInfo;

    /**
     * @return the shipping info.
     */
    public ShippingInfo getShippingInfo() {
        return this.shippingInfo;
    }
}
