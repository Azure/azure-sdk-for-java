// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.context.core.impl;

import javax.annotation.Nonnull;

import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;

public class StorageAccountManager extends AzureManager<StorageAccount, String> {
    
    private final Azure azure;

    public StorageAccountManager(@Nonnull Azure azure, AzureProperties azureProperties) {
        super(azureProperties);
        this.azure = azure;
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
        
        return azure.storageAccounts().getByResourceGroup(azureProperties.getResourceGroup(), key);
    }

    @Override
    public StorageAccount internalCreate(String key) {
        return azure.storageAccounts().define(key).withRegion(azureProperties.getRegion())
            .withExistingResourceGroup(azureProperties.getResourceGroup()).create();
    }
}
