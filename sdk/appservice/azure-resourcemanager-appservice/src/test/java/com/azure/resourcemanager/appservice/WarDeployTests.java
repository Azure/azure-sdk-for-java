// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.core.management.Region;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WarDeployTests extends AppServiceTest {
    private String webappName = "";

    private final File warFile = new File(WarDeployTests.class.getResource("/helloworld.war").getPath());

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName = "JAVA" + generateRandomResourceName("webapp-", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void canDeployWar() throws Exception {
        // Create web app
        WebApp webApp = appServiceManager.webApps()
            .define(webappName)
            .withRegion(Region.US_WEST3)
            .withNewResourceGroup(rgName)
            .withNewWindowsPlan(PricingTier.PREMIUM_P1V3)
            .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
            .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
            .create();
        Assertions.assertNotNull(webApp);

        if (!isPlaybackMode()) {
            webApp.deploy(DeployType.WAR, warFile, new DeployOptions().withPath("webapps/ROOT"));
            ResourceManagerUtils.sleep(Duration.ofSeconds(60));

            Response<String> response = curl("https://" + webappName + "." + "azurewebsites.net");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Azure Samples Hello World"));
        }
    }

    @Test
    public void canDeployMultipleWars() throws Exception {
        // Create web app
        WebApp webApp = appServiceManager.webApps()
            .define(webappName)
            .withRegion(Region.US_WEST3)
            .withNewResourceGroup(rgName)
            .withNewWindowsPlan(PricingTier.PREMIUM_P1V3)
            .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
            .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
            .create();
        Assertions.assertNotNull(webApp);

        if (!isPlaybackMode()) {
            webApp.deploy(DeployType.WAR, warFile, new DeployOptions().withPath("webapps/ROOT"));
            try (InputStream is = new FileInputStream(warFile)) {
                webApp.deploy(DeployType.WAR, is, warFile.length(), new DeployOptions().withPath("webapps/app2"));
            }

            ResourceManagerUtils.sleep(Duration.ofSeconds(60));

            Response<String> response = curl("https://" + webappName + "." + "azurewebsites.net");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Azure Samples Hello World"));

            response = curl("https://" + webappName + "." + "azurewebsites.net/app2/");
            Assertions.assertEquals(200, response.getStatusCode());
            body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Azure Samples Hello World"));
        }
    }
}
