/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Describes a sku for a scalable resource.
 */
public class SkuDescription {
    /**
     * Name of the resource sku.
     */
    private String name;

    /**
     * Service Tier of the resource sku.
     */
    private String tier;

    /**
     * Size specifier of the resource sku.
     */
    private String size;

    /**
     * Family code of the resource sku.
     */
    private String family;

    /**
     * Current number of instances assigned to the resource.
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
     * @return the SkuDescription object itself.
     */
    public SkuDescription withName(String name) {
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
     * @return the SkuDescription object itself.
     */
    public SkuDescription withTier(String tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Get the size value.
     *
     * @return the size value
     */
    public String size() {
        return this.size;
    }

    /**
     * Set the size value.
     *
     * @param size the size value to set
     * @return the SkuDescription object itself.
     */
    public SkuDescription withSize(String size) {
        this.size = size;
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
     * @return the SkuDescription object itself.
     */
    public SkuDescription withFamily(String family) {
        this.family = family;
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
     * @return the SkuDescription object itself.
     */
    public SkuDescription withCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

}
