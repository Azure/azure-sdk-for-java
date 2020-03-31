/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.Response;
import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipInputStream;

public class LinuxWebAppsTests extends AppServiceTest {
    private String RG_NAME_1 = "";
    private String RG_NAME_2 = "";
    private String WEBAPP_NAME_1 = "";
    private String WEBAPP_NAME_2 = "";

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
//    @Ignore("Pending ICM 39157077 & https://github.com/Azure-App-Service/kudu/issues/30")
    public void canCRUDLinuxWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewLinuxPlan(PricingTier.BASIC_B1)
                .withPublicDockerHubImage("wordpress")
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());
        Assertions.assertEquals(OperatingSystem.LINUX, plan1.operatingSystem());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp1.operatingSystem());

        // Create in a new group with existing app service plan
        WebApp webApp2 = appServiceManager.webApps().define(WEBAPP_NAME_2)
                .withExistingLinuxPlan(plan1)
                .withNewResourceGroup(RG_NAME_2)
                .withPublicDockerHubImage("tomcat")
                .withContainerLoggingEnabled()
                .create();
        Assertions.assertNotNull(webApp2);
        Assertions.assertEquals(Region.US_WEST, webApp2.region());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp2.operatingSystem());

        // Get
        WebApp webApp = appServiceManager.webApps().getByResourceGroup(RG_NAME_1, webApp1.name());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp.operatingSystem());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp.operatingSystem());

        // View logs
        if (!isPlaybackMode()) {
            // warm up
            curl("http://" + webApp.defaultHostName());
        }
        byte[] logs = webApp.getContainerLogs();
        Assertions.assertTrue(logs.length > 0);
        byte[] logsZip = webApp.getContainerLogsZip();
        if (!isPlaybackMode()) {
            ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(logsZip));
            Assertions.assertNotNull(zipInputStream.getNextEntry());
            byte[] unzipped = IOUtils.toByteArray(zipInputStream);
            Assertions.assertTrue(unzipped.length > 0);
        }

        // Update
        webApp = webApp1.update()
                .withNewAppServicePlan(PricingTier.STANDARD_S2)
                .apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan2);
        Assertions.assertEquals(Region.US_WEST, plan2.region());
        Assertions.assertEquals(PricingTier.STANDARD_S2, plan2.pricingTier());
        Assertions.assertEquals(OperatingSystem.LINUX, plan2.operatingSystem());

        webApp = webApp1.update()
                .withBuiltInImage(RuntimeStack.NODEJS_6_6)
                .defineSourceControl()
                    .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test.git")
                    .withBranch("master")
                    .attach()
                .apply();
        Assertions.assertNotNull(webApp);
        if (!isPlaybackMode()) {
            // maybe 2 minutes is enough?
            SdkContext.sleep(120000);
            Response<String> response = curl("http://" + webApp1.defaultHostName());
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Hello world from linux 4"));
        }

         //update to a java 11 image
         webApp = webApp1.update()
                    .withBuiltInImage(RuntimeStack.TOMCAT_9_0_JAVA11)
                    .apply();
         Assertions.assertNotNull(webApp);
    }

    @Test
//    @Ignore("Pending ICM 39157077 & https://github.com/Azure-App-Service/kudu/issues/30")
    public void canCRUDLinuxJava11WebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewLinuxPlan(PricingTier.BASIC_B1)
                .withBuiltInImage(RuntimeStack.TOMCAT_9_0_JAVA11)
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());
        Assertions.assertEquals(OperatingSystem.LINUX, plan1.operatingSystem());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp1.operatingSystem());

        WebApp webApp2 = appServiceManager.webApps().define(WEBAPP_NAME_2)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_2)
                .withNewLinuxPlan(PricingTier.BASIC_B2)
                .withBuiltInImage(RuntimeStack.TOMCAT_8_5_JAVA11)
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp2.region());
        plan1 = appServiceManager.appServicePlans().getById(webApp2.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B2, plan1.pricingTier());
        Assertions.assertEquals(OperatingSystem.LINUX, plan1.operatingSystem());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp2.operatingSystem());

        WebApp webApp = webApp1.update()
                .withBuiltInImage(RuntimeStack.NODEJS_6_6)
                .defineSourceControl()
                .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test.git")
                .withBranch("master")
                .attach()
                .apply();
        Assertions.assertNotNull(webApp);
        if (!isPlaybackMode()) {
            // maybe 2 minutes is enough?
            SdkContext.sleep(120000);
            Response<String> response = curl("https://" + webApp1.defaultHostName());
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Hello world from linux 4"));
        }
    }
}