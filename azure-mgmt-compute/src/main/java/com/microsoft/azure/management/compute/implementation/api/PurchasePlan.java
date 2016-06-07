/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for establishing the purchase context of any 3rd Party artifact
 * through MarketPlace.
 */
public class PurchasePlan {
    /**
     * Gets or sets the publisher ID.
     */
    @JsonProperty(required = true)
    private String publisher;

    /**
     * Gets or sets the plan ID.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the product ID.
     */
    @JsonProperty(required = true)
    private String product;

    /**
     * Get the publisher value.
     *
     * @return the publisher value
     */
    public String publisher() {
        return this.publisher;
    }

    /**
     * Set the publisher value.
     *
     * @param publisher the publisher value to set
     * @return the PurchasePlan object itself.
     */
    public PurchasePlan withPublisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

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
     * @return the PurchasePlan object itself.
     */
    public PurchasePlan withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the product value.
     *
     * @return the product value
     */
    public String product() {
        return this.product;
    }

    /**
     * Set the product value.
     *
     * @param product the product value to set
     * @return the PurchasePlan object itself.
     */
    public PurchasePlan withProduct(String product) {
        this.product = product;
        return this;
    }

}
