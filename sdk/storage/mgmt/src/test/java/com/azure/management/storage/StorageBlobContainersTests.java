package com.azure.management.storage;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class StorageBlobContainersTests extends StorageManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        rgName = generateRandomResourceName("javacsmrg", 15);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateBlobContainer() {
        String SA_NAME = generateRandomResourceName("javacmsa", 15);
        Map<String, String> metadataTest = new HashMap<String, String>();
        metadataTest.put("a", "b");
        metadataTest.put("c", "d");


        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobContainers blobContainers = this.storageManager.blobContainers();
        BlobContainer blobContainer = blobContainers.defineContainer("blob-test")
                .withExistingBlobService(rgName, SA_NAME)
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
        String SA_NAME = generateRandomResourceName("javacmsa", 15);

        Map<String, String> metadataInitial = new HashMap<String, String>();
        metadataInitial.put("a", "b");

        Map<String, String> metadataTest = new HashMap<String, String>();
        metadataTest.put("c", "d");
        metadataTest.put("e", "f");


        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobContainers blobContainers = this.storageManager.blobContainers();
        BlobContainer blobContainer = blobContainers.defineContainer("blob-test")
                .withExistingBlobService(rgName, SA_NAME)
                .withPublicAccess(PublicAccess.CONTAINER)
                .withMetadata(metadataInitial)
                .create();

        blobContainer.update()
                .withPublicAccess(PublicAccess.BLOB)
                .withMetadata("c", "d")
                .withMetadata("e", "f")
                .apply();

        Assertions.assertEquals("blob-test", blobContainer.name());
        Assertions.assertEquals(PublicAccess.BLOB, blobContainer.publicAccess());
        Assertions.assertEquals(metadataTest, blobContainer.metadata());
    }
}