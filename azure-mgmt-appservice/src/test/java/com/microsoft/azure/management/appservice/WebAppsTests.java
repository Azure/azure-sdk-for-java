/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class WebAppsTests extends AppServiceTestBase {
    private static final String RG_NAME_1 = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String RG_NAME_2 = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String WEBAPP_NAME_1 = ResourceNamer.randomResourceName("java-webapp-", 20);
    private static final String WEBAPP_NAME_2 = ResourceNamer.randomResourceName("java-webapp-", 20);
    private static final String APP_SERVICE_PLAN_NAME_1 = ResourceNamer.randomResourceName("java-asp-", 20);
    private static final String APP_SERVICE_PLAN_NAME_2 = ResourceNamer.randomResourceName("java-asp-", 20);

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().deleteByName(RG_NAME_1);
        resourceManager.resourceGroups().deleteByName(RG_NAME_2);
    }

    @Test
    public void canCRUDWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withNewResourceGroup(RG_NAME_1)
                .withNewAppServicePlan(APP_SERVICE_PLAN_NAME_1)
                .withRegion(Region.US_WEST)
                .withPricingTier(AppServicePricingTier.BASIC_B1)
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2013)
                .create();
        Assert.assertNotNull(webApp1);
        Assert.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getByGroup(RG_NAME_1, APP_SERVICE_PLAN_NAME_1);
        Assert.assertNotNull(plan1);
        Assert.assertEquals(Region.US_WEST, plan1.region());
        Assert.assertEquals(AppServicePricingTier.BASIC_B1, plan1.pricingTier());

        // Create in a new group with existing app service plan
        WebApp webApp2 = appServiceManager.webApps().define(WEBAPP_NAME_2)
                .withNewResourceGroup(RG_NAME_2)
                .withExistingAppServicePlan(plan1)
                .create();
        Assert.assertNotNull(webApp2);
        Assert.assertEquals(Region.US_WEST, webApp1.region());

        // Get
        WebApp webApp = appServiceManager.webApps().getByGroup(RG_NAME_1, webApp1.name());
        Assert.assertEquals(webApp1.id(), webApp.id());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assert.assertEquals(webApp2.name(), webApp.name());

        // List
        List<WebApp> webApps = appServiceManager.webApps().listByGroup(RG_NAME_1);
        Assert.assertEquals(1, webApps.size());
        webApps = appServiceManager.webApps().listByGroup(RG_NAME_2);
        Assert.assertEquals(1, webApps.size());

        // Update
        webApp1.update()
                .withNewAppServicePlan(APP_SERVICE_PLAN_NAME_2)
                .withPricingTier(AppServicePricingTier.STANDARD_S2)
                .apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getByGroup(RG_NAME_1, APP_SERVICE_PLAN_NAME_2);
        Assert.assertNotNull(plan2);
        Assert.assertEquals(Region.US_WEST, plan2.region());
        Assert.assertEquals(AppServicePricingTier.STANDARD_S2, plan2.pricingTier());
    }
}