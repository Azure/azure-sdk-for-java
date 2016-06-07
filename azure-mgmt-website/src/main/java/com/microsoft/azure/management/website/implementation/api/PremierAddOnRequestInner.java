/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.Map;

/**
 * The PremierAddOnRequestInner model.
 */
public class PremierAddOnRequestInner {
    /**
     * Geo region resource belongs to e.g. SouthCentralUS, SouthEastAsia.
     */
    private String location;

    /**
     * Tags associated with resource.
     */
    private Map<String, String> tags;

    /**
     * Azure resource manager plan.
     */
    private ArmPlan plan;

    /**
     * Resource specific properties.
     */
    private Object properties;

    /**
     * Sku description of the resource.
     */
    private SkuDescription sku;

    /**
     * Get the location value.
     *
     * @return the location value
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the location value to set
     * @return the PremierAddOnRequestInner object itself.
     */
    public PremierAddOnRequestInner withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the PremierAddOnRequestInner object itself.
     */
    public PremierAddOnRequestInner withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the plan value.
     *
     * @return the plan value
     */
    public ArmPlan plan() {
        return this.plan;
    }

    /**
     * Set the plan value.
     *
     * @param plan the plan value to set
     * @return the PremierAddOnRequestInner object itself.
     */
    public PremierAddOnRequestInner withPlan(ArmPlan plan) {
        this.plan = plan;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public Object properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the PremierAddOnRequestInner object itself.
     */
    public PremierAddOnRequestInner withProperties(Object properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public SkuDescription sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the PremierAddOnRequestInner object itself.
     */
    public PremierAddOnRequestInner withSku(SkuDescription sku) {
        this.sku = sku;
        return this;
    }

}
