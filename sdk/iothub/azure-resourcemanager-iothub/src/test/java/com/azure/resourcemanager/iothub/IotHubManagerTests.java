// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.iothub;

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
import com.azure.resourcemanager.iothub.models.ArmIdentity;
import com.azure.resourcemanager.iothub.models.Capabilities;
import com.azure.resourcemanager.iothub.models.CloudToDeviceProperties;
import com.azure.resourcemanager.iothub.models.EventHubProperties;
import com.azure.resourcemanager.iothub.models.FallbackRouteProperties;
import com.azure.resourcemanager.iothub.models.FeedbackProperties;
import com.azure.resourcemanager.iothub.models.IotHubDescription;
import com.azure.resourcemanager.iothub.models.IotHubProperties;
import com.azure.resourcemanager.iothub.models.IotHubSku;
import com.azure.resourcemanager.iothub.models.IotHubSkuInfo;
import com.azure.resourcemanager.iothub.models.MessagingEndpointProperties;
import com.azure.resourcemanager.iothub.models.RoutingProperties;
import com.azure.resourcemanager.iothub.models.RoutingSource;
import com.azure.resourcemanager.iothub.models.ResourceIdentityType;
import com.azure.resourcemanager.iothub.models.StorageEndpointProperties;
import com.azure.resourcemanager.resources.ResourceManager;
import io.netty.util.internal.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class IotHubManagerTests extends TestBase {

    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private IotHubManager iotHubManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        iotHubManager = IotHubManager
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
    public void testIotHubDescription() {
        IotHubDescription iotHubDescription = null;
        try {
            String iothubName = "iotHub" + randomPadding();

            // @embedmeStart
            Map<String, EventHubProperties> eventHubEndpointsMap = new HashMap<>();
            eventHubEndpointsMap.put("events", new EventHubProperties()
                .withRetentionTimeInDays(1L).withPartitionCount(2));

            Map<String, StorageEndpointProperties> storageEndpointsMap = new HashMap<>();
            storageEndpointsMap.put("$default", new StorageEndpointProperties()
                .withSasTtlAsIso8601(Duration.ofHours(1L))
                .withConnectionString(StringUtil.EMPTY_STRING)
                .withContainerName(StringUtil.EMPTY_STRING));

            Map<String, MessagingEndpointProperties> messagingEndpointsMap = new HashMap<>();
            messagingEndpointsMap.put("fileNotifications", new MessagingEndpointProperties()
                .withLockDurationAsIso8601(Duration.ofMinutes(1L))
                .withTtlAsIso8601(Duration.ofHours(1L))
                .withMaxDeliveryCount(10));

            iotHubDescription = iotHubManager.iotHubResources()
                .define(iothubName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new IotHubSkuInfo().withName(IotHubSku.F1).withCapacity(1L))
                .withIdentity(new ArmIdentity().withType(ResourceIdentityType.NONE))
                .withProperties(
                    new IotHubProperties()
                        .withEventHubEndpoints(eventHubEndpointsMap)
                        .withRouting(new RoutingProperties()
                            .withFallbackRoute(
                                new FallbackRouteProperties()
                                    .withName("$fallback")
                                    .withSource(RoutingSource.DEVICE_MESSAGES)
                                    .withCondition("true")
                                    .withIsEnabled(true)
                                    .withEndpointNames(Arrays.asList("events"))))
                        .withStorageEndpoints(storageEndpointsMap)
                        .withMessagingEndpoints(messagingEndpointsMap)
                        .withEnableFileUploadNotifications(false)
                        .withCloudToDevice(new CloudToDeviceProperties()
                            .withMaxDeliveryCount(10)
                            .withDefaultTtlAsIso8601(Duration.ofHours(1L))
                            .withFeedback(new FeedbackProperties()
                                .withLockDurationAsIso8601(Duration.ofMinutes(1L))
                                .withTtlAsIso8601(Duration.ofHours(1L))
                                .withMaxDeliveryCount(10)))
                        .withFeatures(Capabilities.NONE)
                        .withDisableLocalAuth(false)
                        .withEnableDataResidency(false)
                )
                .create();
            // @embedmeEnd
            iotHubDescription.refresh();

            Assertions.assertEquals(iotHubDescription.name(), iothubName);
            Assertions.assertEquals(iotHubDescription.name(), iotHubManager.iotHubResources().getById(iotHubDescription.id()).name());
            Assertions.assertTrue(iotHubManager.iotHubResources().list().stream().count() > 0);
        } finally {
            if (iotHubDescription != null) {
                iotHubManager.iotHubResources().deleteById(iotHubDescription.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
