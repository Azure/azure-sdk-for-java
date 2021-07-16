// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import org.springframework.lang.NonNull;

/**
 * Resource manager for Storage account.
 */
public class StorageAccountManager extends AzureManager<StorageAccount, String> {

    private final AzureResourceManager azureResourceManager;

    public StorageAccountManager(@NonNull AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceMetadata);
        this.azureResourceManager = azureResourceManager;
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
            return azureResourceManager.storageAccounts().getByResourceGroup(resourceGroup, key);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public StorageAccount internalCreate(String key) {
        return azureResourceManager.storageAccounts()
                                   .define(key)
                                   .withRegion(region)
                                   .withExistingResourceGroup(resourceGroup)
                                   .create();
    }
}
