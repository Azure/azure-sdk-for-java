// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.BlobServiceProperties;
import com.azure.resourcemanager.storage.models.BlobServices;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageBlobServicesTests extends StorageManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateBlobServices() {
        String saName = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobServices blobServices = this.storageManager.blobServices();
        BlobServiceProperties blobService =
            blobServices
                .define("blobServicesTest")
                .withExistingStorageAccount(storageAccount.resourceGroupName(), storageAccount.name())
                .withDeleteRetentionPolicyEnabled(5)
                .create();

        Assertions.assertTrue(blobService.deleteRetentionPolicy().enabled());
        Assertions.assertEquals(5, blobService.deleteRetentionPolicy().days().intValue());
    }

    @Test
    public void canUpdateBlobServices() {
        String saName = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobServices blobServices = this.storageManager.blobServices();
        BlobServiceProperties blobService =
            blobServices
                .define("blobServicesTest")
                .withExistingStorageAccount(storageAccount.resourceGroupName(), storageAccount.name())
                .withDeleteRetentionPolicyEnabled(5)
                .create();

        blobService.update().withDeleteRetentionPolicyDisabled().apply();

        Assertions.assertFalse(blobService.deleteRetentionPolicy().enabled());
    }
}
