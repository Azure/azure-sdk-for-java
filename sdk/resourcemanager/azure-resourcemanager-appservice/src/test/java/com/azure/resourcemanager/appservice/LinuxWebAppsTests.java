// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import java.io.ByteArrayInputStream;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LinuxWebAppsTests extends AppServiceTest {
    private String rgName1 = "";
    private String rgName2 = "";
    private String webappName1 = "";
    private String webappName2 = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName1 = generateRandomResourceName("java-webapp-", 20);
        webappName2 = generateRandomResourceName("java-webapp-", 20);
        rgName1 = generateRandomResourceName("javacsmrg", 20);
        rgName2 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName2);
        resourceManager.resourceGroups().beginDeleteByName(rgName1);
    }

    @Test
    //    @Ignore("Pending ICM 39157077 & https://github.com/Azure-App-Service/kudu/issues/30")
    public void canCRUDLinuxWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
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
        WebApp webApp2 =
            appServiceManager
                .webApps()
                .define(webappName2)
                .withExistingLinuxPlan(plan1)
                .withNewResourceGroup(rgName2)
                .withPublicDockerHubImage("tomcat")
                .withContainerLoggingEnabled()
                .create();
        Assertions.assertNotNull(webApp2);
        Assertions.assertEquals(Region.US_WEST, webApp2.region());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp2.operatingSystem());

        // Get
        WebApp webApp = appServiceManager.webApps().getByResourceGroup(rgName1, webApp1.name());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp.operatingSystem());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp.operatingSystem());

        // View logs
        if (!isPlaybackMode()) {
            // warm up
            curl("http://" + webApp.defaultHostname());
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
        webApp = webApp1.update().withNewAppServicePlan(PricingTier.STANDARD_S2).apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan2);
        Assertions.assertEquals(Region.US_WEST, plan2.region());
        Assertions.assertEquals(PricingTier.STANDARD_S2, plan2.pricingTier());
        Assertions.assertEquals(OperatingSystem.LINUX, plan2.operatingSystem());

        webApp =
            webApp1
                .update()
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
            Response<String> response = curl("http://" + webApp1.defaultHostname());
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Hello world from linux 4"));
        }

        // update to a java 11 image
        webApp = webApp1.update().withBuiltInImage(RuntimeStack.TOMCAT_9_0_JAVA11).apply();
        Assertions.assertNotNull(webApp);
    }

    @Test
    //    @Ignore("Pending ICM 39157077 & https://github.com/Azure-App-Service/kudu/issues/30")
    public void canCRUDLinuxJava11WebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
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

        WebApp webApp2 =
            appServiceManager
                .webApps()
                .define(webappName2)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName2)
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

        WebApp webApp =
            webApp1
                .update()
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
            Response<String> response = curl("https://" + webApp1.defaultHostname());
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Hello world from linux 4"));
        }
    }
}
