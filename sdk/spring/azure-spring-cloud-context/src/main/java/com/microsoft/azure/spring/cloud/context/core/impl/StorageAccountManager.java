// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.context.core.impl;

import javax.annotation.Nonnull;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;

public class StorageAccountManager extends AzureManager<StorageAccount, String> {

    private final AzureResourceManager azureResourceManager;

    public StorageAccountManager(@Nonnull AzureResourceManager azureResourceManager, AzureProperties azureProperties) {
        super(azureProperties);
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

        return azureResourceManager.storageAccounts().getByResourceGroup(azureProperties.getResourceGroup(), key);
    }

    @Override
    public StorageAccount internalCreate(String key) {
        return azureResourceManager.storageAccounts().define(key).withRegion(azureProperties.getRegion())
                .withExistingResourceGroup(azureProperties.getResourceGroup()).create();
    }
}
