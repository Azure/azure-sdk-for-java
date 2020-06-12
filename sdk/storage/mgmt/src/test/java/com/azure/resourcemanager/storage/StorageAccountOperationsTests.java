// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.List;
import java.util.Map;

import com.azure.resourcemanager.storage.models.SkuName;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionKeySource;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.resourcemanager.storage.models.StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class StorageAccountOperationsTests extends StorageManagementTest {
    private String rgName = "";
    private String saName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        saName = generateRandomResourceName("javacsmsa", 15);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCRUDStorageAccount() throws Exception {
        // Name available
        // Skipping checking name availability for now because of 503 error 'The service is not yet ready to process any
        // requests. Please retry in a few moments.'
        //        CheckNameAvailabilityResult result = storageManager.storageAccounts()
        //                .checkNameAvailability(SA_NAME);
        //        Assertions.assertEquals(true, result.isAvailable());
        // Create
        Flux<Indexable> resourceStream =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.ASIA_EAST)
                .withNewResourceGroup(rgName)
                .withGeneralPurposeAccountKindV2()
                .withTag("tag1", "value1")
                .withHnsEnabled(true)
                .withAzureFilesAadIntegrationEnabled(false)
                .createAsync();
        StorageAccount storageAccount = Utils.<StorageAccount>rootResource(resourceStream.last()).block();
        Assertions.assertEquals(rgName, storageAccount.resourceGroupName());
        Assertions.assertEquals(SkuName.STANDARD_GRS, storageAccount.skuType().name());
        Assertions.assertTrue(storageAccount.isHnsEnabled());
        // Assertions.assertFalse(storageAccount.isAzureFilesAadIntegrationEnabled());
        // List
        PagedIterable<StorageAccount> accounts = storageManager.storageAccounts().listByResourceGroup(rgName);
        boolean found = false;
        for (StorageAccount account : accounts) {
            if (account.name().equals(saName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
        Assertions.assertEquals(1, storageAccount.tags().size());

        // Get
        storageAccount = storageManager.storageAccounts().getByResourceGroup(rgName, saName);
        Assertions.assertNotNull(storageAccount);

        // Get Keys
        List<StorageAccountKey> keys = storageAccount.getKeys();
        Assertions.assertTrue(keys.size() > 0);

        // Regen key
        StorageAccountKey oldKey = keys.get(0);
        List<StorageAccountKey> updatedKeys = storageAccount.regenerateKey(oldKey.keyName());
        Assertions.assertTrue(updatedKeys.size() > 0);
        for (StorageAccountKey updatedKey : updatedKeys) {
            if (updatedKey.keyName().equalsIgnoreCase(oldKey.keyName())) {
                if (!isPlaybackMode()) {
                    Assertions.assertNotEquals(oldKey.value(), updatedKey.value());
                }
                break;
            }
        }

        Map<StorageService, StorageAccountEncryptionStatus> statuses = storageAccount.encryptionStatuses();
        Assertions.assertNotNull(statuses);
        Assertions.assertTrue(statuses.size() > 0);

        Assertions.assertTrue(statuses.containsKey(StorageService.BLOB));
        StorageAccountEncryptionStatus blobServiceEncryptionStatus = statuses.get(StorageService.BLOB);
        Assertions.assertNotNull(blobServiceEncryptionStatus);
        Assertions.assertTrue(blobServiceEncryptionStatus.isEnabled()); // Service will enable this by default

        Assertions.assertTrue(statuses.containsKey(StorageService.FILE));
        StorageAccountEncryptionStatus fileServiceEncryptionStatus = statuses.get(StorageService.FILE);
        Assertions.assertNotNull(fileServiceEncryptionStatus);
        Assertions.assertTrue(fileServiceEncryptionStatus.isEnabled()); // Service will enable this by default

        // Update
        storageAccount = storageAccount.update().withSku(SkuName.STANDARD_LRS).withTag("tag2", "value2").apply();
        Assertions.assertEquals(SkuName.STANDARD_LRS, storageAccount.skuType().name());
        Assertions.assertEquals(2, storageAccount.tags().size());
    }

    @Test
    public void canEnableDisableEncryptionOnStorageAccount() throws Exception {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withBlobEncryption()
                .create();

        Map<StorageService, StorageAccountEncryptionStatus> statuses = storageAccount.encryptionStatuses();
        Assertions.assertNotNull(statuses);
        Assertions.assertTrue(statuses.size() > 0);
        StorageAccountEncryptionStatus blobServiceEncryptionStatus = statuses.get(StorageService.BLOB);
        Assertions.assertNotNull(blobServiceEncryptionStatus);
        Assertions.assertTrue(blobServiceEncryptionStatus.isEnabled());
        Assertions.assertNotNull(blobServiceEncryptionStatus.lastEnabledTime());
        Assertions.assertNotNull(storageAccount.encryptionKeySource());
        Assertions
            .assertTrue(
                storageAccount.encryptionKeySource().equals(StorageAccountEncryptionKeySource.MICROSOFT_STORAGE));

        //        Storage no-longer support disabling encryption
        //
        //        storageAccount = storageAccount.update()
        //                .withoutBlobEncryption()
        //                .apply();
        //        statuses = storageAccount.encryptionStatuses();
        //        Assertions.assertNotNull(statuses);
        //        Assertions.assertTrue(statuses.size() > 0);
        //        blobServiceEncryptionStatus = statuses.get(StorageService.BLOB);
        //        Assertions.assertNotNull(blobServiceEncryptionStatus);
        //        Assertions.assertFalse(blobServiceEncryptionStatus.isEnabled());
    }

    @Test
    public void canEnableLargeFileSharesOnStorageAccount() throws Exception {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .withLargeFileShares(true)
                .create();

        Assertions.assertTrue(storageAccount.isLargeFileSharesEnabled());
    }

    @Test
    public void canEnableDisableFileEncryptionOnStorageAccount() throws Exception {
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withFileEncryption()
                .create();

        Map<StorageService, StorageAccountEncryptionStatus> statuses = storageAccount.encryptionStatuses();
        Assertions.assertNotNull(statuses);
        Assertions.assertTrue(statuses.size() > 0);

        Assertions.assertTrue(statuses.containsKey(StorageService.BLOB));
        StorageAccountEncryptionStatus blobServiceEncryptionStatus = statuses.get(StorageService.BLOB);
        Assertions.assertNotNull(blobServiceEncryptionStatus);
        Assertions.assertTrue(blobServiceEncryptionStatus.isEnabled()); // Enabled by default by default

        Assertions.assertTrue(statuses.containsKey(StorageService.FILE));
        StorageAccountEncryptionStatus fileServiceEncryptionStatus = statuses.get(StorageService.FILE);
        Assertions.assertNotNull(fileServiceEncryptionStatus);
        Assertions.assertTrue(fileServiceEncryptionStatus.isEnabled());

        // Service no longer support disabling file encryption
        //        storageAccount.update()
        //                .withoutFileEncryption()
        //                .apply();
        //
        //        statuses = storageAccount.encryptionStatuses();
        //
        //        Assertions.assertTrue(statuses.containsKey(StorageService.FILE));
        //        fileServiceEncryptionStatus = statuses.get(StorageService.FILE);
        //        Assertions.assertNotNull(fileServiceEncryptionStatus);
        //        Assertions.assertFalse(fileServiceEncryptionStatus.isEnabled());

    }
}
