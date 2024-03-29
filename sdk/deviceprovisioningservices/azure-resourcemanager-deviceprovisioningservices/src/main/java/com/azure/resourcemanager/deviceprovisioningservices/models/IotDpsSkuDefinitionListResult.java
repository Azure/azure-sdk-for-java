// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.deviceprovisioningservices.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.IotDpsSkuDefinitionInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** List of available SKUs. */
@Fluent
public final class IotDpsSkuDefinitionListResult {
    /*
     * The list of SKUs
     */
    @JsonProperty(value = "value")
    private List<IotDpsSkuDefinitionInner> value;

    /*
     * The next link.
     */
    @JsonProperty(value = "nextLink", access = JsonProperty.Access.WRITE_ONLY)
    private String nextLink;

    /**
     * Get the value property: The list of SKUs.
     *
     * @return the value value.
     */
    public List<IotDpsSkuDefinitionInner> value() {
        return this.value;
    }

    /**
     * Set the value property: The list of SKUs.
     *
     * @param value the value value to set.
     * @return the IotDpsSkuDefinitionListResult object itself.
     */
    public IotDpsSkuDefinitionListResult withValue(List<IotDpsSkuDefinitionInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The next link.
     *
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() != null) {
            value().forEach(e -> e.validate());
        }
    }
}
