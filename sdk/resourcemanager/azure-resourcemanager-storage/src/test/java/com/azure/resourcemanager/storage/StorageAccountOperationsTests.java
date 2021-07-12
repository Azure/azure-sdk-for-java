// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.SkuName;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.resourcemanager.storage.models.StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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
        resourceManager.resourceGroups().beginDeleteByName(rgName);
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
        Mono<StorageAccount> resourceStream =
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
        StorageAccount storageAccount = resourceStream.block();
        Assertions.assertEquals(rgName, storageAccount.resourceGroupName());
        Assertions.assertEquals(SkuName.STANDARD_RAGRS, storageAccount.skuType().name());
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
        storageAccount = storageAccount.update()
            .withSku(StorageAccountSkuType.STANDARD_LRS).withTag("tag2", "value2").apply();
        Assertions.assertEquals(SkuName.STANDARD_LRS, storageAccount.skuType().name());
        Assertions.assertEquals(2, storageAccount.tags().size());
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
    public void storageAccountDefault() {
        String saName2 = generateRandomResourceName("javacsmsa", 15);

        // default
        StorageAccount storageAccountDefault = storageManager.storageAccounts().define(saName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .create();

        Assertions.assertEquals(Kind.STORAGE_V2, storageAccountDefault.kind());
        Assertions.assertEquals(SkuName.STANDARD_RAGRS, storageAccountDefault.skuType().name());
        Assertions.assertTrue(storageAccountDefault.isHttpsTrafficOnly());
        Assertions.assertEquals(MinimumTlsVersion.TLS1_2, storageAccountDefault.minimumTlsVersion());
        Assertions.assertTrue(storageAccountDefault.isBlobPublicAccessAllowed());
        Assertions.assertTrue(storageAccountDefault.isSharedKeyAccessAllowed());

        // update to non-default
        StorageAccount storageAccount = storageAccountDefault.update()
            .withHttpAndHttpsTraffic()
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_1)
            .disableBlobPublicAccess()
            .disableSharedKeyAccess()
            .apply();

        Assertions.assertFalse(storageAccount.isHttpsTrafficOnly());
        Assertions.assertEquals(MinimumTlsVersion.TLS1_1, storageAccount.minimumTlsVersion());
        Assertions.assertFalse(storageAccount.isBlobPublicAccessAllowed());
        Assertions.assertFalse(storageAccount.isSharedKeyAccessAllowed());

        // new storage account configured as non-default
        storageAccount = storageManager.storageAccounts().define(saName2)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withSku(StorageAccountSkuType.STANDARD_LRS)
            .withGeneralPurposeAccountKind()
            .withHttpAndHttpsTraffic()
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_1)
            .disableBlobPublicAccess()
            .disableSharedKeyAccess()
            .create();

        Assertions.assertEquals(Kind.STORAGE, storageAccount.kind());
        Assertions.assertEquals(SkuName.STANDARD_LRS, storageAccount.skuType().name());
        Assertions.assertFalse(storageAccount.isHttpsTrafficOnly());
        Assertions.assertEquals(MinimumTlsVersion.TLS1_1, storageAccount.minimumTlsVersion());
        Assertions.assertFalse(storageAccount.isBlobPublicAccessAllowed());
        Assertions.assertFalse(storageAccount.isSharedKeyAccessAllowed());
    }
}
