/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.SubResource;

/**
 * Virtual machine image resource information.
 */
public class VirtualMachineImageResourceInner extends SubResource {
    /**
     * Gets or sets the name of the resource.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the location of the resource.
     */
    @JsonProperty(required = true)
    private String location;

    /**
     * Gets or sets the tags attached to the resource.
     */
    private Map<String, String> tags;

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
     * @return the VirtualMachineImageResourceInner object itself.
     */
    public VirtualMachineImageResourceInner withName(String name) {
        this.name = name;
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
     * @return the VirtualMachineImageResourceInner object itself.
     */
    public VirtualMachineImageResourceInner withLocation(String location) {
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
     * @return the VirtualMachineImageResourceInner object itself.
     */
    public VirtualMachineImageResourceInner withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

}
