// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.storage.generated;

import com.azure.resourcemanager.storage.fluent.models.StorageAccountMigrationInner;
import com.azure.resourcemanager.storage.models.SkuName;

/**
 * Samples for StorageAccounts CustomerInitiatedMigration.
 */
public final class StorageAccountsCustomerInitiatedMigrationSamples {
    /*
     * x-ms-original-file:
     * specification/storage/resource-manager/Microsoft.Storage/stable/2024-01-01/examples/StorageAccountPostMigration.
     * json
     */
    /**
     * Sample code: StorageAccountPostMigration.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void storageAccountPostMigration(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.storageAccounts()
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .customerInitiatedMigration("resource-group-name", "accountname",
                new StorageAccountMigrationInner().withTargetSkuName(SkuName.STANDARD_ZRS),
                com.azure.core.util.Context.NONE);
    }
}
