/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The routes table associated with the ExpressRouteCircuit.
 */
public class ExpressRouteCircuitRoutesTableInner {
    /**
     * Gets AddressPrefix.
     */
    private String addressPrefix;

    /**
     * Gets NextHopType. Possible values include: 'VirtualNetworkGateway',
     * 'VnetLocal', 'Internet', 'VirtualAppliance', 'None'.
     */
    @JsonProperty(required = true)
    private String nextHopType;

    /**
     * Gets NextHopIP.
     */
    private String nextHopIP;

    /**
     * Gets AsPath.
     */
    private String asPath;

    /**
     * Get the addressPrefix value.
     *
     * @return the addressPrefix value
     */
    public String addressPrefix() {
        return this.addressPrefix;
    }

    /**
     * Set the addressPrefix value.
     *
     * @param addressPrefix the addressPrefix value to set
     * @return the ExpressRouteCircuitRoutesTableInner object itself.
     */
    public ExpressRouteCircuitRoutesTableInner withAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
        return this;
    }

    /**
     * Get the nextHopType value.
     *
     * @return the nextHopType value
     */
    public String nextHopType() {
        return this.nextHopType;
    }

    /**
     * Set the nextHopType value.
     *
     * @param nextHopType the nextHopType value to set
     * @return the ExpressRouteCircuitRoutesTableInner object itself.
     */
    public ExpressRouteCircuitRoutesTableInner withNextHopType(String nextHopType) {
        this.nextHopType = nextHopType;
        return this;
    }

    /**
     * Get the nextHopIP value.
     *
     * @return the nextHopIP value
     */
    public String nextHopIP() {
        return this.nextHopIP;
    }

    /**
     * Set the nextHopIP value.
     *
     * @param nextHopIP the nextHopIP value to set
     * @return the ExpressRouteCircuitRoutesTableInner object itself.
     */
    public ExpressRouteCircuitRoutesTableInner withNextHopIP(String nextHopIP) {
        this.nextHopIP = nextHopIP;
        return this;
    }

    /**
     * Get the asPath value.
     *
     * @return the asPath value
     */
    public String asPath() {
        return this.asPath;
    }

    /**
     * Set the asPath value.
     *
     * @param asPath the asPath value to set
     * @return the ExpressRouteCircuitRoutesTableInner object itself.
     */
    public ExpressRouteCircuitRoutesTableInner withAsPath(String asPath) {
        this.asPath = asPath;
        return this;
    }

}
