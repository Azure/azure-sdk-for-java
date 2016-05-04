/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

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
    public String publisher() {
        return this.publisher;
    }

    /**
     * Set the publisher value.
     *
     * @param publisher the publisher value to set
     * @return the ImageReference object itself.
     */
    public ImageReference setPublisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    /**
     * Get the offer value.
     *
     * @return the offer value
     */
    public String offer() {
        return this.offer;
    }

    /**
     * Set the offer value.
     *
     * @param offer the offer value to set
     * @return the ImageReference object itself.
     */
    public ImageReference setOffer(String offer) {
        this.offer = offer;
        return this;
    }

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public String sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the ImageReference object itself.
     */
    public ImageReference setSku(String sku) {
        this.sku = sku;
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
     * @return the ImageReference object itself.
     */
    public ImageReference setVersion(String version) {
        this.version = version;
        return this;
    }

}
