// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appservice.models.AppSetting;
import com.azure.resourcemanager.appservice.models.ConnectionString;
import com.azure.resourcemanager.appservice.models.ConnectionStringType;
import com.azure.resourcemanager.appservice.models.FtpsState;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.NetFrameworkVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.PythonVersion;
import com.azure.resourcemanager.appservice.models.SupportedTlsVersions;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.core.management.Region;
import java.util.Map;

import com.azure.core.management.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebAppConfigTests extends AppServiceTest {
    private String webappName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName = generateRandomResourceName("java-webapp-", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void canCRUDWebAppConfig() throws Exception {
        // Create with new app service plan
        appServiceManager
            .webApps()
            .define(webappName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withNewWindowsPlan(PricingTier.BASIC_B1)
            .withNetFrameworkVersion(NetFrameworkVersion.V3_0)
            .withMinTlsVersion(SupportedTlsVersions.ONE_ONE)
            .create();

        WebApp webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertNotNull(webApp);
        Assertions.assertEquals(Region.US_EAST, webApp.region());
        Assertions.assertEquals(NetFrameworkVersion.V3_0, webApp.netFrameworkVersion());
        Assertions.assertEquals(SupportedTlsVersions.ONE_ONE, webApp.minTlsVersion());

        // Java version
        webApp.update().withJavaVersion(JavaVersion.JAVA_1_7_0_51).withWebContainer(WebContainer.TOMCAT_7_0_50).apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertEquals(JavaVersion.JAVA_1_7_0_51, webApp.javaVersion());

        // Python version
        webApp.update().withPythonVersion(PythonVersion.PYTHON_34).apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertEquals(PythonVersion.PYTHON_34, webApp.pythonVersion());

        // Default documents
        int documentSize = webApp.defaultDocuments().size();
        webApp.update().withDefaultDocument("somedocument.html").apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertEquals(documentSize + 1, webApp.defaultDocuments().size());
        Assertions.assertTrue(webApp.defaultDocuments().contains("somedocument.html"));

        // App settings
        webApp.update().withAppSetting("appkey", "appvalue").withStickyAppSetting("stickykey", "stickyvalue").apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Map<String, AppSetting> appSettingMap = webApp.getAppSettings();
        Assertions.assertEquals("appvalue", appSettingMap.get("appkey").value());
        Assertions.assertEquals(false, appSettingMap.get("appkey").sticky());
        Assertions.assertEquals("stickyvalue", appSettingMap.get("stickykey").value());
        Assertions.assertEquals(true, appSettingMap.get("stickykey").sticky());

        // Connection strings
        webApp
            .update()
            .withConnectionString("connectionName", "connectionValue", ConnectionStringType.CUSTOM)
            .withStickyConnectionString("stickyName", "stickyValue", ConnectionStringType.CUSTOM)
            .apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Map<String, ConnectionString> connectionStringMap = webApp.getConnectionStrings();
        Assertions.assertEquals("connectionValue", connectionStringMap.get("connectionName").value());
        Assertions.assertEquals(false, connectionStringMap.get("connectionName").sticky());
        Assertions.assertEquals("stickyValue", connectionStringMap.get("stickyName").value());
        Assertions.assertEquals(true, connectionStringMap.get("stickyName").sticky());

        // HTTPS only
        webApp = webApp.update().withHttpsOnly(true).apply();
        Assertions.assertTrue(webApp.httpsOnly());

        // FTPS
        webApp = webApp.update().withFtpsState(FtpsState.FTPS_ONLY).apply();
        Assertions.assertEquals(FtpsState.FTPS_ONLY, webApp.ftpsState());

        // Min TLS version
        webApp = webApp.update().withMinTlsVersion(SupportedTlsVersions.ONE_TWO).apply();
        Assertions.assertEquals(SupportedTlsVersions.ONE_TWO, webApp.minTlsVersion());

        // Logs
        webApp = webApp.update().withContainerLoggingEnabled().apply();
        Assertions.assertTrue(webApp.diagnosticLogsConfig().innerModel().httpLogs().fileSystem().enabled());
        // verify on new instance
        // https://github.com/Azure/azure-libraries-for-java/issues/759
        webApp = appServiceManager.webApps().getById(webApp.id());
        Assertions.assertTrue(webApp.diagnosticLogsConfig().innerModel().httpLogs().fileSystem().enabled());
    }

    @Test
    public void canCRUDWebAppConfigJava11() throws Exception {
        // Create with new app service plan
        appServiceManager
            .webApps()
            .define(webappName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withNewWindowsPlan(PricingTier.BASIC_B1)
            .withNetFrameworkVersion(NetFrameworkVersion.V3_0)
            .create();

        WebApp webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertNotNull(webApp);
        Assertions.assertEquals(Region.US_EAST, webApp.region());
        Assertions.assertEquals(NetFrameworkVersion.V3_0, webApp.netFrameworkVersion());

        // Java version
        webApp.update().withJavaVersion(JavaVersion.JAVA_11).withWebContainer(WebContainer.TOMCAT_9_0_NEWEST).apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertEquals(JavaVersion.JAVA_11, webApp.javaVersion());

        // Python version
        webApp.update().withPythonVersion(PythonVersion.PYTHON_34).apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertEquals(PythonVersion.PYTHON_34, webApp.pythonVersion());

        // Default documents
        int documentSize = webApp.defaultDocuments().size();
        webApp.update().withDefaultDocument("somedocument.html").apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Assertions.assertEquals(documentSize + 1, webApp.defaultDocuments().size());
        Assertions.assertTrue(webApp.defaultDocuments().contains("somedocument.html"));

        // App settings
        webApp.update().withAppSetting("appkey", "appvalue").withStickyAppSetting("stickykey", "stickyvalue").apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Map<String, AppSetting> appSettingMap = webApp.getAppSettings();
        Assertions.assertEquals("appvalue", appSettingMap.get("appkey").value());
        Assertions.assertEquals(false, appSettingMap.get("appkey").sticky());
        Assertions.assertEquals("stickyvalue", appSettingMap.get("stickykey").value());
        Assertions.assertEquals(true, appSettingMap.get("stickykey").sticky());

        // Connection strings
        webApp
            .update()
            .withConnectionString("connectionName", "connectionValue", ConnectionStringType.CUSTOM)
            .withStickyConnectionString("stickyName", "stickyValue", ConnectionStringType.CUSTOM)
            .apply();
        webApp = appServiceManager.webApps().getByResourceGroup(rgName, webappName);
        Map<String, ConnectionString> connectionStringMap = webApp.getConnectionStrings();
        Assertions.assertEquals("connectionValue", connectionStringMap.get("connectionName").value());
        Assertions.assertEquals(false, connectionStringMap.get("connectionName").sticky());
        Assertions.assertEquals("stickyValue", connectionStringMap.get("stickyName").value());
        Assertions.assertEquals(true, connectionStringMap.get("stickyName").sticky());

        // HTTPS only
        webApp = webApp.update().withHttpsOnly(true).apply();
        Assertions.assertTrue(webApp.httpsOnly());

        // FTPS
        webApp = webApp.update().withFtpsState(FtpsState.FTPS_ONLY).apply();
        Assertions.assertEquals(FtpsState.FTPS_ONLY, webApp.ftpsState());
    }
}
