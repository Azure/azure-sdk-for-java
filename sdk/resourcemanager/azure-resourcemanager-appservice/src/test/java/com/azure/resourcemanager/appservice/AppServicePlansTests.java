// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AppServicePlansTests extends AppServiceTest {
    private String appServicePlanName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        appServicePlanName = generateRandomResourceName("java-asp-", 20);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    @Disabled(
        "Permanent server side issue: Cannot modify this web hosting plan because another operation is in progress")
    public void canCRUDAppServicePlan() throws Exception {
        // CREATE
        AppServicePlan appServicePlan =
            appServiceManager
                .appServicePlans()
                .define(appServicePlanName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
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
        Assertions
            .assertNotNull(appServiceManager.appServicePlans().getByResourceGroup(rgName, appServicePlanName));
        // LIST
        PagedIterable<AppServicePlan> appServicePlans =
            appServiceManager.appServicePlans().listByResourceGroup(rgName);
        boolean found = false;
        for (AppServicePlan asp : appServicePlans) {
            if (appServicePlanName.equals(asp.name())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // UPDATE
        appServicePlan =
            appServicePlan
                .update()
                .withPricingTier(PricingTier.STANDARD_S1)
                .withPerSiteScaling(true)
                .withCapacity(3)
                .apply();
        Assertions.assertNotNull(appServicePlan);
        Assertions.assertEquals(PricingTier.STANDARD_S1, appServicePlan.pricingTier());
        Assertions.assertEquals(true, appServicePlan.perSiteScaling());
        Assertions.assertEquals(3, appServicePlan.capacity());
    }

    @Test
    public void failOnAppServiceNotFound() {
        resourceManager.resourceGroups().define(rgName)
            .withRegion(Region.US_WEST)
            .create();
        Assertions.assertThrows(ManagementException.class, () -> {
            appServiceManager.appServicePlans().getByResourceGroup("rgName", "no_such_appservice");
        });
    }
}
