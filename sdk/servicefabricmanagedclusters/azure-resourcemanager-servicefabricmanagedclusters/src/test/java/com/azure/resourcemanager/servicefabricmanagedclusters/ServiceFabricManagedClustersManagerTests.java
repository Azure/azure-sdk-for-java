// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicefabricmanagedclusters;

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
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedCluster;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.Sku;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.SkuName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

public class ServiceFabricManagedClustersManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private ServiceFabricManagedClustersManager serviceFabricManagedClustersManager = null;
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

        serviceFabricManagedClustersManager = ServiceFabricManagedClustersManager.configure()
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
    public void testCreateManagedCluster() {
        ManagedCluster managedCluster = null;
        try {
            String clusterName = "cluster" + randomPadding();
            String adminUser = "user" + randomPadding();
            String adminPassWord = UUID.randomUUID().toString().replace("-", "@").substring(0, 13);
            // @embedmeStart
            managedCluster = serviceFabricManagedClustersManager.managedClusters()
                .define(clusterName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new Sku().withName(SkuName.STANDARD))
                .withAdminUsername(adminUser)
                .withAdminPassword(adminPassWord)
                .withDnsName(clusterName)
                .withClientConnectionPort(19000)
                .withHttpGatewayConnectionPort(19080)
                .create();
            // @embedmeEnd
            managedCluster.refresh();
            Assertions.assertEquals(clusterName, managedCluster.name());
            Assertions.assertEquals(clusterName,
                serviceFabricManagedClustersManager.managedClusters().getById(managedCluster.id()).name());
            Assertions.assertTrue(serviceFabricManagedClustersManager.managedClusters()
                .listByResourceGroup(resourceGroupName)
                .stream()
                .findAny()
                .isPresent());
        } finally {
            if (managedCluster != null) {
                serviceFabricManagedClustersManager.managedClusters().deleteById(managedCluster.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
