/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;

/**
 * Specfies the peering config.
 */
public class ExpressRouteCircuitPeeringConfig {
    /**
     * Gets or sets the reference of AdvertisedPublicPrefixes.
     */
    private List<String> advertisedPublicPrefixes;

    /**
     * Gets or sets AdvertisedPublicPrefixState of the Peering resource .
     * Possible values include: 'NotConfigured', 'Configuring', 'Configured',
     * 'ValidationNeeded'.
     */
    private String advertisedPublicPrefixesState;

    /**
     * Gets or Sets CustomerAsn of the peering.
     */
    private Integer customerASN;

    /**
     * Gets or Sets RoutingRegistryName of the config.
     */
    private String routingRegistryName;

    /**
     * Get the advertisedPublicPrefixes value.
     *
     * @return the advertisedPublicPrefixes value
     */
    public List<String> advertisedPublicPrefixes() {
        return this.advertisedPublicPrefixes;
    }

    /**
     * Set the advertisedPublicPrefixes value.
     *
     * @param advertisedPublicPrefixes the advertisedPublicPrefixes value to set
     * @return the ExpressRouteCircuitPeeringConfig object itself.
     */
    public ExpressRouteCircuitPeeringConfig withAdvertisedPublicPrefixes(List<String> advertisedPublicPrefixes) {
        this.advertisedPublicPrefixes = advertisedPublicPrefixes;
        return this;
    }

    /**
     * Get the advertisedPublicPrefixesState value.
     *
     * @return the advertisedPublicPrefixesState value
     */
    public String advertisedPublicPrefixesState() {
        return this.advertisedPublicPrefixesState;
    }

    /**
     * Set the advertisedPublicPrefixesState value.
     *
     * @param advertisedPublicPrefixesState the advertisedPublicPrefixesState value to set
     * @return the ExpressRouteCircuitPeeringConfig object itself.
     */
    public ExpressRouteCircuitPeeringConfig withAdvertisedPublicPrefixesState(String advertisedPublicPrefixesState) {
        this.advertisedPublicPrefixesState = advertisedPublicPrefixesState;
        return this;
    }

    /**
     * Get the customerASN value.
     *
     * @return the customerASN value
     */
    public Integer customerASN() {
        return this.customerASN;
    }

    /**
     * Set the customerASN value.
     *
     * @param customerASN the customerASN value to set
     * @return the ExpressRouteCircuitPeeringConfig object itself.
     */
    public ExpressRouteCircuitPeeringConfig withCustomerASN(Integer customerASN) {
        this.customerASN = customerASN;
        return this;
    }

    /**
     * Get the routingRegistryName value.
     *
     * @return the routingRegistryName value
     */
    public String routingRegistryName() {
        return this.routingRegistryName;
    }

    /**
     * Set the routingRegistryName value.
     *
     * @param routingRegistryName the routingRegistryName value to set
     * @return the ExpressRouteCircuitPeeringConfig object itself.
     */
    public ExpressRouteCircuitPeeringConfig withRoutingRegistryName(String routingRegistryName) {
        this.routingRegistryName = routingRegistryName;
        return this;
    }

}
