/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.RestClient;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

import java.util.Map;

public class FunctionDeploymentSlotsTests extends AppServiceTest {
    private String RG_NAME_1 = "";
    private String WEBAPP_NAME_1 = "";
    private String SLOT_NAME_1 = "";
    private String SLOT_NAME_2 = "";
    private String SLOT_NAME_3 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-funcapp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);
        SLOT_NAME_1 = generateRandomResourceName("java-slot-", 20);
        SLOT_NAME_2 = generateRandomResourceName("java-slot-", 20);
        SLOT_NAME_3 = generateRandomResourceName("java-slot-", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Disabled("Contains connection string in request payload")
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
        Assertions.assertNotNull(functionApp1);
        Assertions.assertEquals(Region.US_WEST, functionApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(functionApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());

        // Create a deployment slot with empty config
        FunctionDeploymentSlot slot1 = functionApp1.deploymentSlots().define(SLOT_NAME_1)
                .withBrandNewConfiguration()
                .withPythonVersion(PythonVersion.PYTHON_27)
                .create();
        Assertions.assertNotNull(slot1);
        Assertions.assertNotEquals(JavaVersion.JAVA_1_7_0_51, slot1.javaVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_27, slot1.pythonVersion());
        Map<String, AppSetting> appSettingMap = slot1.getAppSettings();
        Assertions.assertFalse(appSettingMap.containsKey("appkey"));
        Assertions.assertFalse(appSettingMap.containsKey("stickykey"));
        Map<String, ConnectionString> connectionStringMap = slot1.getConnectionStrings();
        Assertions.assertFalse(connectionStringMap.containsKey("connectionName"));
        Assertions.assertFalse(connectionStringMap.containsKey("stickyName"));

        // Create a deployment slot with web app's config
        FunctionDeploymentSlot slot2 = functionApp1.deploymentSlots().define(SLOT_NAME_2)
                .withConfigurationFromParent()
                .create();
        Assertions.assertNotNull(slot2);
        Assertions.assertEquals(JavaVersion.JAVA_1_7_0_51, slot2.javaVersion());
        appSettingMap = slot2.getAppSettings();
        Assertions.assertEquals("appvalue", appSettingMap.get("appkey").value());
        Assertions.assertEquals(false, appSettingMap.get("appkey").sticky());
        Assertions.assertEquals("stickyvalue", appSettingMap.get("stickykey").value());
        Assertions.assertEquals(true, appSettingMap.get("stickykey").sticky());
        connectionStringMap = slot2.getConnectionStrings();
        Assertions.assertEquals("connectionValue", connectionStringMap.get("connectionName").value());
        Assertions.assertEquals(false, connectionStringMap.get("connectionName").sticky());
        Assertions.assertEquals("stickyValue", connectionStringMap.get("stickyName").value());
        Assertions.assertEquals(true, connectionStringMap.get("stickyName").sticky());

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
        appSettingMap = slot2.getAppSettings();
        Assertions.assertEquals("slot2value", appSettingMap.get("slot2key").value());

        // Create 3rd deployment slot with configuration from slot 2
        FunctionDeploymentSlot slot3 = functionApp1.deploymentSlots().define(SLOT_NAME_3)
                .withConfigurationFromDeploymentSlot(slot2)
                .create();
        Assertions.assertNotNull(slot3);
        Assertions.assertEquals(JavaVersion.OFF, slot3.javaVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot3.pythonVersion());
        appSettingMap = slot3.getAppSettings();
        Assertions.assertEquals("slot2value", appSettingMap.get("slot2key").value());

        // Get
        FunctionDeploymentSlot deploymentSlot = functionApp1.deploymentSlots().getByName(SLOT_NAME_3);
        Assertions.assertEquals(slot3.id(), deploymentSlot.id());

        // List
        PagedIterable<FunctionDeploymentSlot> deploymentSlots = functionApp1.deploymentSlots().list();
        Assertions.assertEquals(3, TestUtilities.getSize(deploymentSlots));

        // Swap
        slot3.swap(slot1.name());
        slot1 = functionApp1.deploymentSlots().getByName(SLOT_NAME_1);
        Assertions.assertEquals(JavaVersion.OFF, slot1.javaVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot1.pythonVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_27, slot3.pythonVersion());
        Map<String, AppSetting> slot1AppSettings = slot1.getAppSettings();
        Map<String, AppSetting> slot3AppSettings = slot3.getAppSettings();
        Assertions.assertEquals("appvalue", slot1AppSettings.get("appkey").value());
        Assertions.assertEquals("slot2value", slot1AppSettings.get("slot2key").value());
        Assertions.assertEquals("sticky2value", slot3AppSettings.get("sticky2key").value());
        Assertions.assertEquals("stickyvalue", slot3AppSettings.get("stickykey").value());
    }
}