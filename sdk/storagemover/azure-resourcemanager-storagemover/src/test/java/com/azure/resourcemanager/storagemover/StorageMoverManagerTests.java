// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class StorageMoverManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.create("eastus2euap", "East US 2 EUAP");
    private String resourceGroupName = "rg" + randomPadding();
    private StorageMoverManager storageMoverManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        storageMoverManager = StorageMoverManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCreateStorageMover() {
        StorageMover storageMover = null;
        try {
            String moveName = "move" + randomPadding();
            // @embedmeStart
            storageMover = storageMoverManager.storageMovers()
                .define(moveName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .create();
            // @embedmeEnd
            storageMover.refresh();
            Assertions.assertEquals(storageMover.name(), moveName);
            Assertions.assertEquals(storageMover.name(), storageMoverManager.storageMovers().getById(storageMover.id()).name());
            Assertions.assertTrue(storageMoverManager.storageMovers().list().stream().count() > 0);
        } finally {
            if (storageMover != null) {
                storageMoverManager.storageMovers().deleteById(storageMover.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
