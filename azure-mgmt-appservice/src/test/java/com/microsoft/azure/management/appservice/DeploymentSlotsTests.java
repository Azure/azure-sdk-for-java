/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class DeploymentSlotsTests extends AppServiceTestBase {
    private static final String RG_NAME = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String WEBAPP_NAME = ResourceNamer.randomResourceName("java-webapp-", 20);
    private static final String SLOT_NAME_1 = ResourceNamer.randomResourceName("java-slot-", 20);
    private static final String SLOT_NAME_2 = ResourceNamer.randomResourceName("java-slot-", 20);
    private static final String SLOT_NAME_3 = ResourceNamer.randomResourceName("java-slot-", 20);
    private static final String APP_SERVICE_PLAN_NAME = ResourceNamer.randomResourceName("java-asp-", 20);

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCRUDSwapSlots() throws Exception {
        // Create web app
        WebApp webApp = appServiceManager.webApps().define(WEBAPP_NAME)
                .withNewResourceGroup(RG_NAME)
                .withNewAppServicePlan(APP_SERVICE_PLAN_NAME)
                .withRegion(Region.US_WEST)
                .withPricingTier(AppServicePricingTier.STANDARD_S2)
                .withAppSetting("appkey", "appvalue")
                .withStickyAppSetting("stickykey", "stickyvalue")
                .withConnectionString("connectionName", "connectionValue", ConnectionStringType.CUSTOM)
                .withStickyConnectionString("stickyName", "stickyValue", ConnectionStringType.CUSTOM)
                .withJavaVersion(JavaVersion.JAVA_1_7_0_51)
                .withWebContainer(WebContainer.TOMCAT_7_0_50)
                .create();
        Assert.assertNotNull(webApp);
        Assert.assertEquals(Region.US_WEST, webApp.region());

        // Create a deployment slot with empty config
        DeploymentSlot slot1 = webApp.deploymentSlots().define(SLOT_NAME_1)
                .withBrandNewConfiguration()
                .withPythonVersion(PythonVersion.PYTHON_27)
                .create();
        Assert.assertNotNull(slot1);
        Assert.assertNotEquals(JavaVersion.JAVA_1_7_0_51, slot1.javaVersion());
        Assert.assertEquals(PythonVersion.PYTHON_27, slot1.pythonVersion());
        Map<String, AppSetting> appSettingMap = slot1.appSettings();
        Assert.assertFalse(appSettingMap.containsKey("appkey"));
        Assert.assertFalse(appSettingMap.containsKey("stickykey"));
        Map<String, ConnectionString> connectionStringMap = slot1.connectionStrings();
        Assert.assertFalse(connectionStringMap.containsKey("connectionName"));
        Assert.assertFalse(connectionStringMap.containsKey("stickyName"));

        // Create a deployment slot with web app's config
        DeploymentSlot slot2 = webApp.deploymentSlots().define(SLOT_NAME_2)
                .withConfigurationFromParent()
                .create();
        Assert.assertNotNull(slot2);
        Assert.assertEquals(JavaVersion.JAVA_1_7_0_51, slot2.javaVersion());
        appSettingMap = slot2.appSettings();
        Assert.assertEquals("appvalue", appSettingMap.get("appkey").value());
        Assert.assertEquals(false, appSettingMap.get("appkey").sticky());
        Assert.assertEquals("stickyvalue", appSettingMap.get("stickykey").value());
        Assert.assertEquals(true, appSettingMap.get("stickykey").sticky());
        connectionStringMap = slot2.connectionStrings();
        Assert.assertEquals("connectionValue", connectionStringMap.get("connectionName").value());
        Assert.assertEquals(false, connectionStringMap.get("connectionName").sticky());
        Assert.assertEquals("stickyValue", connectionStringMap.get("stickyName").value());
        Assert.assertEquals(true, connectionStringMap.get("stickyName").sticky());

        // Update deployment slot
        slot2.update()
                .withoutJava()
                .withPythonVersion(PythonVersion.PYTHON_34)
                .withAppSetting("slot2key", "slot2value")
                .withStickyAppSetting("sticky2key", "sticky2value")
                .apply();
        Assert.assertNotNull(slot2);
        Assert.assertEquals(JavaVersion.OFF, slot2.javaVersion());
        Assert.assertEquals(PythonVersion.PYTHON_34, slot2.pythonVersion());
        appSettingMap = slot2.appSettings();
        Assert.assertEquals("slot2value", appSettingMap.get("slot2key").value());

        // Create 3rd deployment slot with configuration from slot 2
        DeploymentSlot slot3 = webApp.deploymentSlots().define(SLOT_NAME_3)
                .withConfigurationFromDeploymentSlot(slot2)
                .create();
        Assert.assertNotNull(slot3);
        Assert.assertEquals(JavaVersion.OFF, slot3.javaVersion());
        Assert.assertEquals(PythonVersion.PYTHON_34, slot3.pythonVersion());
        appSettingMap = slot3.appSettings();
        Assert.assertEquals("slot2value", appSettingMap.get("slot2key").value());

        // Get
        DeploymentSlot deploymentSlot = webApp.deploymentSlots().getByName(SLOT_NAME_3);
        Assert.assertEquals(slot3.id(), deploymentSlot.id());

        // List
        List<DeploymentSlot> deploymentSlots = webApp.deploymentSlots().list();
        Assert.assertEquals(3, deploymentSlots.size());

        // Swap
        slot3.swap(slot1.name());
        slot1 = webApp.deploymentSlots().getByName(SLOT_NAME_1);
        Assert.assertEquals(JavaVersion.OFF, slot1.javaVersion());
        Assert.assertEquals(PythonVersion.PYTHON_34, slot1.pythonVersion());
        Assert.assertEquals(PythonVersion.PYTHON_27, slot3.pythonVersion());
        Assert.assertEquals("appvalue", slot1.appSettings().get("appkey").value());
        Assert.assertEquals("slot2value", slot1.appSettings().get("slot2key").value());
        Assert.assertEquals("sticky2value", slot3.appSettings().get("sticky2key").value());
        Assert.assertEquals("stickyvalue", slot3.appSettings().get("stickykey").value());
    }
}