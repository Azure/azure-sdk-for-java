// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.appservice.models.WebDeployment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class WebAppsWebDeployTests extends AppServiceTest {
    private String rgName1 = "";
    private String webappName1 = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName1 = generateRandomResourceName("java-webapp-", 20);
        rgName1 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName1);
    }

    @Test
    public void canDeployWarFile() throws Exception {
        // Create with new app service plan
        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
                .withNewWindowsPlan(PricingTier.BASIC_B1)
                .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());

        WebDeployment deployment =
            webApp1
                .deploy()
                .withPackageUri(
                    "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/resourcemanager/azure-resourcemanager-appservice/src/test/resources/webapps.zip")
                .withExistingDeploymentsDeleted(true)
                .execute();

        Assertions.assertNotNull(deployment);
        if (!isPlaybackMode()) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(10));
            Response<String> response = curl("http://" + webApp1.defaultHostname() + "/helloworld/");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Current time:"));
        }
    }
}
