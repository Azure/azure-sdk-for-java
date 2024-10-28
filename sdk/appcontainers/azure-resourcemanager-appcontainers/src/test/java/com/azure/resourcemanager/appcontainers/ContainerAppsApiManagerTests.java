// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appcontainers;

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
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironment;
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironmentPropertiesPeerAuthentication;
import com.azure.resourcemanager.appcontainers.models.Mtls;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ContainerAppsApiManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST2;
    private String resourceGroupName = "rg" + randomPadding();
    private ContainerAppsApiManager containerAppsApiManager = null;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile).withDefaultSubscription();

        containerAppsApiManager = ContainerAppsApiManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
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
    public void testCreateManagedEnvironment() {
        ManagedEnvironment managedEnvironment = null;
        try {
            String envName = "env" + randomPadding();
            // @embedmeStart
            managedEnvironment = containerAppsApiManager.managedEnvironments()
                .define(envName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withPeerAuthentication(
                    new ManagedEnvironmentPropertiesPeerAuthentication().withMtls(new Mtls().withEnabled(false)))
                .create();
            // @embedmeEnd
            managedEnvironment.refresh();
            Assertions.assertEquals(envName, managedEnvironment.name());
            Assertions.assertEquals(envName,
                containerAppsApiManager.managedEnvironments().getById(managedEnvironment.id()).name());
            Assertions.assertTrue(containerAppsApiManager.managedEnvironments()
                .listByResourceGroup(resourceGroupName)
                .stream()
                .findAny()
                .isPresent());
        } finally {
            if (managedEnvironment != null) {
                containerAppsApiManager.managedEnvironments().deleteById(managedEnvironment.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
