// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;

/**
 * Resource manager for resource group.
 */
public class ResourceGroupCrud extends AbstractResourceCrud<ResourceGroup, String> {

    /**
     * Creates a new instance of {@link ResourceGroupCrud}.
     *
     * @param azureResourceManager The Azure resource manager.
     * @param azureResourceMetadata The Azure resource metadata.
     */
    public ResourceGroupCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
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
            return resourceManager.resourceGroups().getByName(key);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ResourceGroup internalCreate(String key) {
        return resourceManager.resourceGroups()
                              .define(key)
                              .withRegion(resourceMetadata.getResourceGroup())
                              .create();
    }
}
