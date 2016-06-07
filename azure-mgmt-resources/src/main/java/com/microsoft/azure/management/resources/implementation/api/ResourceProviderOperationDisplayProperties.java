/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Resource provider operation's display properties.
 */
public class ResourceProviderOperationDisplayProperties {
    /**
     * Gets or sets operation description.
     */
    private String publisher;

    /**
     * Gets or sets operation provider.
     */
    private String provider;

    /**
     * Gets or sets operation resource.
     */
    private String resource;

    /**
     * Gets or sets operation.
     */
    private String operation;

    /**
     * Gets or sets operation description.
     */
    private String description;

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
     * @return the ResourceProviderOperationDisplayProperties object itself.
     */
    public ResourceProviderOperationDisplayProperties withPublisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    /**
     * Get the provider value.
     *
     * @return the provider value
     */
    public String provider() {
        return this.provider;
    }

    /**
     * Set the provider value.
     *
     * @param provider the provider value to set
     * @return the ResourceProviderOperationDisplayProperties object itself.
     */
    public ResourceProviderOperationDisplayProperties withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Get the resource value.
     *
     * @return the resource value
     */
    public String resource() {
        return this.resource;
    }

    /**
     * Set the resource value.
     *
     * @param resource the resource value to set
     * @return the ResourceProviderOperationDisplayProperties object itself.
     */
    public ResourceProviderOperationDisplayProperties withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Get the operation value.
     *
     * @return the operation value
     */
    public String operation() {
        return this.operation;
    }

    /**
     * Set the operation value.
     *
     * @param operation the operation value to set
     * @return the ResourceProviderOperationDisplayProperties object itself.
     */
    public ResourceProviderOperationDisplayProperties withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the ResourceProviderOperationDisplayProperties object itself.
     */
    public ResourceProviderOperationDisplayProperties withDescription(String description) {
        this.description = description;
        return this;
    }

}
