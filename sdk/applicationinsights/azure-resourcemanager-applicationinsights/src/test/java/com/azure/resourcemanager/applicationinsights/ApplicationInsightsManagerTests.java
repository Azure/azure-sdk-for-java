// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.applicationinsights;

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
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponent;
import com.azure.resourcemanager.applicationinsights.models.ApplicationType;
import com.azure.resourcemanager.applicationinsights.models.IngestionMode;
import com.azure.resourcemanager.loganalytics.LogAnalyticsManager;
import com.azure.resourcemanager.loganalytics.models.Workspace;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ApplicationInsightsManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private ApplicationInsightsManager applicationInsightsManager;
    private LogAnalyticsManager logAnalyticsManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        applicationInsightsManager = ApplicationInsightsManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        logAnalyticsManager = LogAnalyticsManager
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
    public void testCreateComponent() {
        ApplicationInsightsComponent component = null;
        String randomPadding = randomPadding();
        try {
            String componentName = "component" + randomPadding;
            String spaceName = "space" + randomPadding;
            // @embedStart
            Workspace workspace = logAnalyticsManager.workspaces()
                .define(spaceName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .create();

            component = applicationInsightsManager.components()
                .define(componentName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withKind("web")
                .withApplicationType(ApplicationType.WEB)
                .withWorkspaceResourceId(workspace.id())
                .withIngestionMode(IngestionMode.LOG_ANALYTICS)
                .create();
            // @embedEnd
            component.refresh();
            Assertions.assertEquals(component.name(), componentName);
            Assertions.assertEquals(component.name(), applicationInsightsManager.components().getById(component.id()).name());
            Assertions.assertTrue(applicationInsightsManager.components().list().stream().findAny().isPresent());
        } finally {
            if (component != null) {
                applicationInsightsManager.components().deleteById(component.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
