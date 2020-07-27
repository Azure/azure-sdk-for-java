/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;

public class StorageAccountManager extends AzureManager<StorageAccount, String> {

    public StorageAccountManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
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
