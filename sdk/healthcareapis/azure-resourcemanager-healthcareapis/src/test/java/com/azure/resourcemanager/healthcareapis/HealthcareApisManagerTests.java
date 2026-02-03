// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.healthcareapis;

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
import com.azure.resourcemanager.healthcareapis.models.*;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class HealthcareApisManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private HealthcareApisManager healthcareApisManager = null;
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

        healthcareApisManager = HealthcareApisManager.configure()
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
    public void testCreateHealthcareWorkspace() {
        Workspace workspace = null;
        try {
            String randomPadding = randomPadding();
            String workspaceName = "workspace" + randomPadding;
            // @embedmeStart
            workspace = healthcareApisManager.workspaces()
                .define(workspaceName)
                .withExistingResourceGroup(resourceGroupName)
                .withRegion(REGION)
                .withProperties(new WorkspaceProperties().withPublicNetworkAccess(PublicNetworkAccess.DISABLED))
                .create();
            // @embedmeEnd
            workspace.refresh();
            Assertions.assertEquals(workspace.name(), workspaceName);
            Assertions.assertEquals(workspace.name(),
                healthcareApisManager.workspaces().getById(workspace.id()).name());
            Assertions.assertTrue(healthcareApisManager.workspaces()
                .listByResourceGroup(resourceGroupName)
                .stream()
                .findAny()
                .isPresent());
        } finally {
            if (workspace != null) {
                healthcareApisManager.workspaces().deleteById(workspace.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
