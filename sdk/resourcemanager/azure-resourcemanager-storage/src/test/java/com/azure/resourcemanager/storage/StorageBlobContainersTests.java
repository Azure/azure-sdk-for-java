// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import java.util.HashMap;
import java.util.Map;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.BlobContainers;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageBlobContainersTests extends StorageManagementTest {
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
    public void canCreateBlobContainer() {
        String saName = generateRandomResourceName("javacmsa", 15);
        Map<String, String> metadataTest = new HashMap<String, String>();
        metadataTest.put("a", "b");
        metadataTest.put("c", "d");

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobContainers blobContainers = this.storageManager.blobContainers();
        BlobContainer blobContainer =
            blobContainers
                .defineContainer("blob-test")
                .withExistingBlobService(rgName, saName)
                .withPublicAccess(PublicAccess.CONTAINER)
                .withMetadata("a", "b")
                .withMetadata("c", "d")
                .create();

        Assertions.assertEquals("blob-test", blobContainer.name());
        Assertions.assertEquals(PublicAccess.CONTAINER, blobContainer.publicAccess());
        Assertions.assertEquals(metadataTest, blobContainer.metadata());
    }

    @Test
    public void canUpdateBlobContainer() {
        String saName = generateRandomResourceName("javacmsa", 15);

        Map<String, String> metadataInitial = new HashMap<String, String>();
        metadataInitial.put("a", "b");

        Map<String, String> metadataTest = new HashMap<String, String>();
        metadataTest.put("c", "d");
        metadataTest.put("e", "f");

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobContainers blobContainers = this.storageManager.blobContainers();
        BlobContainer blobContainer =
            blobContainers
                .defineContainer("blob-test")
                .withExistingBlobService(rgName, saName)
                .withPublicAccess(PublicAccess.CONTAINER)
                .withMetadata(metadataInitial)
                .create();

        blobContainer
            .update()
            .withPublicAccess(PublicAccess.BLOB)
            .withMetadata("c", "d")
            .withMetadata("e", "f")
            .apply();

        Assertions.assertEquals("blob-test", blobContainer.name());
        Assertions.assertEquals(PublicAccess.BLOB, blobContainer.publicAccess());
        Assertions.assertEquals(metadataTest, blobContainer.metadata());
    }
}
