/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;

import java.util.List;

/**
 * Resource Type.
 */
public class ResourceType {
    /**
     * Gets or sets the resource type name.
     */
    private String name;

    /**
     * Gets or sets the resource type display name.
     */
    private String displayName;

    /**
     * Gets or sets the resource type operations.
     */
    private List<ProviderOperation> operations;

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
     * @return the ResourceType object itself.
     */
    public ResourceType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the ResourceType object itself.
     */
    public ResourceType setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the operations value.
     *
     * @return the operations value
     */
    public List<ProviderOperation> operations() {
        return this.operations;
    }

    /**
     * Set the operations value.
     *
     * @param operations the operations value to set
     * @return the ResourceType object itself.
     */
    public ResourceType setOperations(List<ProviderOperation> operations) {
        this.operations = operations;
        return this;
    }

}
