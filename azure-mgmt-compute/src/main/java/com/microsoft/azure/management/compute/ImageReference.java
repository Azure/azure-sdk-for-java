/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.implementation.ImageReferenceInner;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The image reference.
 */
@LangDefinition
public class ImageReference extends WrapperImpl<ImageReferenceInner> {
    /**
     * the image resource id
     */
    private String id;

    /**
     * the image publisher.
     */
    private String publisher;

    /**
     * the image offer.
     */
    private String offer;

    /**
     * the image sku.
     */
    private String sku;

    /**
     * the image version. The allowed formats are Major.Minor.Build or
     * 'latest'. Major, Minor and Build being decimal numbers. Specify
     * 'latest' to use the latest version of image.
     */
    private String version;


    /**
     * Creates ImageReference.
     */
    public ImageReference() {
        super(null);
    }

    /**
     * Creates ImageReference.
     *
     * @param inner the inner object
     */
    public ImageReference(ImageReferenceInner inner) {
        super(inner);
        this.withPublisher(inner.publisher())
                .withOffer(inner.offer())
                .withVersion(inner.version())
                .withSku(inner.sku())
                .withId(inner.id());
    }

    @Override
    public ImageReferenceInner inner() {
        if (super.inner() != null) {
            return super.inner();
        }
        ImageReferenceInner imageReferenceInner = new ImageReferenceInner();
        imageReferenceInner
                .withPublisher(this.publisher())
                .withOffer(this.offer())
                .withVersion(this.version())
                .withSku(this.sku())
                .withId(this.id());
        return imageReferenceInner;
    }

    /**
     * @return the resource id of the image
     */
    public String id() {
        return this.id;
    }

    /**
     * Sets the image resource id value.
     * @param id the id
     * @return the ImageReference object itself.
     */
    public ImageReference withId(String id) {
        this.id = id;
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