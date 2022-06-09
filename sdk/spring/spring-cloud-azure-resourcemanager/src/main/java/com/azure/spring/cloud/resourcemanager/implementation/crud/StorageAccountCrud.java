// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;

/**
 * Resource manager for Storage account.
 */
public class StorageAccountCrud extends AbstractResourceCrud<StorageAccount, String> {

    /**
     * Creates a new instance of {@link StorageAccountCrud}.
     *
     * @param azureResourceManager The Azure resource manager.
     * @param azureResourceMetadata The Azure resource metadata.
     */
    public StorageAccountCrud(AzureResourceManager azureResourceManager,
                              AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(String key) {
        return key;
    }

    @Override
    String getResourceType() {
        return StorageAccount.class.getSimpleName();
    }

    @Override
    public StorageAccount internalGet(String key) {
        try {
            return this.resourceManager.storageAccounts().getByResourceGroup(this.resourceMetadata.getResourceGroup(),
                                                                             key);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public StorageAccount internalCreate(String key) {
        return this.resourceManager.storageAccounts()
                                   .define(key)
                                   .withRegion(this.resourceMetadata.getRegion())
                                   .withExistingResourceGroup(this.resourceMetadata.getResourceGroup())
                                   .create();
    }
}
