// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.NetFrameworkVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RemoteVisualStudioVersion;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebAppRuntimeStack;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebAppsTests extends AppServiceTest {
    private String rgName1 = "";
    private String rgName2 = "";
    private String webappName1 = "";
    private String webappName2 = "";
    private String webappName3 = "";
    private String appServicePlanName1 = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName1 = generateRandomResourceName("java-webapp-", 20);
        webappName2 = generateRandomResourceName("java-webapp-", 20);
        webappName3 = generateRandomResourceName("java-webapp-", 20);
        appServicePlanName1 = generateRandomResourceName("java-asp-", 20);
        rgName1 = generateRandomResourceName("javacsmrg", 20);
        rgName2 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName2);
        resourceManager.resourceGroups().beginDeleteByName(rgName1);
    }

    @Test
    public void canCRUDWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
                .withNewWindowsPlan(appServicePlanName1, PricingTier.BASIC_B1)
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2019)
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(appServicePlanName1, plan1.name());
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());

        // Create in a new group with existing app service plan
        WebApp webApp2 =
            appServiceManager
                .webApps()
                .define(webappName2)
                .withExistingWindowsPlan(plan1)
                .withNewResourceGroup(rgName2)
                .create();
        Assertions.assertNotNull(webApp2);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());

        // Get
        WebApp webApp = appServiceManager.webApps().getByResourceGroup(rgName1, webApp1.name());
        Assertions.assertEquals(webApp1.id(), webApp.id());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assertions.assertEquals(webApp2.name(), webApp.name());

        // List
        PagedIterable<WebApp> webApps = appServiceManager.webApps().listByResourceGroup(rgName1);
        Assertions.assertEquals(1, TestUtilities.getSize(webApps));
        webApps = appServiceManager.webApps().listByResourceGroup(rgName2);
        Assertions.assertEquals(1, TestUtilities.getSize(webApps));

        // Update
        webApp1
            .update()
            .withNewAppServicePlan(PricingTier.STANDARD_S2)
            .withRuntimeStack(WebAppRuntimeStack.NETCORE)
            .apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan2);
        Assertions.assertEquals(Region.US_WEST, plan2.region());
        Assertions.assertEquals(PricingTier.STANDARD_S2, plan2.pricingTier());
        Assertions
            .assertEquals(
                WebAppRuntimeStack.NETCORE.runtime(),
                webApp1
                    .manager()
                    .inner()
                    .getWebApps()
                    .listMetadata(webApp1.resourceGroupName(), webApp1.name())
                    .properties()
                    .get("CURRENT_STACK"));

        WebApp webApp3 =
            appServiceManager
                .webApps()
                .define(webappName3)
                .withExistingWindowsPlan(plan1)
                .withExistingResourceGroup(rgName2)
                .withRuntimeStack(WebAppRuntimeStack.NET)
                .withNetFrameworkVersion(NetFrameworkVersion.V4_6)
                .create();
        Assertions
            .assertEquals(
                WebAppRuntimeStack.NET.runtime(),
                webApp3
                    .manager()
                    .inner()
                    .getWebApps()
                    .listMetadata(webApp3.resourceGroupName(), webApp3.name())
                    .properties()
                    .get("CURRENT_STACK"));
    }
}
