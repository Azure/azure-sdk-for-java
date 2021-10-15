// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.ImmutabilityPolicy;
import com.azure.resourcemanager.storage.models.ImmutabilityPolicyState;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImmutabilityPolicyTests extends StorageManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCRUDImmutabilityPolicy() {
        String saName = generateRandomResourceName("javacsmsa", 15);

        StorageAccount storageAccount = storageManager.storageAccounts().define(saName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .create();

        BlobContainer container = storageManager.blobContainers().defineContainer("container")
            .withExistingStorageAccount(storageAccount)
            .withPublicAccess(PublicAccess.NONE)
            .create();

        // immutability policy
        ImmutabilityPolicy policy = storageManager.blobContainers().defineImmutabilityPolicy()
            .withExistingContainer(storageAccount.resourceGroupName(), storageAccount.name(), container.name())
            .withImmutabilityPeriodSinceCreationInDays(7)
            .create();

        Assertions.assertEquals(7, policy.immutabilityPeriodSinceCreationInDays());

        // refresh
        policy.refresh();
        Assertions.assertEquals(ImmutabilityPolicyState.UNLOCKED, policy.state());

        policy = storageManager.blobContainers().getImmutabilityPolicy(storageAccount.resourceGroupName(), storageAccount.name(), container.name());

        // update
        policy.update()
            .withImmutabilityPeriodSinceCreationInDays(14)
            .apply();

        Assertions.assertEquals(14, policy.immutabilityPeriodSinceCreationInDays());

        // delete
        storageManager.blobContainers().deleteImmutabilityPolicy(storageAccount.resourceGroupName(), storageAccount.name(), container.name(), policy.etag());

        // new immutability policy
        policy = storageManager.blobContainers().defineImmutabilityPolicy()
            .withExistingContainer(storageAccount.resourceGroupName(), storageAccount.name(), container.name())
            .withImmutabilityPeriodSinceCreationInDays(7)
            .create();

        // lock, now the immutability policy cannot be deleted
        policy.lock();

        Assertions.assertEquals(ImmutabilityPolicyState.LOCKED, policy.state());

        // extend
        policy.extend(14);
    }
}
