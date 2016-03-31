/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The information about the platform or marketplace image.
 */
public class ImageReference {
    /**
     * Gets or sets the publisher of the image.
     */
    @JsonProperty(required = true)
    private String publisher;

    /**
     * Gets or sets the offer of the image.
     */
    @JsonProperty(required = true)
    private String offer;

    /**
     * Gets or sets the SKU of the image.
     */
    @JsonProperty(required = true)
    private String sku;

    /**
     * Gets or sets the version of the image. A value of 'latest' can be
     * specified to select the latest version of an image.
     */
    @JsonProperty(required = true)
    private String version;

    /**
     * Get the publisher value.
     *
     * @return the publisher value
     */
    public String getPublisher() {
        return this.publisher;
    }

    /**
     * Set the publisher value.
     *
     * @param publisher the publisher value to set
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * Get the offer value.
     *
     * @return the offer value
     */
    public String getOffer() {
        return this.offer;
    }

    /**
     * Set the offer value.
     *
     * @param offer the offer value to set
     */
    public void setOffer(String offer) {
        this.offer = offer;
    }

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public String getSku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Get the version value.
     *
     * @return the version value
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Set the version value.
     *
     * @param version the version value to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
