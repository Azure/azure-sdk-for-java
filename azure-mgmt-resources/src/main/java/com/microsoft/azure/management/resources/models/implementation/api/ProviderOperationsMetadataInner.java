/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;

import java.util.List;

/**
 * Provider Operations metadata.
 */
public class ProviderOperationsMetadataInner {
    /**
     * Gets or sets the provider id.
     */
    private String id;

    /**
     * Gets or sets the provider name.
     */
    private String name;

    /**
     * Gets or sets the provider type.
     */
    private String type;

    /**
     * Gets or sets the provider display name.
     */
    private String displayName;

    /**
     * Gets or sets the provider resource types.
     */
    private List<ResourceType> resourceTypes;

    /**
     * Gets or sets the provider operations.
     */
    private List<ProviderOperation> operations;

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
     * @return the ProviderOperationsMetadataInner object itself.
     */
    public ProviderOperationsMetadataInner setId(String id) {
        this.id = id;
        return this;
    }

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
     * @return the ProviderOperationsMetadataInner object itself.
     */
    public ProviderOperationsMetadataInner setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the ProviderOperationsMetadataInner object itself.
     */
    public ProviderOperationsMetadataInner setType(String type) {
        this.type = type;
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
     * @return the ProviderOperationsMetadataInner object itself.
     */
    public ProviderOperationsMetadataInner setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the resourceTypes value.
     *
     * @return the resourceTypes value
     */
    public List<ResourceType> resourceTypes() {
        return this.resourceTypes;
    }

    /**
     * Set the resourceTypes value.
     *
     * @param resourceTypes the resourceTypes value to set
     * @return the ProviderOperationsMetadataInner object itself.
     */
    public ProviderOperationsMetadataInner setResourceTypes(List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
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
     * @return the ProviderOperationsMetadataInner object itself.
     */
    public ProviderOperationsMetadataInner setOperations(List<ProviderOperation> operations) {
        this.operations = operations;
        return this;
    }

}
