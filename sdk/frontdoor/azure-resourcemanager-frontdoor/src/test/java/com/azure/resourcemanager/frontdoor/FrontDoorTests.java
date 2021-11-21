// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.frontdoor;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.SubResource;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.frontdoor.fluent.models.FrontendEndpointInner;
import com.azure.resourcemanager.frontdoor.models.Backend;
import com.azure.resourcemanager.frontdoor.models.BackendEnabledState;
import com.azure.resourcemanager.frontdoor.models.BackendPool;
import com.azure.resourcemanager.frontdoor.models.ForwardingConfiguration;
import com.azure.resourcemanager.frontdoor.models.FrontDoor;
import com.azure.resourcemanager.frontdoor.models.FrontDoorForwardingProtocol;
import com.azure.resourcemanager.frontdoor.models.FrontDoorHealthProbeMethod;
import com.azure.resourcemanager.frontdoor.models.FrontDoorProtocol;
import com.azure.resourcemanager.frontdoor.models.HealthProbeEnabled;
import com.azure.resourcemanager.frontdoor.models.HealthProbeSettingsModel;
import com.azure.resourcemanager.frontdoor.models.LoadBalancingSettingsModel;
import com.azure.resourcemanager.frontdoor.models.RoutingRule;
import com.azure.resourcemanager.frontdoor.models.RoutingRuleEnabledState;
import com.azure.resourcemanager.frontdoor.models.SessionAffinityEnabledState;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class FrontDoorTests extends TestBase {

    private static final Region REGION = Region.US_EAST2;

    private String subscriptionId;
    private String resourceGroupName;
    private String fdName;

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void frontDoorTest() {
        StorageManager storageManager = StorageManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        FrontDoorManager manager = FrontDoorManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        resourceGroupName = "rg" + randomPadding();
        String saName = "sa" + randomPadding();
        fdName = "fd" + randomPadding();

        storageManager.resourceManager().resourceGroups().define(resourceGroupName)
            .withRegion(REGION)
            .create();

        try {
            // @embedmeStart
            StorageAccount storageAccount = storageManager.storageAccounts()
                .define(saName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .create();

            String backendAddress = fdName + ".blob.core.windows.net";
            String frontendName = "frontend1";
            String loadBalancingName = "loadbalancing1";
            String healthProbeName = "healthprobe1";
            String routingRuleName = "rule1";
            String backendPoolName = "backend1";
            subscriptionId = ResourceId.fromString(storageAccount.id()).subscriptionId();
            String frontendEndpointsId = getResourceId("frontendEndpoints", frontendName);
            String loadBalancingSettingsId = getResourceId("loadBalancingSettings", loadBalancingName);
            String healthProbeSettingsId = getResourceId("healthProbeSettings", healthProbeName);
            String backendPoolsId = getResourceId("backendPools", backendPoolName);

            FrontDoor frontDoor = manager.frontDoors().define(fdName)
                .withRegion("global")
                .withExistingResourceGroup(resourceGroupName)
                .withFrontendEndpoints(Collections.singletonList(
                    new FrontendEndpointInner()
                        .withName(frontendName)
                        .withHostname(fdName + ".azurefd.net")
                        .withSessionAffinityEnabledState(SessionAffinityEnabledState.DISABLED)
                ))
                .withBackendPools(Collections.singletonList(
                    new BackendPool().withName(backendPoolName).withBackends(Collections.singletonList(
                            new Backend()
                                .withAddress(backendAddress)
                                .withEnabledState(BackendEnabledState.ENABLED)
                                .withBackendHostHeader(backendAddress)
                                .withHttpPort(80)
                                .withHttpsPort(443)
                                .withPriority(1)
                                .withWeight(50)
                        ))
                        .withLoadBalancingSettings(new SubResource().withId(loadBalancingSettingsId))
                        .withHealthProbeSettings(new SubResource().withId(healthProbeSettingsId))
                ))
                .withLoadBalancingSettings(Collections.singletonList(
                    new LoadBalancingSettingsModel()
                        .withName(loadBalancingName)
                        .withSampleSize(4)
                        .withSuccessfulSamplesRequired(2)
                        .withAdditionalLatencyMilliseconds(0)
                ))
                .withHealthProbeSettings(Collections.singletonList(
                    new HealthProbeSettingsModel()
                        .withName(healthProbeName)
                        .withEnabledState(HealthProbeEnabled.ENABLED)
                        .withPath("/")
                        .withProtocol(FrontDoorProtocol.HTTPS)
                        .withHealthProbeMethod(FrontDoorHealthProbeMethod.HEAD)
                        .withIntervalInSeconds(30)
                ))
                .withRoutingRules(Collections.singletonList(
                    new RoutingRule()
                        .withName(routingRuleName)
                        .withEnabledState(RoutingRuleEnabledState.ENABLED)
                        .withFrontendEndpoints(Collections.singletonList(new SubResource().withId(frontendEndpointsId)))
                        .withAcceptedProtocols(Arrays.asList(FrontDoorProtocol.HTTP, FrontDoorProtocol.HTTPS))
                        .withPatternsToMatch(Collections.singletonList("/*"))
                        .withRouteConfiguration(new ForwardingConfiguration()
                            .withForwardingProtocol(FrontDoorForwardingProtocol.HTTPS_ONLY)
                            .withBackendPool(new SubResource().withId(backendPoolsId)))
                ))
                .create();
            // @embedmeEnd
        } finally {
            storageManager.resourceManager().resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    private static final Random RANDOM = new Random();

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

    private String getResourceId(String type, String name) {
        return String.format("/subscriptions/%1$s/resourceGroups/%2$s/providers/Microsoft.Network/frontdoors/%3$s/%4$s/%5$s",
            subscriptionId, resourceGroupName, fdName, type, name);
    }
}
