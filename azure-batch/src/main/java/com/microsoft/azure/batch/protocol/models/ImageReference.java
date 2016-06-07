/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A reference to an Azure Virtual Machines Marketplace image.
 */
public class ImageReference {
    /**
     * The publisher of the Azure Virtual Machines Marketplace image. For
     * example, Canonical or MicrosoftWindowsServer.
     */
    @JsonProperty(required = true)
    private String publisher;

    /**
     * The offer type of the Azure Virtual Machines Marketplace image. For
     * example, UbuntuServer or WindowsServer.
     */
    @JsonProperty(required = true)
    private String offer;

    /**
     * The SKU of the Azure Virtual Machines Marketplace image. For example,
     * 14.04.0-LTS or 2012-R2-Datacenter.
     */
    @JsonProperty(required = true)
    private String sku;

    /**
     * The version of the Azure Virtual Machines Marketplace image. A value of
     * 'latest' can be specified to select the latest version of an image. If
     * omitted, the default is 'latest'.
     */
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
    public ImageReference withPublisher(String publisher) {
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
    public ImageReference withOffer(String offer) {
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
    public ImageReference withSku(String sku) {
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
    public ImageReference withVersion(String version) {
        this.version = version;
        return this;
    }

}
