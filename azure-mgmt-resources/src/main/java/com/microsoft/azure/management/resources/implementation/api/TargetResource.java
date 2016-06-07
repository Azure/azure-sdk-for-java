/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Target resource.
 */
public class TargetResource {
    /**
     * Gets or sets the ID of the resource.
     */
    private String id;

    /**
     * Gets or sets the name of the resource.
     */
    private String resourceName;

    /**
     * Gets or sets the type of the resource.
     */
    private String resourceType;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the TargetResource object itself.
     */
    public TargetResource withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the resourceName value.
     *
     * @return the resourceName value
     */
    public String resourceName() {
        return this.resourceName;
    }

    /**
     * Set the resourceName value.
     *
     * @param resourceName the resourceName value to set
     * @return the TargetResource object itself.
     */
    public TargetResource withResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    /**
     * Get the resourceType value.
     *
     * @return the resourceType value
     */
    public String resourceType() {
        return this.resourceType;
    }

    /**
     * Set the resourceType value.
     *
     * @param resourceType the resourceType value to set
     * @return the TargetResource object itself.
     */
    public TargetResource withResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

}
