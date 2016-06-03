/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * VirtualNetworkGatewaySku details.
 */
public class VirtualNetworkGatewaySku {
    /**
     * Gateway sku name -Basic/HighPerformance/Standard. Possible values
     * include: 'Basic', 'HighPerformance', 'Standard'.
     */
    private String name;

    /**
     * Gateway sku tier -Basic/HighPerformance/Standard. Possible values
     * include: 'Basic', 'HighPerformance', 'Standard'.
     */
    private String tier;

    /**
     * The capacity.
     */
    private Integer capacity;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the VirtualNetworkGatewaySku object itself.
     */
    public VirtualNetworkGatewaySku withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the tier value.
     *
     * @return the tier value
     */
    public String tier() {
        return this.tier;
    }

    /**
     * Set the tier value.
     *
     * @param tier the tier value to set
     * @return the VirtualNetworkGatewaySku object itself.
     */
    public VirtualNetworkGatewaySku withTier(String tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Get the capacity value.
     *
     * @return the capacity value
     */
    public Integer capacity() {
        return this.capacity;
    }

    /**
     * Set the capacity value.
     *
     * @param capacity the capacity value to set
     * @return the VirtualNetworkGatewaySku object itself.
     */
    public VirtualNetworkGatewaySku withCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

}
