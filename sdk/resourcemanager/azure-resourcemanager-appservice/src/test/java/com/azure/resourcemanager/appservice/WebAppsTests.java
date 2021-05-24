// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionAppBasic;
import com.azure.resourcemanager.appservice.models.IpFilterTag;
import com.azure.resourcemanager.appservice.models.IpSecurityRestriction;
import com.azure.resourcemanager.appservice.models.LogLevel;
import com.azure.resourcemanager.appservice.models.NetFrameworkVersion;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RemoteVisualStudioVersion;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebAppBasic;
import com.azure.resourcemanager.appservice.models.WebAppRuntimeStack;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
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
        if (rgName2 != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName2);
        }
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
        PagedIterable<WebAppBasic> webApps = appServiceManager.webApps().listByResourceGroup(rgName1);
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
                    .serviceClient()
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
                    .serviceClient()
                    .getWebApps()
                    .listMetadata(webApp3.resourceGroupName(), webApp3.name())
                    .properties()
                    .get("CURRENT_STACK"));
    }

    @Test
    public void canListWebApp() throws Exception {
        rgName2 = null;

        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
                .withNewWindowsPlan(appServicePlanName1, PricingTier.BASIC_B1)
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2019)
                .withHttpsOnly(true)
                .defineDiagnosticLogsConfiguration()
                    .withApplicationLogging()
                    .withLogLevel(LogLevel.VERBOSE)
                    .withApplicationLogsStoredOnFileSystem()
                    .attach()
                .create();

        PagedIterable<WebAppBasic> webApps = appServiceManager.webApps()
            .listByResourceGroup(rgName1);
        Assertions.assertEquals(1, TestUtilities.getSize(webApps));

        WebAppBasic webAppBasic1 = webApps.iterator().next();
        // verify basic info
        Assertions.assertEquals(webApp1.id(), webAppBasic1.id());
        Assertions.assertEquals(webApp1.name(), webAppBasic1.name());
        Assertions.assertEquals(webApp1.appServicePlanId(), webAppBasic1.appServicePlanId());
        Assertions.assertEquals(webApp1.operatingSystem(), webAppBasic1.operatingSystem());
        Assertions.assertEquals(webApp1.httpsOnly(), webAppBasic1.httpsOnly());
        // verify detailed info after refresh
        WebApp webAppBasic1Refreshed = webAppBasic1.refresh();
        Assertions.assertEquals(webApp1.remoteDebuggingVersion(), webAppBasic1Refreshed.remoteDebuggingVersion());
        Assertions.assertEquals(webApp1.diagnosticLogsConfig().applicationLoggingStorageBlobLogLevel(),
            webAppBasic1Refreshed.diagnosticLogsConfig().applicationLoggingStorageBlobLogLevel());
    }

    @Test
    public void canCRUDWebAppWithContainer() {
        rgName2 = null;

        AppServicePlan plan1 = appServiceManager.appServicePlans().define(appServicePlanName1)
            .withRegion(Region.US_EAST)     // many other regions does not have quota for PREMIUM_P1V3
            .withNewResourceGroup(rgName1)
            .withPricingTier(PricingTier.PREMIUM_P1V3)
            .withOperatingSystem(OperatingSystem.WINDOWS)
            .create();

        final String imageAndTag = "mcr.microsoft.com/azure-app-service/samples/aspnethelloworld:latest";

        WebApp webApp1 = appServiceManager.webApps().define(webappName1)
            .withExistingWindowsPlan(plan1)
            .withExistingResourceGroup(rgName1)
            .withPublicDockerHubImage(imageAndTag)
            .create();

        Assertions.assertNotNull(webApp1.windowsFxVersion());
        Assertions.assertTrue(webApp1.windowsFxVersion().contains(imageAndTag));
    }

    @Test
    public void canListWebAppAndFunctionApp() {
        rgName2 = null;

        WebApp webApp1 = appServiceManager.webApps()
            .define(webappName1)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName1)
            .withNewWindowsPlan(appServicePlanName1, PricingTier.BASIC_B1)
            .create();

        FunctionApp functionApp1 = appServiceManager.functionApps()
            .define(webappName2)
            .withRegion(Region.US_WEST)
            .withExistingResourceGroup(rgName1)
            .withNewFreeAppServicePlan()
            .create();

        PagedIterable<WebAppBasic> webApps = appServiceManager.webApps().listByResourceGroup(rgName1);

        PagedIterable<FunctionAppBasic> functionApps = appServiceManager.functionApps().listByResourceGroup(rgName1);

        Assertions.assertEquals(1, TestUtilities.getSize(webApps));

        Assertions.assertEquals(1, TestUtilities.getSize(functionApps));

        Assertions.assertEquals(webappName1, webApps.iterator().next().name());

        Assertions.assertEquals(webappName2, functionApps.iterator().next().name());
    }

    @Test
    public void canUpdateIpRestriction() {
        WebApp webApp2 =
            appServiceManager
                .webApps()
                .define(webappName2)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName2)
                .withNewWindowsPlan(appServicePlanName1, PricingTier.BASIC_B1)
                .create();
        webApp2.refresh();

        Assertions.assertEquals(1, webApp2.ipSecurityRules().size());
        Assertions.assertEquals("Allow", webApp2.ipSecurityRules().iterator().next().action());
        Assertions.assertEquals("Any", webApp2.ipSecurityRules().iterator().next().ipAddress());

        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
                .withNewWindowsPlan(appServicePlanName1, PricingTier.BASIC_B1)
                .withAccessFromIpAddressRange("167.220.0.0/16", 300)
                .withAccessFromIpAddress("167.220.0.1", 400)
                .withAccessRule(new IpSecurityRestriction()
                    .withAction("Allow")
                    .withPriority(500)
                    .withTag(IpFilterTag.SERVICE_TAG)
                    .withIpAddress("AzureFrontDoor.Backend"))
                .create();

        Assertions.assertEquals(3 + 1, webApp1.ipSecurityRules().size());
        Assertions.assertTrue(webApp1.ipSecurityRules().stream().anyMatch(r -> "Deny".equals(r.action()) && "Any".equals(r.ipAddress())));

        IpSecurityRestriction serviceTagRule = webApp1.ipSecurityRules().stream()
            .filter(r -> r.tag() == IpFilterTag.SERVICE_TAG)
            .findFirst().get();

        webApp1.update()
            .withoutIpAddressAccess("167.220.0.1")
            .withoutIpAddressRangeAccess("167.220.0.0/16")
            .withoutAccessRule(serviceTagRule)
            .withAccessFromIpAddressRange("167.220.0.0/24", 300)
            .apply();

        Assertions.assertEquals(1 + 1, webApp1.ipSecurityRules().size());

        webApp1.update()
            .withAccessFromAllNetworks()
            .apply();

        Assertions.assertEquals(1, webApp1.ipSecurityRules().size());
        Assertions.assertEquals("Allow", webApp1.ipSecurityRules().iterator().next().action());
        Assertions.assertEquals("Any", webApp1.ipSecurityRules().iterator().next().ipAddress());
    }
}
