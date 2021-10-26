// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

public class OneDeployTests extends AppServiceTest {

    @Test
    @DoNotRecord
    public void canDeployZip() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        String webAppName1 = generateRandomResourceName("webapp", 10);

        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webAppName1)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.BASIC_B1)
                .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                .withWebContainer(WebContainer.TOMCAT_8_5_NEWEST)
                .withHttpsOnly(true)
                .create();

        File zipFile = new File(OneDeployTests.class.getResource("/webapps.zip").getPath());
        webApp1.deploy(DeployType.ZIP, zipFile);

        // wait a bit
        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        String response = curl("https://" + webAppName1 + ".azurewebsites.net/" + "helloworld/").getValue();
        Assertions.assertTrue(response.contains("Hello"));
    }
}
