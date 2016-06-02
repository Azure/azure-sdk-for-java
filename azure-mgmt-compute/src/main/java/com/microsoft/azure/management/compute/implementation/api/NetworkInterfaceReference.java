/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.SubResource;

/**
 * Describes a network interface reference.
 */
@JsonFlatten
public class NetworkInterfaceReference extends SubResource {
    /**
     * Gets or sets whether this is a primary NIC on a virtual machine.
     */
    @JsonProperty(value = "properties.primary")
    private Boolean primary;

    /**
     * Get the primary value.
     *
     * @return the primary value
     */
    public Boolean primary() {
        return this.primary;
    }

    /**
     * Set the primary value.
     *
     * @param primary the primary value to set
     * @return the NetworkInterfaceReference object itself.
     */
    public NetworkInterfaceReference withPrimary(Boolean primary) {
        this.primary = primary;
        return this;
    }

}
