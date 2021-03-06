/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.network.v2019_08_01.implementation;

import java.util.List;
import com.microsoft.azure.management.network.v2019_08_01.ApplicationGatewayBackendHealthPool;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for ApplicationGatewayBackendHealth API service call.
 */
public class ApplicationGatewayBackendHealthInner {
    /**
     * A list of ApplicationGatewayBackendHealthPool resources.
     */
    @JsonProperty(value = "backendAddressPools")
    private List<ApplicationGatewayBackendHealthPool> backendAddressPools;

    /**
     * Get a list of ApplicationGatewayBackendHealthPool resources.
     *
     * @return the backendAddressPools value
     */
    public List<ApplicationGatewayBackendHealthPool> backendAddressPools() {
        return this.backendAddressPools;
    }

    /**
     * Set a list of ApplicationGatewayBackendHealthPool resources.
     *
     * @param backendAddressPools the backendAddressPools value to set
     * @return the ApplicationGatewayBackendHealthInner object itself.
     */
    public ApplicationGatewayBackendHealthInner withBackendAddressPools(List<ApplicationGatewayBackendHealthPool> backendAddressPools) {
        this.backendAddressPools = backendAddressPools;
        return this;
    }

}
