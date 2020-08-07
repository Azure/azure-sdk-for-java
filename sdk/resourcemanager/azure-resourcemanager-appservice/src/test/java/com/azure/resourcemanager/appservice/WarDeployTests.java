// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WarDeployTests extends AppServiceTest {
    private String webappName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName = "JAVA" + generateRandomResourceName("webapp-", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void canDeployWar() throws Exception {
        if (!isPlaybackMode()) {
            // webApp.warDeploy method randomly fails in playback mode with error java.net.UnknownHostException,
            // Run this only in live mode ignore in playback until we find the root cause
            // https://api.travis-ci.org/v3/job/427936160/log.txt
            //
            // Create web app
            WebApp webApp =
                appServiceManager
                    .webApps()
                    .define(webappName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.STANDARD_S1)
                    .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                    .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
                    .create();
            Assertions.assertNotNull(webApp);

            webApp.warDeploy(new File(WarDeployTests.class.getResource("/helloworld.war").getPath()));

            if (!isPlaybackMode()) {
                Response<String> response = curl("http://" + webappName + "." + "azurewebsites.net");
                Assertions.assertEquals(200, response.getStatusCode());
                String body = response.getValue();
                Assertions.assertNotNull(body);
                Assertions.assertTrue(body.contains("Azure Samples Hello World"));
            }
        }
    }

    @Test
    public void canDeployMultipleWars() throws Exception {
        // Create web app
        WebApp webApp =
            appServiceManager
                .webApps()
                .define(webappName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
                .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
                .create();
        Assertions.assertNotNull(webApp);

        if (!isPlaybackMode()) {
            webApp.warDeploy(new File(WarDeployTests.class.getResource("/helloworld.war").getPath()));
            try (InputStream is = new FileInputStream(new File(WarDeployTests.class.getResource("/helloworld.war").getPath()))) {
                webApp.warDeploy(is, "app2");
            }

            Response<String> response = curl("http://" + webappName + "." + "azurewebsites.net");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Azure Samples Hello World"));

            response = curl("http://" + webappName + "." + "azurewebsites.net/app2/");
            Assertions.assertEquals(200, response.getStatusCode());
            body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Azure Samples Hello World"));
        }
    }
}
