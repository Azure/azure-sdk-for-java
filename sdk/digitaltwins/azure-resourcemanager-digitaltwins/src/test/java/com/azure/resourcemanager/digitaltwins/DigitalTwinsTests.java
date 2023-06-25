// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.digitaltwins;

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
import com.azure.resourcemanager.digitaltwins.models.DigitalTwinsDescription;
import com.azure.resourcemanager.digitaltwins.models.PublicNetworkAccess;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;

public class DigitalTwinsTests extends TestBase {

    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private AzureDigitalTwinsManager digitalTwinsManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        digitalTwinsManager = AzureDigitalTwinsManager
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
    @DoNotRecord(skipInPlayback = true)
    public void testDigitalTwinsDescription() {
        DigitalTwinsDescription digitalTwinsDescription = null;
        try {
            String digitalTwinsName = "digitalTwin" + randomPadding();
            // embedmeStart
            digitalTwinsDescription = digitalTwinsManager.digitalTwins()
                .define(digitalTwinsName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withPrivateEndpointConnections(Collections.emptyList())
                .create();
            // embedmeEnd
            digitalTwinsDescription.refresh();

            Assertions.assertEquals(digitalTwinsDescription.name(), digitalTwinsName);
            Assertions.assertEquals(digitalTwinsDescription.name(), digitalTwinsManager.digitalTwins().getById(digitalTwinsDescription.id()).name());
            Assertions.assertTrue(digitalTwinsManager.digitalTwins().list().stream().count() > 0);
        } finally {
            if (digitalTwinsDescription != null) {
                digitalTwinsManager.digitalTwins().getById(digitalTwinsDescription.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

}
