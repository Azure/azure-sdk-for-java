/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Contains sku in an ExpressRouteCircuit.
 */
public class ExpressRouteCircuitSku {
    /**
     * Gets or sets name of the sku.
     */
    private String name;

    /**
     * Gets or sets tier of the sku. Possible values include: 'Standard',
     * 'Premium'.
     */
    private String tier;

    /**
     * Gets or sets family of the sku. Possible values include:
     * 'UnlimitedData', 'MeteredData'.
     */
    private String family;

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
     * @return the ExpressRouteCircuitSku object itself.
     */
    public ExpressRouteCircuitSku withName(String name) {
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
     * @return the ExpressRouteCircuitSku object itself.
     */
    public ExpressRouteCircuitSku withTier(String tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Get the family value.
     *
     * @return the family value
     */
    public String family() {
        return this.family;
    }

    /**
     * Set the family value.
     *
     * @param family the family value to set
     * @return the ExpressRouteCircuitSku object itself.
     */
    public ExpressRouteCircuitSku withFamily(String family) {
        this.family = family;
        return this;
    }

}
