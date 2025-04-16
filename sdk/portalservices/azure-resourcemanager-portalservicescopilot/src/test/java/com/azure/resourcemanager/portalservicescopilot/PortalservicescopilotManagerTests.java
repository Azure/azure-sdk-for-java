// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.portalservicescopilot;

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
import com.azure.resourcemanager.portalservicescopilot.fluent.models.CopilotSettingsResourceInner;
import com.azure.resourcemanager.portalservicescopilot.models.CopilotSettingsProperties;
import com.azure.resourcemanager.portalservicescopilot.models.CopilotSettingsResource;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class PortalservicescopilotManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.EUROPE_WEST;
    private String resourceGroupName = "rg" + randomPadding();
    private PortalservicescopilotManager portalservicescopilotManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        portalservicescopilotManager = PortalservicescopilotManager.configure()
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
    @Disabled("The client '99c13838-960f-4a77-9925-2111e5a104b4' with object id '99c13838-960f-4a77-9925-2111e5a104b4' does not have authorization to perform action 'Microsoft.PortalServices/copilotSettings/write' over scope '/providers/Microsoft.PortalServices/copilotSettings/default' or the scope is invalid.")
    public void testCreateCopilotSettingsResource() {
        CopilotSettingsResource resource = null;
        try {
            // @embedmeStart
            resource = portalservicescopilotManager.copilotSettings()
                .createOrUpdate(new CopilotSettingsResourceInner()
                    .withProperties(new CopilotSettingsProperties().withAccessControlEnabled(true)));
            // @embedmeEnd
            Assertions.assertTrue(resource.properties().accessControlEnabled());
        } finally {
            if (resource != null) {
                portalservicescopilotManager.copilotSettings().delete();
            }

        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
