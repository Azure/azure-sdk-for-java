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
import org.junit.Ignore;

import java.util.List;

public class AppServicePlansTests extends AppServiceTest {
    private static String APP_SERVICE_PLAN_NAME = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        APP_SERVICE_PLAN_NAME = generateRandomResourceName("java-asp-", 20);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    @Ignore("Permanent server side issue: Cannot modify this web hosting plan because another operation is in progress")
    public void canCRUDAppServicePlan() throws Exception {
        // CREATE
        AppServicePlan appServicePlan = appServiceManager.appServicePlans()
                .define(APP_SERVICE_PLAN_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withPricingTier(PricingTier.PREMIUM_P1)
                .withOperatingSystem(OperatingSystem.WINDOWS)
                .withPerSiteScaling(false)
                .withCapacity(2)
                .create();
        Assert.assertNotNull(appServicePlan);
        Assert.assertEquals(PricingTier.PREMIUM_P1, appServicePlan.pricingTier());
        Assert.assertEquals(false, appServicePlan.perSiteScaling());
        Assert.assertEquals(2, appServicePlan.capacity());
        Assert.assertEquals(0, appServicePlan.numberOfWebApps());
        Assert.assertEquals(20, appServicePlan.maxInstances());
        // GET
        Assert.assertNotNull(appServiceManager.appServicePlans().getByResourceGroup(RG_NAME, APP_SERVICE_PLAN_NAME));
        // LIST
        List<AppServicePlan> appServicePlans = appServiceManager.appServicePlans().listByResourceGroup(RG_NAME);
        boolean found = false;
        for (AppServicePlan asp : appServicePlans) {
            if (APP_SERVICE_PLAN_NAME.equals(asp.name())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // UPDATE
        appServicePlan = appServicePlan.update()
                .withPricingTier(PricingTier.STANDARD_S1)
                .withPerSiteScaling(true)
                .withCapacity(3)
                .apply();
        Assert.assertNotNull(appServicePlan);
        Assert.assertEquals(PricingTier.STANDARD_S1, appServicePlan.pricingTier());
        Assert.assertEquals(true, appServicePlan.perSiteScaling());
        Assert.assertEquals(3, appServicePlan.capacity());
    }
}