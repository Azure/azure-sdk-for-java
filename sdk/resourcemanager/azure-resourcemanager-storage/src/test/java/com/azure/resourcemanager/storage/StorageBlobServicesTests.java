// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.BlobServiceProperties;
import com.azure.resourcemanager.storage.models.BlobServices;
import com.azure.resourcemanager.storage.models.LastAccessTimeTrackingPolicy;
import com.azure.resourcemanager.storage.models.Name;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

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
                .withContainerDeleteRetentionPolicyEnabled(10)
                .withBlobVersioningEnabled()
                .create();

        Assertions.assertTrue(blobService.deleteRetentionPolicy().enabled());
        Assertions.assertEquals(5, blobService.deleteRetentionPolicy().days().intValue());
        Assertions.assertTrue(blobService.isBlobVersioningEnabled());
        Assertions.assertTrue(blobService.containerDeleteRetentionPolicy().enabled());
        Assertions.assertEquals(10, blobService.containerDeleteRetentionPolicy().days().intValue());
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
                .withContainerDeleteRetentionPolicyEnabled(10)
                .withBlobVersioningEnabled()
                .create();

        Assertions.assertTrue(blobService.isBlobVersioningEnabled());
        Assertions.assertTrue(blobService.containerDeleteRetentionPolicy().enabled());

        blobService.update()
            .withDeleteRetentionPolicyDisabled()
            .withBlobVersioningDisabled()
            .withContainerDeleteRetentionPolicyDisabled()
            .apply();

        Assertions.assertFalse(blobService.deleteRetentionPolicy().enabled());
        Assertions.assertFalse(blobService.isBlobVersioningEnabled());
        Assertions.assertFalse(blobService.containerDeleteRetentionPolicy().enabled());
    }

    @Test
    public void canSpecifyLATTrackingPolicy() {
        String saName = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobServices blobServices = this.storageManager.blobServices();

        // can create with LAT policy enabled
        BlobServiceProperties blobService =
            blobServices
                .define("blobServicesTest")
                .withExistingStorageAccount(storageAccount.resourceGroupName(), storageAccount.name())
                .withLastAccessTimeTrackingPolicyEnabled()
                .create();

        Assertions.assertFalse(ResourceManagerUtils.toPrimitiveBoolean(blobService.isBlobVersioningEnabled()));
        Assertions.assertTrue(blobService.isLastAccessTimeTrackingPolicyEnabled());
        LastAccessTimeTrackingPolicy latTrackingPolicy = blobService.lastAccessTimeTrackingPolicy();
        Assertions.assertEquals(1, latTrackingPolicy.trackingGranularityInDays());
        Assertions.assertEquals(Collections.singletonList("blockBlob"), latTrackingPolicy.blobType());
        Assertions.assertEquals(Name.ACCESS_TIME_TRACKING, latTrackingPolicy.name());

        blobService.refresh();

        // can update with LAT policy disabled
        blobService.update()
            .withLastAccessTimeTrackingPolicyDisabled()
            .apply();
        Assertions.assertFalse(blobService.isLastAccessTimeTrackingPolicyEnabled());

        blobService.refresh();

        // can update with LAT policy enabled
        blobService.update()
            .withLastAccessTimeTrackingPolicy(new LastAccessTimeTrackingPolicy().withEnable(true))
            .apply();

        blobService.refresh();

        Assertions.assertTrue(blobService.isLastAccessTimeTrackingPolicyEnabled());
    }
}
