/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * The plan object in an ARM, represents a marketplace plan.
 */
public class ArmPlan {
    /**
     * The name.
     */
    private String name;

    /**
     * The publisher.
     */
    private String publisher;

    /**
     * The product.
     */
    private String product;

    /**
     * The promotion code.
     */
    private String promotionCode;

    /**
     * Version of product.
     */
    private String version;

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
     * @return the ArmPlan object itself.
     */
    public ArmPlan withName(String name) {
        this.name = name;
        return this;
    }

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
     * @return the ArmPlan object itself.
     */
    public ArmPlan withPublisher(String publisher) {
        this.publisher = publisher;
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
     * @return the ArmPlan object itself.
     */
    public ArmPlan withProduct(String product) {
        this.product = product;
        return this;
    }

    /**
     * Get the promotionCode value.
     *
     * @return the promotionCode value
     */
    public String promotionCode() {
        return this.promotionCode;
    }

    /**
     * Set the promotionCode value.
     *
     * @param promotionCode the promotionCode value to set
     * @return the ArmPlan object itself.
     */
    public ArmPlan withPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
        return this;
    }

    /**
     * Get the version value.
     *
     * @return the version value
     */
    public String version() {
        return this.version;
    }

    /**
     * Set the version value.
     *
     * @param version the version value to set
     * @return the ArmPlan object itself.
     */
    public ArmPlan withVersion(String version) {
        this.version = version;
        return this;
    }

}
