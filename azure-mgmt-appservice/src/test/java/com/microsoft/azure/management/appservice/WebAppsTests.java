/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class WebAppsTests extends AppServiceTest {
    private static String RG_NAME_1 = "";
    private static String RG_NAME_2 = "";
    private static String WEBAPP_NAME_1 = "";
    private static String WEBAPP_NAME_2 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-webapp-", 20);
        WEBAPP_NAME_2 = generateRandomResourceName("java-webapp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);
        RG_NAME_2 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_2);
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_1);
    }

    @Test
    public void canCRUDWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewWindowsPlan(PricingTier.BASIC_B1)
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2013)
                .create();
        Assert.assertNotNull(webApp1);
        Assert.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assert.assertNotNull(plan1);
        Assert.assertEquals(Region.US_WEST, plan1.region());
        Assert.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());

        // Create in a new group with existing app service plan
        WebApp webApp2 = appServiceManager.webApps().define(WEBAPP_NAME_2)
                .withExistingWindowsPlan(plan1)
                .withNewResourceGroup(RG_NAME_2)
                .create();
        Assert.assertNotNull(webApp2);
        Assert.assertEquals(Region.US_WEST, webApp1.region());

        // Get
        WebApp webApp = appServiceManager.webApps().getByResourceGroup(RG_NAME_1, webApp1.name());
        Assert.assertEquals(webApp1.id(), webApp.id());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assert.assertEquals(webApp2.name(), webApp.name());

        // List
        List<WebApp> webApps = appServiceManager.webApps().listByResourceGroup(RG_NAME_1);
        Assert.assertEquals(1, webApps.size());
        webApps = appServiceManager.webApps().listByResourceGroup(RG_NAME_2);
        Assert.assertEquals(1, webApps.size());

        // Update
        webApp1.update()
                .withNewAppServicePlan(PricingTier.STANDARD_S2)
                .apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assert.assertNotNull(plan2);
        Assert.assertEquals(Region.US_WEST, plan2.region());
        Assert.assertEquals(PricingTier.STANDARD_S2, plan2.pricingTier());
    }
}