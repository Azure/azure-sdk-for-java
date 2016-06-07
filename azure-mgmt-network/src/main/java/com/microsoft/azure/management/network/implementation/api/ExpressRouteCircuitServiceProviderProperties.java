/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Contains ServiceProviderProperties in an ExpressRouteCircuit.
 */
public class ExpressRouteCircuitServiceProviderProperties {
    /**
     * Gets or sets serviceProviderName.
     */
    private String serviceProviderName;

    /**
     * Gets or sets peering location.
     */
    private String peeringLocation;

    /**
     * Gets or sets BandwidthInMbps.
     */
    private Integer bandwidthInMbps;

    /**
     * Get the serviceProviderName value.
     *
     * @return the serviceProviderName value
     */
    public String serviceProviderName() {
        return this.serviceProviderName;
    }

    /**
     * Set the serviceProviderName value.
     *
     * @param serviceProviderName the serviceProviderName value to set
     * @return the ExpressRouteCircuitServiceProviderProperties object itself.
     */
    public ExpressRouteCircuitServiceProviderProperties withServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
        return this;
    }

    /**
     * Get the peeringLocation value.
     *
     * @return the peeringLocation value
     */
    public String peeringLocation() {
        return this.peeringLocation;
    }

    /**
     * Set the peeringLocation value.
     *
     * @param peeringLocation the peeringLocation value to set
     * @return the ExpressRouteCircuitServiceProviderProperties object itself.
     */
    public ExpressRouteCircuitServiceProviderProperties withPeeringLocation(String peeringLocation) {
        this.peeringLocation = peeringLocation;
        return this;
    }

    /**
     * Get the bandwidthInMbps value.
     *
     * @return the bandwidthInMbps value
     */
    public Integer bandwidthInMbps() {
        return this.bandwidthInMbps;
    }

    /**
     * Set the bandwidthInMbps value.
     *
     * @param bandwidthInMbps the bandwidthInMbps value to set
     * @return the ExpressRouteCircuitServiceProviderProperties object itself.
     */
    public ExpressRouteCircuitServiceProviderProperties withBandwidthInMbps(Integer bandwidthInMbps) {
        this.bandwidthInMbps = bandwidthInMbps;
        return this;
    }

}
