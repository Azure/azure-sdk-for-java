/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AuthenticationTests extends AppServiceTest {
    private String RG_NAME_1 = "";
    private String WEBAPP_NAME_1 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-webapp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_1);
    }

    @Test
    @Disabled("Need facebook developer account")
    public void canCRUDWebAppWithAuthentication() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
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

        String response = curl("http://" + webApp1.defaultHostName()).getValue();
        Assertions.assertTrue(response.contains("do not have permission"));

        // Update
        webApp1.update()
                .defineAuthentication()
                    .withAnonymousAuthentication()
                    .withFacebook("appId", "appSecret")
                    .attach()
                .apply();

        response = curl("http://" + webApp1.defaultHostName()).getValue();
        Assertions.assertFalse(response.contains("do not have permission"));

    }
}