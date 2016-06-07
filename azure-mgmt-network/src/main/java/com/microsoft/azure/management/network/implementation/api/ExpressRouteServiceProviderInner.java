/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * ExpressRouteResourceProvider object.
 */
@JsonFlatten
public class ExpressRouteServiceProviderInner extends Resource {
    /**
     * Gets or list of peering locations.
     */
    @JsonProperty(value = "properties.peeringLocations")
    private List<String> peeringLocations;

    /**
     * Gets or bandwidths offered.
     */
    @JsonProperty(value = "properties.bandwidthsOffered")
    private List<ExpressRouteServiceProviderBandwidthsOffered> bandwidthsOffered;

    /**
     * Gets or sets Provisioning state of the resource.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Get the peeringLocations value.
     *
     * @return the peeringLocations value
     */
    public List<String> peeringLocations() {
        return this.peeringLocations;
    }

    /**
     * Set the peeringLocations value.
     *
     * @param peeringLocations the peeringLocations value to set
     * @return the ExpressRouteServiceProviderInner object itself.
     */
    public ExpressRouteServiceProviderInner withPeeringLocations(List<String> peeringLocations) {
        this.peeringLocations = peeringLocations;
        return this;
    }

    /**
     * Get the bandwidthsOffered value.
     *
     * @return the bandwidthsOffered value
     */
    public List<ExpressRouteServiceProviderBandwidthsOffered> bandwidthsOffered() {
        return this.bandwidthsOffered;
    }

    /**
     * Set the bandwidthsOffered value.
     *
     * @param bandwidthsOffered the bandwidthsOffered value to set
     * @return the ExpressRouteServiceProviderInner object itself.
     */
    public ExpressRouteServiceProviderInner withBandwidthsOffered(List<ExpressRouteServiceProviderBandwidthsOffered> bandwidthsOffered) {
        this.bandwidthsOffered = bandwidthsOffered;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the ExpressRouteServiceProviderInner object itself.
     */
    public ExpressRouteServiceProviderInner withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

}
