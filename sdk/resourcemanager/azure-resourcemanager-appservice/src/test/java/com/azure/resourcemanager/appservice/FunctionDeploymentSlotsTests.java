// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.AppSetting;
import com.azure.resourcemanager.appservice.models.ConnectionString;
import com.azure.resourcemanager.appservice.models.ConnectionStringType;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlot;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.PythonVersion;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import java.util.Map;

import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FunctionDeploymentSlotsTests extends AppServiceTest {
    private String webappName1 = "";
    private String slotName1 = "";
    private String slotName2 = "";
    private String slotName3 = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName1 = generateRandomResourceName("java-funcapp-", 20);
        rgName = generateRandomResourceName("javacsmrg", 20);
        slotName1 = generateRandomResourceName("java-slot-", 20);
        slotName2 = generateRandomResourceName("java-slot-", 20);
        slotName3 = generateRandomResourceName("java-slot-", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void canCRUDFunctionSwapSlots() throws Exception {
        // Create with consumption
        FunctionApp functionApp1 =
            appServiceManager
                .functionApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withNewAppServicePlan(PricingTier.STANDARD_S1)
                .withAppSetting("appkey", "appvalue")
                .withStickyAppSetting("stickykey", "stickyvalue")
                .withConnectionString("connectionName", "connectionValue", ConnectionStringType.CUSTOM)
                .withStickyConnectionString("stickyName", "stickyValue", ConnectionStringType.CUSTOM)
                .withPythonVersion(PythonVersion.PYTHON_27)
                .create();
        Assertions.assertNotNull(functionApp1);
        Assertions.assertEquals(Region.US_WEST, functionApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(functionApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());

        /*
        IMPORTANT, function app cannot create slot with empty config.
        "site.withSiteConfig(new SiteConfigInner())" sets the empty config.
        However service will response with "Function App app settings are not expected to be empty. Please try again with WEBSITE_CONTENTAZUREFILECONNECTIONSTRING as a storage account connection string.: Function App app settings are not expected to be empty. Please try again with WEBSITE_CONTENTAZUREFILECONNECTIONSTRING as a storage account connection string.", if this is done to function app.
         */

        // Create a deployment slot with empty config
        FunctionDeploymentSlot slot1 =
            functionApp1
                .deploymentSlots()
                .define(slotName1)
                .withBrandNewConfiguration()
                .withPythonVersion(PythonVersion.PYTHON_34)
                .create();
        Assertions.assertNotNull(slot1);
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot1.pythonVersion());
//        Map<String, AppSetting> appSettingMap = slot1.getAppSettings();
//        Assertions.assertFalse(appSettingMap.containsKey("appkey"));
//        Assertions.assertFalse(appSettingMap.containsKey("stickykey"));
//        Map<String, ConnectionString> connectionStringMap = slot1.getConnectionStrings();
//        Assertions.assertFalse(connectionStringMap.containsKey("connectionName"));
//        Assertions.assertFalse(connectionStringMap.containsKey("stickyName"));

        // Create a deployment slot with web app's config
        FunctionDeploymentSlot slot2 =
            functionApp1.deploymentSlots().define(slotName2).withConfigurationFromParent().create();
        Assertions.assertNotNull(slot2);
        Assertions.assertEquals(PythonVersion.PYTHON_27, slot2.pythonVersion());
        Map<String, AppSetting> appSettingMap = slot2.getAppSettings();
        Assertions.assertEquals("appvalue", appSettingMap.get("appkey").value());
        Assertions.assertEquals(false, appSettingMap.get("appkey").sticky());
        Assertions.assertEquals("stickyvalue", appSettingMap.get("stickykey").value());
        Assertions.assertEquals(true, appSettingMap.get("stickykey").sticky());
        Map<String, ConnectionString> connectionStringMap = slot2.getConnectionStrings();
        Assertions.assertEquals("connectionValue", connectionStringMap.get("connectionName").value());
        Assertions.assertEquals(false, connectionStringMap.get("connectionName").sticky());
        Assertions.assertEquals("stickyValue", connectionStringMap.get("stickyName").value());
        Assertions.assertEquals(true, connectionStringMap.get("stickyName").sticky());

        // Update deployment slot
        slot2
            .update()
            .withPythonVersion(PythonVersion.PYTHON_34)
            .withAppSetting("slot2key", "slot2value")
            .withStickyAppSetting("sticky2key", "sticky2value")
            .apply();
        Assertions.assertNotNull(slot2);
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot2.pythonVersion());
        appSettingMap = slot2.getAppSettings();
        Assertions.assertEquals("slot2value", appSettingMap.get("slot2key").value());

        // Create 3rd deployment slot with configuration from slot 2
        FunctionDeploymentSlot slot3 =
            functionApp1.deploymentSlots().define(slotName3).withConfigurationFromDeploymentSlot(slot2).create();
        Assertions.assertNotNull(slot3);
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot3.pythonVersion());
        appSettingMap = slot3.getAppSettings();
        Assertions.assertEquals("slot2value", appSettingMap.get("slot2key").value());

        slot3
            .update()
            .withPythonVersion(PythonVersion.PYTHON_27)
            .apply();

        // Get
        FunctionDeploymentSlot deploymentSlot = functionApp1.deploymentSlots().getByName(slotName3);
        Assertions.assertEquals(slot3.id(), deploymentSlot.id());

        // List
        PagedIterable<FunctionDeploymentSlot> deploymentSlots = functionApp1.deploymentSlots().list();
        Assertions.assertEquals(3, TestUtilities.getSize(deploymentSlots));

        // Swap
        slot3.swap(slot1.name());
        slot1 = functionApp1.deploymentSlots().getByName(slotName1);
        Assertions.assertEquals(PythonVersion.PYTHON_27, slot1.pythonVersion());
        Assertions.assertEquals(PythonVersion.PYTHON_34, slot3.pythonVersion());
        Map<String, AppSetting> slot1AppSettings = slot1.getAppSettings();
        Map<String, AppSetting> slot3AppSettings = slot3.getAppSettings();
        Assertions.assertEquals("appvalue", slot1AppSettings.get("appkey").value());
        Assertions.assertEquals("slot2value", slot1AppSettings.get("slot2key").value());
        Assertions.assertEquals("sticky2value", slot3AppSettings.get("sticky2key").value());
        Assertions.assertEquals("stickyvalue", slot3AppSettings.get("stickykey").value());
    }
}
