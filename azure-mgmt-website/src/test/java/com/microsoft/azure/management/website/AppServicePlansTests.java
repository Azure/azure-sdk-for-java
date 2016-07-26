/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class AppServicePlansTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg323";
    private static final String APP_SERVICE_PLAN_NAME = "java-appservice-plan-323";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCRUBAppServicePlan() throws Exception {
        // CREATE
        AppServicePlan appServicePlan = appServiceManager.appServicePlans()
                .define(APP_SERVICE_PLAN_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withPricingTier(AppServicePricingTier.BASIC_B1)
                .create();
        Assert.assertNotNull(appServicePlan);
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
    }
}
