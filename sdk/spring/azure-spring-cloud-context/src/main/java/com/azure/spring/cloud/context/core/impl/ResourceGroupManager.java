// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.spring.cloud.context.core.config.AzureProperties;

/**
 * Resource manager for resource group.
 */
public class ResourceGroupManager extends AzureManager<ResourceGroup, String> {

    private final AzureResourceManager azureResourceManager;
    
    public ResourceGroupManager(AzureResourceManager azureResourceManager, AzureProperties azureProperties) {
        super(azureProperties);
        this.azureResourceManager = azureResourceManager;
    }

    @Override
    String getResourceName(String key) {
        return key;
    }

    @Override
    String getResourceType() {
        return ResourceGroup.class.getSimpleName();
    }

    @Override
    public ResourceGroup internalGet(String key) {
        return azureResourceManager.resourceGroups().getByName(key);
    }

    @Override
    public ResourceGroup internalCreate(String key) {
        return azureResourceManager.resourceGroups()
                                   .define(key)
                                   .withRegion(region)
                                   .create();
    }
}
