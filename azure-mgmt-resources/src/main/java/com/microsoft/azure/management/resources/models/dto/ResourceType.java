/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto;

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
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the operations value.
     *
     * @return the operations value
     */
    public List<ProviderOperation> getOperations() {
        return this.operations;
    }

    /**
     * Set the operations value.
     *
     * @param operations the operations value to set
     */
    public void setOperations(List<ProviderOperation> operations) {
        this.operations = operations;
    }

}
