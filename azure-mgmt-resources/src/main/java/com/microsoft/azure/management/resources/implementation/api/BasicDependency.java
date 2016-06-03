/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Deployment dependency information.
 */
public class BasicDependency {
    /**
     * Gets or sets the ID of the dependency.
     */
    private String id;

    /**
     * Gets or sets the dependency resource type.
     */
    private String resourceType;

    /**
     * Gets or sets the dependency resource name.
     */
    private String resourceName;

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
     * @return the BasicDependency object itself.
     */
    public BasicDependency withId(String id) {
        this.id = id;
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
     * @return the BasicDependency object itself.
     */
    public BasicDependency withResourceType(String resourceType) {
        this.resourceType = resourceType;
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
     * @return the BasicDependency object itself.
     */
    public BasicDependency withResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

}
