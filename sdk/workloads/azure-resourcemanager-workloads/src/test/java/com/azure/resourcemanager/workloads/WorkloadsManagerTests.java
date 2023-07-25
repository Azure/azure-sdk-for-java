// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.workloads;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.loganalytics.LogAnalyticsManager;
import com.azure.resourcemanager.loganalytics.models.PublicNetworkAccessType;
import com.azure.resourcemanager.loganalytics.models.WorkspaceCapping;
import com.azure.resourcemanager.loganalytics.models.WorkspaceFeatures;
import com.azure.resourcemanager.loganalytics.models.WorkspaceSku;
import com.azure.resourcemanager.loganalytics.models.WorkspaceSkuNameEnum;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.workloads.models.ManagedRGConfiguration;
import com.azure.resourcemanager.workloads.models.Monitor;
import com.azure.resourcemanager.workloads.models.RoutingPreference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class WorkloadsManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region RESOURCE_REGION = Region.US_EAST2;
    private static final Region APPLICATION_REGION = Region.US_WEST;
    private static final String MANAGED_RG_NAME = "managerRG" + randomPadding();
    private String resourceGroupName = "rg" + randomPadding();
    private LogAnalyticsManager logAnalyticsManager;
    private NetworkManager networkManager;
    private WorkloadsManager workloadsManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        logAnalyticsManager = LogAnalyticsManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        networkManager = NetworkManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        workloadsManager = WorkloadsManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
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
                .withRegion(RESOURCE_REGION)
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
    @DoNotRecord(skipInPlayback = true)
    public void testCreateMonitor() {
        Monitor monitor = null;
        String randomPadding = randomPadding();
        try {
            String monitorName = "monitor" + randomPadding;
            String vNetworkName = "network" + randomPadding;
            String workspaceName = "workspace" + randomPadding;
            // @embedmeStart
            monitor = workloadsManager.monitors()
                .define(monitorName)
                .withRegion(RESOURCE_REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAppLocation(APPLICATION_REGION.name())
                .withRoutingPreference(RoutingPreference.ROUTE_ALL)
                .withManagedResourceGroupConfiguration(
                    new ManagedRGConfiguration().withName(MANAGED_RG_NAME))
                .withLogAnalyticsWorkspaceArmId(
                    logAnalyticsManager.workspaces()
                        .define(workspaceName)
                        .withRegion(RESOURCE_REGION)
                        .withExistingResourceGroup(resourceGroupName)
                        .withSku(new WorkspaceSku()
                            .withName(WorkspaceSkuNameEnum.PER_GB2018))
                        .withFeatures(new WorkspaceFeatures()
                            .withEnableLogAccessUsingOnlyResourcePermissions(true))
                        .withRetentionInDays(30)
                        .withWorkspaceCapping(new WorkspaceCapping().withDailyQuotaGb(-1D))
                        .withPublicNetworkAccessForIngestion(PublicNetworkAccessType.ENABLED)
                        .withPublicNetworkAccessForQuery(PublicNetworkAccessType.ENABLED)
                        .create()
                        .id())
                .withMonitorSubnet(
                    networkManager.networks()
                        .define(vNetworkName)
                        .withRegion(RESOURCE_REGION)
                        .withExistingResourceGroup(resourceGroupName)
                        .withAddressSpace("172.29.0.0/16")
                        .withSubnet("default", "172.29.0.0/24")
                        .create()
                        .id())
                .create();
            // @embedmeEnd
            monitor.refresh();
            Assertions.assertEquals(monitor.name(), monitorName);
            Assertions.assertEquals(monitor.name(), workloadsManager.monitors().getById(monitor.id()).name());
            Assertions.assertTrue(workloadsManager.monitors().list().stream().count() > 0);
        } finally {
            if (monitor != null) {
                workloadsManager.monitors().deleteById(monitor.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
