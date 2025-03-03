// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.databasewatcher;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.databasewatcher.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.databasewatcher.models.ManagedServiceIdentityV4;
import com.azure.resourcemanager.databasewatcher.models.Watcher;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class DatabaseWatcherManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST2;
    private static final String RANDOM_PADDING = randomPadding();
    private static String resourceGroupName = "rg" + RANDOM_PADDING;
    private DatabaseWatcherManager databaseWatcherManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        databaseWatcherManager = DatabaseWatcherManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .authenticate(credential, profile);

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroupName).withRegion(REGION).create();
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
    public void testCreateDatabaseWatch() {
        Watcher watcher = null;
        try {
            String watchName = "dw" + RANDOM_PADDING;
            // @embedmeStart
            watcher = databaseWatcherManager.watchers()
                .define(watchName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withIdentity(new ManagedServiceIdentityV4().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
                .create();
            // @embedmeEnd
            Assertions.assertEquals(watcher.name(), watchName);
            Assertions.assertEquals(watcher.name(), databaseWatcherManager.watchers().getById(watcher.id()).name());
            Assertions.assertTrue(databaseWatcherManager.watchers().list().stream().findAny().isPresent());
        } finally {
            if (watcher != null) {
                databaseWatcherManager.watchers().deleteById(watcher.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
