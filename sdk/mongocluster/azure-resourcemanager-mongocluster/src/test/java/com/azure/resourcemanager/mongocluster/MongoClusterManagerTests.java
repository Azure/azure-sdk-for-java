// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.mongocluster;

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
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.mongocluster.models.AdministratorProperties;
import com.azure.resourcemanager.mongocluster.models.ComputeProperties;
import com.azure.resourcemanager.mongocluster.models.HighAvailabilityMode;
import com.azure.resourcemanager.mongocluster.models.HighAvailabilityProperties;
import com.azure.resourcemanager.mongocluster.models.MongoCluster;
import com.azure.resourcemanager.mongocluster.models.MongoClusterProperties;
import com.azure.resourcemanager.mongocluster.models.PublicNetworkAccess;
import com.azure.resourcemanager.mongocluster.models.ShardingProperties;
import com.azure.resourcemanager.mongocluster.models.StorageProperties;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

public class MongoClusterManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private MongoClusterManager mongoClusterManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        mongoClusterManager = MongoClusterManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
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
    public void testCreateMongoCluster() {
        MongoCluster mongoCluster = null;
        String clusterName = "cluster" + randomPadding();
        try {
            String loginUser = "loginUser" + randomPadding();
            String loginPwd = UUID.randomUUID().toString().replace("-", "@").substring(0, 13);
            // @embedmeStart
            mongoCluster = mongoClusterManager.mongoClusters()
                .define(clusterName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withProperties(new MongoClusterProperties()
                    .withAdministrator(new AdministratorProperties().withUserName(loginUser).withPassword(loginPwd))
                    .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                    .withStorage(new StorageProperties().withSizeGb(128L))
                    .withCompute(new ComputeProperties().withTier("M30"))
                    .withHighAvailability(
                        new HighAvailabilityProperties().withTargetMode(HighAvailabilityMode.DISABLED))
                    .withSharding(new ShardingProperties().withShardCount(1))
                    .withServerVersion("7.0"))
                .create();
            // @embedmeEnd
            mongoCluster.refresh();
            Assertions.assertEquals(clusterName, mongoCluster.name());
            Assertions.assertEquals(mongoCluster.name(),
                mongoClusterManager.mongoClusters().getById(mongoCluster.id()).name());
            Assertions.assertTrue(
                mongoClusterManager.mongoClusters().listByResourceGroup(resourceGroupName).stream().count() > 0);
        } finally {
            if (mongoCluster != null) {
                mongoClusterManager.mongoClusters().deleteById(mongoCluster.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
