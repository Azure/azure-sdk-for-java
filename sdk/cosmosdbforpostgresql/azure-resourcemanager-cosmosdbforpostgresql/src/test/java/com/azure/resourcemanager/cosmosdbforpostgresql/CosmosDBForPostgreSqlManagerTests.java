// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmosdbforpostgresql;

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
import com.azure.resourcemanager.cosmosdbforpostgresql.models.Cluster;
import com.azure.resourcemanager.cosmosdbforpostgresql.models.MaintenanceWindow;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class CosmosDBForPostgreSqlManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private CosmosDBForPostgreSqlManager cosmosDBForPostgreSqlManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        cosmosDBForPostgreSqlManager = CosmosDBForPostgreSqlManager
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
    public void testCreateCluster() {
        Cluster cluster = null;
        String  randomPadding = randomPadding();
        try {
            String clusterName = "cluster" + randomPadding;
            String adminPwd = "Pass@" + randomPadding;
            // @embedmeStart
            cluster = cosmosDBForPostgreSqlManager
                .clusters()
                .define(clusterName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAdministratorLoginPassword(adminPwd)
                .withPostgresqlVersion("15")
                .withCitusVersion("12.1")
                .withMaintenanceWindow(new MaintenanceWindow()
                    .withCustomWindow("Disabled")
                    .withDayOfWeek(0)
                    .withStartHour(0)
                    .withStartMinute(0))
                .withEnableShardsOnCoordinator(true)
                .withEnableHa(false)
                .withCoordinatorServerEdition("GeneralPurpose")
                .withNodeServerEdition("MemoryOptimized")
                .withCoordinatorStorageQuotaInMb(131072)
                .withNodeStorageQuotaInMb(524288)
                .withCoordinatorVCores(2)
                .withNodeVCores(4)
                .withCoordinatorEnablePublicIpAccess(true)
                .withNodeEnablePublicIpAccess(true)
                .withNodeCount(0)
                .create();
            // @embedmeEnd
            cluster.refresh();
            Assertions.assertEquals(cluster.name(), clusterName);
            Assertions.assertEquals(cluster.name(), cosmosDBForPostgreSqlManager.clusters().getById(cluster.id()).name());
            Assertions.assertTrue(cosmosDBForPostgreSqlManager.clusters().list().stream().findAny().isPresent());
        } finally {
            if (cluster != null) {
                cosmosDBForPostgreSqlManager.clusters().deleteById(cluster.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
