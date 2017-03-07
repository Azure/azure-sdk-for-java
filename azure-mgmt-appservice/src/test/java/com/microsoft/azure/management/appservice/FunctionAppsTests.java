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

public class FunctionAppsTests extends AppServiceTest {
    private static String RG_NAME_1 = "";
    private static String RG_NAME_2 = "";
    private static String WEBAPP_NAME_1 = "";
    private static String WEBAPP_NAME_2 = "";
    private static String APP_SERVICE_PLAN_NAME_1 = "";
    private static String APP_SERVICE_PLAN_NAME_2 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-func-", 20);
        WEBAPP_NAME_2 = generateRandomResourceName("java-func-", 20);
        APP_SERVICE_PLAN_NAME_1 = generateRandomResourceName("java-asp-", 20);
        APP_SERVICE_PLAN_NAME_2 = generateRandomResourceName("java-asp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);
        RG_NAME_2 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME_2);
        resourceManager.resourceGroups().deleteByName(RG_NAME_1);
    }

    @Test
    public void canCRUDFunctionApp() throws Exception {
        // Create with new app service plan
        FunctionApp functionApp1 = appServiceManager.functionApps().define("delete-no-storage-6")
                .withNewResourceGroup(RG_NAME_1, Region.US_WEST)
                .withNewConsumptionPlan(Region.US_WEST)
                .withNewStorageAccount()
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2013)
                .create();
        Assert.assertNotNull(functionApp1);
        Assert.assertEquals(Region.US_WEST, functionApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getByGroup(RG_NAME_1, APP_SERVICE_PLAN_NAME_1);
        Assert.assertNotNull(plan1);
        Assert.assertEquals(Region.US_WEST, plan1.region());
        Assert.assertEquals(AppServicePricingTier.BASIC_B1, plan1.pricingTier());

        // Create in a new group with existing app service plan
        WebApp webApp2 = appServiceManager.webApps().define(WEBAPP_NAME_2)
                .withNewResourceGroup(RG_NAME_2, Region.US_WEST)
                .withExistingAppServicePlan(plan1)
                .create();
        Assert.assertNotNull(webApp2);
        Assert.assertEquals(Region.US_WEST, functionApp1.region());

        // Get
        WebApp webApp = appServiceManager.webApps().getByGroup(RG_NAME_1, functionApp1.name());
        Assert.assertEquals(functionApp1.id(), webApp.id());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assert.assertEquals(webApp2.name(), webApp.name());

        // List
        List<WebApp> webApps = appServiceManager.webApps().listByGroup(RG_NAME_1);
        Assert.assertEquals(1, webApps.size());
        webApps = appServiceManager.webApps().listByGroup(RG_NAME_2);
        Assert.assertEquals(1, webApps.size());

        // Update
        functionApp1.update()
                .withNewAppServicePlan(APP_SERVICE_PLAN_NAME_2)
                .withPricingTier(AppServicePricingTier.STANDARD_S2)
                .apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getByGroup(RG_NAME_1, APP_SERVICE_PLAN_NAME_2);
        Assert.assertNotNull(plan2);
        Assert.assertEquals(Region.US_WEST, plan2.region());
        Assert.assertEquals(AppServicePricingTier.STANDARD_S2, plan2.pricingTier());
    }
}