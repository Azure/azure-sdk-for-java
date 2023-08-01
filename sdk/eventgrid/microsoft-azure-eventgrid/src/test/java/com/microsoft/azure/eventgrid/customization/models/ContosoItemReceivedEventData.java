// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventgrid.customization.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContosoItemReceivedEventData {
    @JsonProperty(value = "itemSku", access = JsonProperty.Access.WRITE_ONLY)
    private String itemSku;

    @JsonProperty(value = "itemUri", access = JsonProperty.Access.WRITE_ONLY)
    private String itemUri;

    public String itemSku() {
        return this.itemSku;
    }

    public String itemUri() {
        return this.itemUri;
    }
}

