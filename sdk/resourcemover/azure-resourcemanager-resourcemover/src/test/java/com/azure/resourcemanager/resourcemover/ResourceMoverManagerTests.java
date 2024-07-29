// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resourcemover;

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
import com.azure.resourcemanager.resourcemover.models.MoveCollection;
import com.azure.resourcemanager.resourcemover.models.MoveCollectionProperties;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ResourceMoverManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST2;
    private String resourceGroupName = "rg" + randomPadding();
    private ResourceMoverManager resourceMoverManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceMoverManager = ResourceMoverManager
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
    public void testCreateMoveCollection() {
        MoveCollection moveCollection = null;
        try {
            String collectionName = "collection" + randomPadding();
            // @embedmeStart
            moveCollection = resourceMoverManager.moveCollections()
                .define(collectionName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withProperties(new MoveCollectionProperties()
                    .withSourceRegion(Region.US_WEST2.name())
                    .withTargetRegion(Region.US_WEST.name()))
                .create();
            // @embedmeEnd
            moveCollection.refresh();
            Assertions.assertEquals(moveCollection.name(), collectionName);
            Assertions.assertEquals(moveCollection.name(), resourceMoverManager.moveCollections().getById(moveCollection.id()).name());
            Assertions.assertTrue(resourceMoverManager.moveCollections().list().stream().count() > 0);
        } finally {
            if (moveCollection != null) {
                resourceMoverManager.moveCollections().deleteById(moveCollection.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
