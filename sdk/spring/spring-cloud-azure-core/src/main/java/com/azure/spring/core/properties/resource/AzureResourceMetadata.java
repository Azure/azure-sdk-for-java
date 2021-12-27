// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.resource;

/**
 * Metadata defining an Azure resource.
 */
public class AzureResourceMetadata {

    private String resourceGroup;
    private String resourceId;
    private String region;

    /**
     * Get the resource group of this resource.
     * @return The resource group.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    /**
     * Set the resource group of this resource.
     * @param resourceGroup The resource group.
     */
    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Get the resource id of this resource.
     * @return The resource id.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Set the resource id of this resource.
     * @param resourceId The resource id.
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Get the region of this resource.
     * @return The region.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Set the region of this resource.
     * @param region The region.
     */
    public void setRegion(String region) {
        this.region = region;
    }

}
