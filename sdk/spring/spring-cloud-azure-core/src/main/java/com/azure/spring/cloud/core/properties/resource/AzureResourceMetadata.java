// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.resource;

/**
 * Metadata defining an Azure resource.
 */
public interface AzureResourceMetadata {

    /**
     * Get the resource group of this resource.
     * @return The resource group.
     */
    String getResourceGroup();

    /**
     * Get the resource id of this resource.
     * @return The resource id.
     */
    String getResourceId();

    /**
     * Get the region of this resource.
     * @return The region.
     */
    String getRegion();
}
