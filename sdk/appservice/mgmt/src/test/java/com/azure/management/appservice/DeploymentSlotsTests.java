/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeploymentSlotsTests extends AppServiceTest {
    private String WEBAPP_NAME = "";
    private String SLOT_NAME_1 = "";
    private String SLOT_NAME_2 = "";
    private String SLOT_NAME_3 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME = generateRandomResourceName("java-webapp-", 20);
        SLOT_NAME_1 = generateRandomResourceName("java-slot-", 20);
        SLOT_NAME_2 = generateRandomResourceName("java-slot-", 20);
        SLOT_NAME_3 = generateRandomResourceName("java-slot-", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Test
    public void canCRUDSwapSlots() throws Exception {
        // Create web app
        WebApp webApp = appServiceManager.webApps().define(WEBAPP_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withNewWindowsPlan(PricingTier.STANDARD_S2)
                .withJavaVersion(JavaVersion.JAVA_1_7_0_51)
                .withWebContainer(WebContainer.TOMCAT_7_0_50)
                .create();
        Assertions.assertNotNull(webApp);
        Assertions.assertEquals(Region.US_WEST, webApp.region());

        // Create a deployment slot with web app's config
        DeploymentSlot slot2 = webApp.deploymentSlots().define(SLOT_NAME_2)
                .withConfigurationFromParent()
                .create();
        Assertions.assertNotNull(slot2);
        Assertions.assertEquals(JavaVersion.JAVA_1_7_0_51, slot2.javaVersion());

        // Update deployment slot
        slot2.update()
                .withoutJava()
                .withPythonVersion(PythonVersion.PYTHON_34)
                .withAppSetting("slot2key", "slot2value")
                .withStickyAppSetting("sticky2key", "sticky2value")
                .apply();
        Assertions.assertNotNull(slot2);
        Assertions.assertEquals(JavaVersion.OFF, slot2.javaVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot2.pythonVersion());

        // Create 3rd deployment slot with configuration from slot 2
        DeploymentSlot slot3 = webApp.deploymentSlots().define(SLOT_NAME_3)
                .withConfigurationFromDeploymentSlot(slot2)
                .create();
        Assertions.assertNotNull(slot3);
        Assertions.assertEquals(JavaVersion.OFF, slot3.javaVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot3.pythonVersion());

        // Get
        DeploymentSlot deploymentSlot = webApp.deploymentSlots().getByName(SLOT_NAME_3);
        Assertions.assertEquals(slot3.id(), deploymentSlot.id());

    }
}