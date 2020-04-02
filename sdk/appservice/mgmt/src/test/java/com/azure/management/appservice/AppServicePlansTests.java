/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AppServicePlansTests extends AppServiceTest {
    private String APP_SERVICE_PLAN_NAME = "";

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
    @Disabled("Permanent server side issue: Cannot modify this web hosting plan because another operation is in progress")
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
        Assertions.assertNotNull(appServicePlan);
        Assertions.assertEquals(PricingTier.PREMIUM_P1, appServicePlan.pricingTier());
        Assertions.assertEquals(false, appServicePlan.perSiteScaling());
        Assertions.assertEquals(2, appServicePlan.capacity());
        Assertions.assertEquals(0, appServicePlan.numberOfWebApps());
        Assertions.assertEquals(20, appServicePlan.maxInstances());
        // GET
        Assertions.assertNotNull(appServiceManager.appServicePlans().getByResourceGroup(RG_NAME, APP_SERVICE_PLAN_NAME));
        // LIST
        PagedIterable<AppServicePlan> appServicePlans = appServiceManager.appServicePlans().listByResourceGroup(RG_NAME);
        boolean found = false;
        for (AppServicePlan asp : appServicePlans) {
            if (APP_SERVICE_PLAN_NAME.equals(asp.name())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // UPDATE
        appServicePlan = appServicePlan.update()
                .withPricingTier(PricingTier.STANDARD_S1)
                .withPerSiteScaling(true)
                .withCapacity(3)
                .apply();
        Assertions.assertNotNull(appServicePlan);
        Assertions.assertEquals(PricingTier.STANDARD_S1, appServicePlan.pricingTier());
        Assertions.assertEquals(true, appServicePlan.perSiteScaling());
        Assertions.assertEquals(3, appServicePlan.capacity());
    }
}