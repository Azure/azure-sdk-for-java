// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.batch;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.batch.models.AutoStorageBaseProperties;
import com.azure.resourcemanager.batch.models.BatchAccount;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class BatchTests extends TestBase {

    private static final Random RANDOM = new Random();

    private static final Region REGION = Region.US_WEST2;
    private static final String RESOURCE_GROUP = "rg" + randomPadding();
    private static final String STORAGE_ACCOUNT = "sa" + randomPadding();
    private static final String BATCH_ACCOUNT = "ba" + randomPadding();

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testCreateBatchAccount() {

        BatchManager batchManager = BatchManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        StorageManager storageManager = StorageManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        try {
            // resource group
            storageManager.resourceManager().resourceGroups().define(RESOURCE_GROUP)
                .withRegion(REGION)
                .create();

            // storage account
            StorageAccount storageAccount = storageManager.storageAccounts().define(STORAGE_ACCOUNT)
                .withRegion(REGION)
                .withExistingResourceGroup(RESOURCE_GROUP)
                .create();

            // batch account
            BatchAccount account = batchManager
                .batchAccounts()
                .define(BATCH_ACCOUNT)
                .withRegion(REGION)
                .withExistingResourceGroup(RESOURCE_GROUP)
                .withAutoStorage(
                    new AutoStorageBaseProperties()
                        .withStorageAccountId(storageAccount.id()))
                .create();


            assertNotNull(account);

            BatchAccount batchAccount = batchManager.batchAccounts().getByResourceGroup(RESOURCE_GROUP, BATCH_ACCOUNT);
            assertEquals(BATCH_ACCOUNT, batchAccount.name());
            assertEquals(REGION.toString(), batchAccount.location());

        } finally {
            storageManager.resourceManager().resourceGroups().beginDeleteByName(RESOURCE_GROUP);
        }

    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }


}
