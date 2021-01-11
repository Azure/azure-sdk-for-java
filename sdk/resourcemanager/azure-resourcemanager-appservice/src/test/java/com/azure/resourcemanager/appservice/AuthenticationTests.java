// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.BuiltInAuthenticationProvider;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AuthenticationTests extends AppServiceTest {
    private String rgName1 = "";
    private String webappName1 = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName1 = generateRandomResourceName("java-webapp-", 20);
        rgName1 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName1);
    }

    @Test
    @Disabled("Need facebook developer account")
    public void canCRUDWebAppWithAuthentication() throws Exception {
        // Create with new app service plan
        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
                .withNewWindowsPlan(PricingTier.BASIC_B1)
                .defineAuthentication()
                .withDefaultAuthenticationProvider(BuiltInAuthenticationProvider.FACEBOOK)
                .withFacebook("appId", "appSecret")
                .attach()
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());

        String response = curl("http://" + webApp1.defaultHostname()).getValue();
        Assertions.assertTrue(response.contains("do not have permission"));

        // Update
        webApp1
            .update()
            .defineAuthentication()
            .withAnonymousAuthentication()
            .withFacebook("appId", "appSecret")
            .attach()
            .apply();

        response = curl("http://" + webApp1.defaultHostname()).getValue();
        Assertions.assertFalse(response.contains("do not have permission"));
    }
}
