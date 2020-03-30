package com.azure.management.storage;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StorageBlobServicesTests extends StorageManagementTest {
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
    public void canCreateBlobServices() {
        String SA_NAME = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobServices blobServices = this.storageManager.blobServices();
        BlobServiceProperties blobService = blobServices.define("blobServicesTest")
                .withExistingStorageAccount(storageAccount.resourceGroupName(), storageAccount.name())
                .withDeleteRetentionPolicyEnabled(5)
                .create();

        Assertions.assertTrue(blobService.deleteRetentionPolicy().isEnabled());
        Assertions.assertEquals(5, blobService.deleteRetentionPolicy().getDays().intValue());

    }

    @Test
    public void canUpdateBlobServices() {
        String SA_NAME = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(SA_NAME)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobServices blobServices = this.storageManager.blobServices();
        BlobServiceProperties blobService = blobServices.define("blobServicesTest")
                .withExistingStorageAccount(storageAccount.resourceGroupName(), storageAccount.name())
                .withDeleteRetentionPolicyEnabled(5)
                .create();

        blobService.update()
                .withDeleteRetentionPolicyDisabled()
                .apply();

        Assertions.assertFalse(blobService.deleteRetentionPolicy().isEnabled());

    }
}