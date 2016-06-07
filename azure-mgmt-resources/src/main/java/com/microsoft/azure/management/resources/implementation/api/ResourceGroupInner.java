/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resource group information.
 */
public class ResourceGroupInner {
    /**
     * Gets the ID of the resource group.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * Gets or sets the Name of the resource group.
     */
    private String name;

    /**
     * The properties property.
     */
    private ResourceGroupProperties properties;

    /**
     * Gets or sets the location of the resource group. It cannot be changed
     * after the resource group has been created. Has to be one of the
     * supported Azure Locations, such as West US, East US, West Europe, East
     * Asia, etc.
     */
    @JsonProperty(required = true)
    private String location;

    /**
     * Gets or sets the tags attached to the resource group.
     */
    private Map<String, String> tags;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
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
     * @return the ResourceGroupInner object itself.
     */
    public ResourceGroupInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public ResourceGroupProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the ResourceGroupInner object itself.
     */
    public ResourceGroupInner withProperties(ResourceGroupProperties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the location value.
     *
     * @return the location value
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the location value to set
     * @return the ResourceGroupInner object itself.
     */
    public ResourceGroupInner withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the ResourceGroupInner object itself.
     */
    public ResourceGroupInner withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

}
