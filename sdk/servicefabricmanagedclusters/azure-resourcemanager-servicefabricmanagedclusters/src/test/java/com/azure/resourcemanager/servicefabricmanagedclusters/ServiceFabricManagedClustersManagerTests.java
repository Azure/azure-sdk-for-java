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
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedCluster;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.Sku;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.SkuName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
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
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        serviceFabricManagedClustersManager = ServiceFabricManagedClustersManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        canRegisterProviders(Arrays.asList("Microsoft.ServiceFabric"));

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

    /**
     * Check and register service resources
     *
     * @param providerNamespaces the resource provider names
     */
    private void canRegisterProviders(List<String> providerNamespaces) {
        providerNamespaces.forEach(providerNamespace -> {
            Provider provider = resourceManager.providers().getByName(providerNamespace);
            if (!"Registered".equalsIgnoreCase(provider.registrationState())
                && !"Registering".equalsIgnoreCase(provider.registrationState())) {
                provider = resourceManager.providers().register(providerNamespace);
            }
            while (!"Registered".equalsIgnoreCase(provider.registrationState())) {
                ResourceManagerUtils.sleep(Duration.ofSeconds(5));
                provider = resourceManager.providers().getByName(provider.namespace());
            }
        });
    }
}
