/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.Response;
import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebAppsWebDeployTests extends AppServiceTest {
    private String RG_NAME_1 = "";
    private String WEBAPP_NAME_1 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-webapp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_1);
    }

    @Test
    public void canDeployWarFile() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
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

        WebDeployment deployment = webApp1.deploy()
                .withPackageUri("https://github.com/Azure/azure-libraries-for-java/raw/master/azure-mgmt-appservice/src/test/resources/webapps.zip")
                .withExistingDeploymentsDeleted(true)
                .execute();

        Assertions.assertNotNull(deployment);
        if (!isPlaybackMode()) {
            SdkContext.sleep(10000);
            Response<String> response = curl("http://" + webApp1.defaultHostName() + "/helloworld");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains("Current time:"));
        }
    }
}