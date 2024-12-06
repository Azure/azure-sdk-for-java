// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

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
        if (rgName2 != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName2);
        }
        if (rgName1 != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName1);
        }
    }

    @Test
    public void canCRUDLinuxWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps()
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
        WebApp webApp2 = appServiceManager.webApps()
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
            try {
                // warm up
                curl("https://" + webApp.defaultHostname());
            } catch (Exception e) {
                // ignore
            }
        }
        byte[] logs = webApp.getContainerLogs();
        Assertions.assertTrue(logs.length > 0);
        byte[] logsZip = webApp.getContainerLogsZip();
        if (!isPlaybackMode()) {
            ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(logsZip));
            Assertions.assertNotNull(zipInputStream.getNextEntry());
            byte[] unzipped = readAllBytes(zipInputStream);
            Assertions.assertTrue(unzipped.length > 0);
        }

        // Update
        webApp = webApp1.update().withNewAppServicePlan(PricingTier.STANDARD_S2).apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(webApp.appServicePlanId());
        Assertions.assertNotNull(plan2);
        Assertions.assertEquals(Region.US_WEST, plan2.region());
        Assertions.assertEquals(PricingTier.STANDARD_S2, plan2.pricingTier());
        Assertions.assertEquals(OperatingSystem.LINUX, plan2.operatingSystem());

        // update to a java 11 image
        webApp = webApp1.update().withBuiltInImage(RuntimeStack.TOMCAT_9_0_JAVA11).apply();
        Assertions.assertNotNull(webApp);
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];

        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        return outputStream.toByteArray();
    }

    @Test
    public void canCRUDLinuxJava11WebApp() throws Exception {
        rgName2 = null;
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps()
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
    }

    @Test
    public void canCRUDLinuxJava17WebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps()
            .define(webappName1)
            .withRegion(Region.US_WEST)
            .withNewResourceGroup(rgName1)
            .withNewLinuxPlan(PricingTier.BASIC_B1)
            .withBuiltInImage(RuntimeStack.TOMCAT_10_0_JAVA17)
            .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());
        Assertions.assertEquals(OperatingSystem.LINUX, plan1.operatingSystem());
        Assertions.assertEquals(OperatingSystem.LINUX, webApp1.operatingSystem());
        Assertions.assertEquals(
            String.format("%s|%s", RuntimeStack.TOMCAT_10_0_JAVA17.stack(), RuntimeStack.TOMCAT_10_0_JAVA17.version()),
            webApp1.linuxFxVersion());

        rgName2 = null;
    }
}
