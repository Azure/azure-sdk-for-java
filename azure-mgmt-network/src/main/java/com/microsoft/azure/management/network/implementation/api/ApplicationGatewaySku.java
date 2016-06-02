/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * SKU of application gateway.
 */
public class ApplicationGatewaySku {
    /**
     * Gets or sets name of application gateway SKU. Possible values include:
     * 'Standard_Small', 'Standard_Medium', 'Standard_Large'.
     */
    private String name;

    /**
     * Gets or sets tier of application gateway. Possible values include:
     * 'Standard'.
     */
    private String tier;

    /**
     * Gets or sets capacity (instance count) of application gateway.
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
     * @return the ApplicationGatewaySku object itself.
     */
    public ApplicationGatewaySku withName(String name) {
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
     * @return the ApplicationGatewaySku object itself.
     */
    public ApplicationGatewaySku withTier(String tier) {
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
     * @return the ApplicationGatewaySku object itself.
     */
    public ApplicationGatewaySku withCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

}
