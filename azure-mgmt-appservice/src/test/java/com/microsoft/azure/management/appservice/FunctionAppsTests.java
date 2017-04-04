/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FunctionAppsTests extends AppServiceTest {
    private static String RG_NAME_1 = "";
    private static String RG_NAME_2 = "";
    private static String WEBAPP_NAME_1 = "";
    private static String WEBAPP_NAME_2 = "";
    private static String WEBAPP_NAME_3 = "";
    private static String APP_SERVICE_PLAN_NAME_1 = "";
    private static String APP_SERVICE_PLAN_NAME_2 = "";
    private static String STORAGE_ACCOUNT_NAME_1 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-func-", 20);
        WEBAPP_NAME_2 = generateRandomResourceName("java-func-", 20);
        WEBAPP_NAME_3 = generateRandomResourceName("java-func-", 20);
        APP_SERVICE_PLAN_NAME_1 = generateRandomResourceName("java-asp-", 20);
        APP_SERVICE_PLAN_NAME_2 = generateRandomResourceName("java-asp-", 20);
        STORAGE_ACCOUNT_NAME_1 = generateRandomResourceName("javastore", 20);
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
    public void canCRUDFunctionApp() throws Exception {
        // Create with consumption
        FunctionApp functionApp1 = appServiceManager.functionApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .create();
        Assert.assertNotNull(functionApp1);
        Assert.assertEquals(Region.US_WEST, functionApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(functionApp1.appServicePlanId());
        Assert.assertNotNull(plan1);
        Assert.assertEquals(Region.US_WEST, plan1.region());
        Assert.assertEquals(new PricingTier("Dynamic", "Y1"), plan1.pricingTier());

        // Create with the same consumption plan
        FunctionApp functionApp2 = appServiceManager.functionApps().define(WEBAPP_NAME_2)
                .withExistingAppServicePlan(plan1)
                .withNewResourceGroup(RG_NAME_2)
                .withExistingStorageAccount(functionApp1.storageAccount())
                .create();
        Assert.assertNotNull(functionApp2);
        Assert.assertEquals(Region.US_WEST, functionApp2.region());

        // Create with app service plan
        FunctionApp functionApp3 = appServiceManager.functionApps().define(WEBAPP_NAME_3)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME_2)
                .withNewAppServicePlan(PricingTier.BASIC_B1)
                .withExistingStorageAccount(functionApp1.storageAccount())
                .create();
        Assert.assertNotNull(functionApp2);
        Assert.assertEquals(Region.US_WEST, functionApp2.region());

        // Get
        FunctionApp functionApp = appServiceManager.functionApps().getByResourceGroup(RG_NAME_1, functionApp1.name());
        Assert.assertEquals(functionApp1.id(), functionApp.id());
        functionApp = appServiceManager.functionApps().getById(functionApp2.id());
        Assert.assertEquals(functionApp2.name(), functionApp.name());

        // List
        List<FunctionApp> functionApps = appServiceManager.functionApps().listByResourceGroup(RG_NAME_1);
        Assert.assertEquals(1, functionApps.size());
        functionApps = appServiceManager.functionApps().listByResourceGroup(RG_NAME_2);
        Assert.assertEquals(2, functionApps.size());

        // Update
        functionApp2.update()
                .withNewStorageAccount(STORAGE_ACCOUNT_NAME_1, SkuName.STANDARD_GRS)
                .apply();
        Assert.assertEquals(STORAGE_ACCOUNT_NAME_1, functionApp2.storageAccount().name());

        // Scale
        functionApp3.update()
                .withNewAppServicePlan(PricingTier.STANDARD_S2)
                .apply();
        Assert.assertNotEquals(functionApp3.appServicePlanId(), functionApp1.appServicePlanId());
    }
}