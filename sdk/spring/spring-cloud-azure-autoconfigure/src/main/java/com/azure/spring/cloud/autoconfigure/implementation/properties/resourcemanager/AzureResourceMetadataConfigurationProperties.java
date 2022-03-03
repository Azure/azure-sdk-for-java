// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.resourcemanager;

import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;

/**
 * Metadata defining an Azure resource.
 */
public class AzureResourceMetadataConfigurationProperties implements AzureResourceMetadata {

    /**
     * The resource group holds an Azure resource.
     */
    private String resourceGroup;
    /**
     * ID of an Azure resource.
     */
    private String resourceId;
    /**
     * The region of an Azure resource.
     */
    private String region;

    @Override
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

    @Override
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

    @Override
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
