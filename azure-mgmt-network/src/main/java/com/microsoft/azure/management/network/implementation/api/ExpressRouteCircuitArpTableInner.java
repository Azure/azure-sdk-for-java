/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * The arp table associated with the ExpressRouteCircuit.
 */
public class ExpressRouteCircuitArpTableInner {
    /**
     * Gets ipAddress.
     */
    private String ipAddress;

    /**
     * Gets macAddress.
     */
    private String macAddress;

    /**
     * Get the ipAddress value.
     *
     * @return the ipAddress value
     */
    public String ipAddress() {
        return this.ipAddress;
    }

    /**
     * Set the ipAddress value.
     *
     * @param ipAddress the ipAddress value to set
     * @return the ExpressRouteCircuitArpTableInner object itself.
     */
    public ExpressRouteCircuitArpTableInner withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    /**
     * Get the macAddress value.
     *
     * @return the macAddress value
     */
    public String macAddress() {
        return this.macAddress;
    }

    /**
     * Set the macAddress value.
     *
     * @param macAddress the macAddress value to set
     * @return the ExpressRouteCircuitArpTableInner object itself.
     */
    public ExpressRouteCircuitArpTableInner withMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

}
