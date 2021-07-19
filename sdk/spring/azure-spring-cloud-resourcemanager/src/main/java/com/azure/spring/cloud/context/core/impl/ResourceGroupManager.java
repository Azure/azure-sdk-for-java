// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;

/**
 * Resource manager for resource group.
 */
public class ResourceGroupManager extends AzureManager<ResourceGroup, String> {

    private final AzureResourceManager azureResourceManager;
    
    public ResourceGroupManager(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceMetadata);
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
        try {
            return azureResourceManager.resourceGroups().getByName(key);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ResourceGroup internalCreate(String key) {
        return azureResourceManager.resourceGroups()
                                   .define(key)
                                   .withRegion(region)
                                   .create();
    }
}
