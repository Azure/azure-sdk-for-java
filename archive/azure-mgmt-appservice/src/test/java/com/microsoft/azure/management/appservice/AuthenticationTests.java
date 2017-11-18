/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class AuthenticationTests extends AppServiceTest {
    private static String RG_NAME_1 = "";
    private static String WEBAPP_NAME_1 = "";

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
    @Ignore("Need facebook developer account")
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
        Assert.assertNotNull(webApp1);
        Assert.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assert.assertNotNull(plan1);
        Assert.assertEquals(Region.US_WEST, plan1.region());
        Assert.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());

        Request request = new Request.Builder().url("http://" + webApp1.defaultHostName()).get().build();
        String response = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build().newCall(request).execute().body().string();
        Assert.assertTrue(response.contains("do not have permission"));

        // Update
        webApp1.update()
                .updateAuthentication()
                    .withAnonymousAuthentication()
                    .parent()
                .apply();

        request = new Request.Builder().url("http://" + webApp1.defaultHostName()).get().build();
        response = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build().newCall(request).execute().body().string();
        Assert.assertFalse(response.contains("do not have permission"));

    }
}