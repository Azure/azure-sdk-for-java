/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The resource group properties.
 */
public class ResourceGroupProperties {
    /**
     * Gets resource group provisioning state.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String provisioningState;

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

}
