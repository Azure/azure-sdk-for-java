// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.PythonVersion;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.appservice.models.WebDeploymentSlotBasic;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeploymentSlotsTests extends AppServiceTest {
    private String webappName = "";
//    private String slotName1 = "";
    private String slotName2 = "";
    private String slotName3 = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName = generateRandomResourceName("java-webapp-", 20);
//        slotName1 = generateRandomResourceName("java-slot-", 20);
        slotName2 = generateRandomResourceName("java-slot-", 20);
        slotName3 = generateRandomResourceName("java-slot-", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void canCRUDSwapSlots() throws Exception {
        // Create web app
        WebApp webApp =
            appServiceManager
                .webApps()
                .define(webappName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S2)
                .withJavaVersion(JavaVersion.JAVA_1_7_0_51)
                .withWebContainer(WebContainer.TOMCAT_7_0_50)
                .create();
        Assertions.assertNotNull(webApp);
        Assertions.assertEquals(Region.US_WEST, webApp.region());

        // Create a deployment slot with web app's config
        DeploymentSlot slot2 = webApp.deploymentSlots().define(slotName2).withConfigurationFromParent().create();
        Assertions.assertNotNull(slot2);
        Assertions.assertEquals(JavaVersion.JAVA_1_7_0_51, slot2.javaVersion());

        // Update deployment slot
        slot2
            .update()
            .withoutJava()
            .withPythonVersion(PythonVersion.PYTHON_34)
            .withAppSetting("slot2key", "slot2value")
            .withStickyAppSetting("sticky2key", "sticky2value")
            .apply();
        Assertions.assertNotNull(slot2);
        Assertions.assertEquals(JavaVersion.OFF, slot2.javaVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot2.pythonVersion());

        // Create 3rd deployment slot with configuration from slot 2
        DeploymentSlot slot3 =
            webApp.deploymentSlots().define(slotName3)
                .withConfigurationFromDeploymentSlot(slot2)
                .withHttpsOnly(true)
                .create();
        Assertions.assertNotNull(slot3);
        Assertions.assertEquals(JavaVersion.OFF, slot3.javaVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot3.pythonVersion());
        Assertions.assertTrue(slot3.httpsOnly());

        // Get
        DeploymentSlot deploymentSlot = webApp.deploymentSlots().getByName(slotName3);
        Assertions.assertEquals(slot3.id(), deploymentSlot.id());

        // List
        WebDeploymentSlotBasic slotBasic3 = webApp.deploymentSlots().list().stream()
            .filter(slotBasic -> slotName3.equals(slotBasic.name()))
            .findFirst().orElse(null);
        Assertions.assertNotNull(slotBasic3);
        Assertions.assertEquals(slot3.id(), slotBasic3.id());
        Assertions.assertEquals(slot3.name(), slotBasic3.name());
        Assertions.assertEquals(slot3.appServicePlanId(), slotBasic3.appServicePlanId());
        Assertions.assertEquals(slot3.operatingSystem(), slotBasic3.operatingSystem());
        Assertions.assertEquals(slot3.httpsOnly(), slotBasic3.httpsOnly());

        DeploymentSlot slot3Refreshed = slotBasic3.refresh();
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot3Refreshed.pythonVersion());

        // Swap
        slot2.update()
            .withoutAppSetting("slot2key")
            .apply();

        DeploymentSlot slot3Swapped = slot3Refreshed;
        slot3Swapped.swap(slot2.name());
        Assertions.assertFalse(slot3Swapped.getAppSettings().containsKey("slot2key"));
    }
}
