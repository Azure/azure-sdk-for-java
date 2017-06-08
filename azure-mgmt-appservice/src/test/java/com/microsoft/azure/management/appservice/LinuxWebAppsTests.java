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
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LinuxWebAppsTests extends AppServiceTest {
    private static String RG_NAME_1 = "";
    private static String RG_NAME_2 = "";
    private static String WEBAPP_NAME_1 = "";
    private static String WEBAPP_NAME_2 = "";
    private static OkHttpClient httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-webapp-", 20);
        WEBAPP_NAME_2 = generateRandomResourceName("java-webapp-", 20);
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
    @Ignore("Pending ICM 39157077 & https://github.com/Azure-App-Service/kudu/issues/30")
    public void canCRUDLinuxWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewLinuxPlan(PricingTier.BASIC_B1)
                .withPublicDockerHubImage("wordpress")
                .create();
        Assert.assertNotNull(webApp1);
        Assert.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assert.assertNotNull(plan1);
        Assert.assertEquals(Region.US_WEST, plan1.region());
        Assert.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());
        Assert.assertEquals(OperatingSystem.LINUX, plan1.operatingSystem());
        Assert.assertEquals(OperatingSystem.LINUX, webApp1.operatingSystem());

        // Create in a new group with existing app service plan
        WebApp webApp2 = appServiceManager.webApps().define(WEBAPP_NAME_2)
                .withExistingWindowsPlan(plan1)
                .withNewResourceGroup(RG_NAME_2)
                .create();
        Assert.assertNotNull(webApp2);
        Assert.assertEquals(Region.US_WEST, webApp2.region());
        Assert.assertEquals(OperatingSystem.LINUX, webApp2.operatingSystem());

        // Get
        WebApp webApp = appServiceManager.webApps().getByResourceGroup(RG_NAME_1, webApp1.name());
        Assert.assertEquals(OperatingSystem.LINUX, webApp.operatingSystem());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assert.assertEquals(OperatingSystem.LINUX, webApp.operatingSystem());

        // Update
        webApp1.update()
                .withNewAppServicePlan(PricingTier.STANDARD_S2)
                .apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assert.assertNotNull(plan2);
        Assert.assertEquals(Region.US_WEST, plan2.region());
        Assert.assertEquals(PricingTier.STANDARD_S2, plan2.pricingTier());
        Assert.assertEquals(OperatingSystem.LINUX, plan2.operatingSystem());

        webApp1.update()
                .withBuiltInImage(RuntimeStack.NODEJS_6_6)
                .defineSourceControl()
                    .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test")
                    .withBranch("master")
                    .attach()
                .apply();
        Assert.assertNotNull(webApp);
        if (!IS_MOCKED) {
            Response response = curl("http://" + webApp1.defaultHostName());
            Assert.assertEquals(200, response.code());
            String body = response.body().string();
            Assert.assertNotNull(body);
            Assert.assertTrue(body.contains("Hello world from linux 4"));
        }
    }

    private static Response curl(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return httpClient.newCall(request).execute();
    }
}