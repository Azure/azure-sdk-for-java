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

public class AppServicePlansTests extends AppServiceTestBase {
    private static final String RG_NAME = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String APP_SERVICE_PLAN_NAME = ResourceNamer.randomResourceName("java-asp-", 20);

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCRUDAppServicePlan() throws Exception {
        // CREATE
        AppServicePlan appServicePlan = appServiceManager.appServicePlans()
                .define(APP_SERVICE_PLAN_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withPricingTier(AppServicePricingTier.PREMIUM_P1)
                .withPerSiteScaling(false)
                .withCapacity(2)
                .create();
        Assert.assertNotNull(appServicePlan);
        Assert.assertEquals(AppServicePricingTier.PREMIUM_P1, appServicePlan.pricingTier());
        Assert.assertEquals(false, appServicePlan.perSiteScaling());
        Assert.assertEquals(2, appServicePlan.capacity());
        Assert.assertEquals(0, appServicePlan.numberOfWebApps());
        Assert.assertEquals(20, appServicePlan.maxInstances());
        // GET
        Assert.assertNotNull(appServiceManager.appServicePlans().getByGroup(RG_NAME, APP_SERVICE_PLAN_NAME));
        // LIST
        List<AppServicePlan> appServicePlans = appServiceManager.appServicePlans().listByGroup(RG_NAME);
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
                .withPricingTier(AppServicePricingTier.STANDARD_S1)
                .withPerSiteScaling(true)
                .withCapacity(3)
                .apply();
        Assert.assertNotNull(appServicePlan);
        Assert.assertEquals(AppServicePricingTier.STANDARD_S1, appServicePlan.pricingTier());
        Assert.assertEquals(true, appServicePlan.perSiteScaling());
        Assert.assertEquals(3, appServicePlan.capacity());
    }
}