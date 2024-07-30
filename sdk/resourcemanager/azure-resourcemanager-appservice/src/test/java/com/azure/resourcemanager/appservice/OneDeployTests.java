// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.appservice.models.CsmDeploymentStatus;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.DeploymentBuildStatus;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.SupportsOneDeploy;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

public class OneDeployTests extends AppServiceTest {

    private static final String HELLOWORLD_JAR_URL = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/helloworld.jar";

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canDeployZip() {
        String webAppName1 = generateRandomResourceName("webapp", 10);

        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webAppName1)
                .withRegion(Region.US_WEST3)
                .withNewResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
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

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canPushDeployJar() throws Exception {
        String webAppName1 = generateRandomResourceName("webapp", 10);

        WebApp webApp1 =
            appServiceManager
                .webApps()
                .define(webAppName1)
                .withRegion(Region.US_WEST3)
                .withNewResourceGroup(rgName)
                .withNewLinuxPlan(PricingTier.STANDARD_S1)
                .withBuiltInImage(RuntimeStack.JAVA_11_JAVA11)
                .withHttpsOnly(true)
                .create();

        // deploy
        File jarFile = new File("helloworld.jar");
        if (!jarFile.exists()) {
            HttpURLConnection connection = (HttpURLConnection) new URL(HELLOWORLD_JAR_URL).openConnection();
            connection.connect();
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = new FileOutputStream(jarFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
            connection.disconnect();
        }
        KuduDeploymentResult deployResult =
            webApp1.pushDeploy(DeployType.JAR, jarFile, new DeployOptions().withTrackDeployment(true));

        String deploymentId = deployResult.deploymentId();
        Assertions.assertNotNull(deploymentId);

        // stream logs
        webApp1.streamApplicationLogsAsync().subscribeOn(Schedulers.single()).subscribe(LOGGER::verbose);

        waitForRuntimeSuccess(webApp1, deploymentId);

        // deploy another slot
        String slotName = generateRandomResourceName("slot", 10);
        DeploymentSlot slot2 = webApp1.deploymentSlots()
            .define(slotName)
            .withConfigurationFromParent()
            .create();

        KuduDeploymentResult slotDeployResult =
            slot2.pushDeploy(DeployType.JAR, jarFile, new DeployOptions().withTrackDeployment(true));

        String slotDeploymentId = slotDeployResult.deploymentId();
        Assertions.assertNotNull(slotDeploymentId);

        waitForRuntimeSuccess(slot2, slotDeploymentId);
    }

    private void waitForRuntimeSuccess(SupportsOneDeploy webapp, String deploymentId) {
        DeploymentBuildStatus buildStatus = null;
        while (!DeploymentBuildStatus.RUNTIME_SUCCESSFUL.equals(buildStatus)) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(10));

            CsmDeploymentStatus deploymentStatus = webapp.getDeploymentStatus(deploymentId);
            Assertions.assertNotNull(deploymentStatus);

            buildStatus = deploymentStatus.status();
            Assertions.assertNotNull(buildStatus);

            if (buildStatus.toString().contains("Failed")) {
                // failed
                break;
            }
        }
        Assertions.assertEquals(DeploymentBuildStatus.RUNTIME_SUCCESSFUL, buildStatus);
    }
}
