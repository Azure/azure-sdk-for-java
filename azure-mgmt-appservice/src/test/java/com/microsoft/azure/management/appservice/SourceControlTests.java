/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SourceControlTests extends AppServiceTestBase {
    private static final String RG_NAME = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String WEBAPP_NAME = ResourceNamer.randomResourceName("java-webapp-", 20);
    private static final String SLOT_NAME = ResourceNamer.randomResourceName("java-slot-", 20);
    private static final String APP_SERVICE_PLAN_NAME = ResourceNamer.randomResourceName("java-asp-", 20);
    private static OkHttpClient httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canDeploySourceControl() throws Exception {
        // Create web app
        WebApp webApp = appServiceManager.webApps().define(WEBAPP_NAME)
                .withNewResourceGroup(RG_NAME)
                .withNewAppServicePlan(APP_SERVICE_PLAN_NAME)
                .withRegion(Region.US_WEST)
                .withPricingTier(AppServicePricingTier.BASIC_B1)
                .defineSourceControl()
                    .withPublicExternalRepository()
                    .withGit("https://github.com/Azure-Samples/app-service-web-nodejs-get-started")
                    .withBranch("master")
                    .attach()
                .withConnectionString("StorageConnectionString",
                        "DefaultEndpointsProtocol=https;AccountName=azurejsdkdemo;AccountKey=Brtk1ZZIiSdPAYATXJwwg8xyeWkyqUmJf0m1dH5caQZJSqKpPksUhHXeW9vo+fSg9BE7I+7dYFEWkZy+VQmCcw==",
                        ConnectionStringType.CUSTOM)
                .create();
        Assert.assertNotNull(webApp);
        Response response = curl("http://" + WEBAPP_NAME + "." + "azurewebsites.net");
        Assert.assertEquals(200, response.code());
        String body = response.body().string();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.contains("Hello world from linux 4"));


        // auto swap
        DeploymentSlot slot = webApp.deploymentSlots().define(SLOT_NAME)
                .withBrandNewConfiguration()
                .withPythonVersion(PythonVersion.PYTHON_27)
                .create();
        Assert.assertNotNull(slot);
        Assert.assertNotEquals(JavaVersion.JAVA_1_7_0_51, slot.javaVersion());
        Assert.assertEquals(PythonVersion.PYTHON_27, slot.pythonVersion());
        Map<String, AppSetting> appSettingMap = slot.appSettings();
        Assert.assertFalse(appSettingMap.containsKey("appkey"));
        Assert.assertFalse(appSettingMap.containsKey("stickykey"));
        Map<String, ConnectionString> connectionStringMap = slot.connectionStrings();
        Assert.assertFalse(connectionStringMap.containsKey("connectionName"));
        Assert.assertFalse(connectionStringMap.containsKey("stickyName"));


    }

    private static Response curl(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return httpClient.newCall(request).execute();
    }
}