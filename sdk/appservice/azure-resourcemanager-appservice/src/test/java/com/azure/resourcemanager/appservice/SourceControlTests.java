// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class SourceControlTests extends AppServiceTest {
    private String webappName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName = generateRandomResourceName("java-webapp-", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void canDeploySourceControl() throws Exception {
        // Create web app
        WebApp webApp = appServiceManager.webApps()
            .define(webappName)
            .withRegion(Region.US_WEST3)
            .withNewResourceGroup(rgName)
            .withNewWindowsPlan(PricingTier.STANDARD_S1)
            .defineSourceControl()
            .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test")
            .withBranch("master")
            .attach()
            .create();
        Assertions.assertNotNull(webApp);
        if (!isPlaybackMode()) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(60));
            Response<String> response = curl("https://" + webappName + "." + "azurewebsites.net");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Hello world from linux 4"));
        }
    }
}
