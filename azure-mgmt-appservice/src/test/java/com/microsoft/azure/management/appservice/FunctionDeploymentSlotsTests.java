/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class FunctionDeploymentSlotsTests extends AppServiceTest {
    private static String RG_NAME_1 = "";
    private static String WEBAPP_NAME_1 = "";
    private static String SLOT_NAME_1 = "";
    private static String SLOT_NAME_2 = "";
    private static String SLOT_NAME_3 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-funcapp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);
        SLOT_NAME_1 = generateRandomResourceName("java-slot-", 20);
        SLOT_NAME_2 = generateRandomResourceName("java-slot-", 20);
        SLOT_NAME_3 = generateRandomResourceName("java-slot-", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Test
    public void canCRUDFunctionSwapSlots() throws Exception {
        // Create with consumption
        FunctionApp functionApp1 = appServiceManager.functionApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewAppServicePlan(PricingTier.STANDARD_S1)
                .withAppSetting("appkey", "appvalue")
                .withStickyAppSetting("stickykey", "stickyvalue")
                .withConnectionString("connectionName", "connectionValue", ConnectionStringType.CUSTOM)
                .withStickyConnectionString("stickyName", "stickyValue", ConnectionStringType.CUSTOM)
                .withJavaVersion(JavaVersion.JAVA_1_7_0_51)
                .withWebContainer(WebContainer.TOMCAT_7_0_50)
                .create();
        Assert.assertNotNull(functionApp1);
        Assert.assertEquals(Region.US_WEST, functionApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(functionApp1.appServicePlanId());
        Assert.assertNotNull(plan1);
        Assert.assertEquals(Region.US_WEST, plan1.region());

        // Create a deployment slot with empty config
        FunctionDeploymentSlot slot1 = functionApp1.deploymentSlots().define(SLOT_NAME_1)
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
        FunctionDeploymentSlot slot2 = functionApp1.deploymentSlots().define(SLOT_NAME_2)
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
        FunctionDeploymentSlot slot3 = functionApp1.deploymentSlots().define(SLOT_NAME_3)
                .withConfigurationFromDeploymentSlot(slot2)
                .create();
        Assert.assertNotNull(slot3);
        Assert.assertEquals(JavaVersion.OFF, slot3.javaVersion());
        Assert.assertEquals(PythonVersion.PYTHON_34, slot3.pythonVersion());
        appSettingMap = slot3.appSettings();
        Assert.assertEquals("slot2value", appSettingMap.get("slot2key").value());

        // Get
        FunctionDeploymentSlot deploymentSlot = functionApp1.deploymentSlots().getByName(SLOT_NAME_3);
        Assert.assertEquals(slot3.id(), deploymentSlot.id());

        // List
        List<FunctionDeploymentSlot> deploymentSlots = functionApp1.deploymentSlots().list();
        Assert.assertEquals(3, deploymentSlots.size());

        // Swap
        slot3.swap(slot1.name());
        slot1 = functionApp1.deploymentSlots().getByName(SLOT_NAME_1);
        Assert.assertEquals(JavaVersion.OFF, slot1.javaVersion());
        Assert.assertEquals(PythonVersion.PYTHON_34, slot1.pythonVersion());
        Assert.assertEquals(PythonVersion.PYTHON_27, slot3.pythonVersion());
        Assert.assertEquals("appvalue", slot1.appSettings().get("appkey").value());
        Assert.assertEquals("slot2value", slot1.appSettings().get("slot2key").value());
        Assert.assertEquals("sticky2value", slot3.appSettings().get("sticky2key").value());
        Assert.assertEquals("stickyvalue", slot3.appSettings().get("stickykey").value());
    }
}