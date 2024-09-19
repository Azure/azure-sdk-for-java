// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.AuthenticationType;
import com.azure.resourcemanager.appservice.models.CsmDeploymentStatus;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.DeploymentBuildStatus;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionAppConfig;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlot;
import com.azure.resourcemanager.appservice.models.FunctionRuntimeStack;
import com.azure.resourcemanager.appservice.models.FunctionsDeployment;
import com.azure.resourcemanager.appservice.models.FunctionsDeploymentStorage;
import com.azure.resourcemanager.appservice.models.FunctionsDeploymentStorageAuthentication;
import com.azure.resourcemanager.appservice.models.FunctionsDeploymentStorageType;
import com.azure.resourcemanager.appservice.models.FunctionsRuntime;
import com.azure.resourcemanager.appservice.models.FunctionsScaleAndConcurrency;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import com.azure.resourcemanager.appservice.models.NameValuePair;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeName;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.SupportsOneDeploy;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;

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

    // test uses storage account key and connection string to configure the function app
    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void canDeployFlexConsumptionFunctionApp(boolean pushDeploy) throws FileNotFoundException {
        final PricingTier flexConsumptionTier = new PricingTier("FlexConsumption", "FC1");

        String appServiceName = generateRandomResourceName("plan", 10);
        String storageAccountName = generateRandomResourceName("sa", 10);
        String containerName = generateRandomResourceName("app-package-", 20);
        String functionAppName = generateRandomResourceName("functionapp", 20);

        AppServicePlan appServicePlan = appServiceManager.appServicePlans()
            .define(appServiceName)
            .withRegion(Region.US_WEST3)
            .withNewResourceGroup(rgName)
            .withPricingTier(flexConsumptionTier)
            .withOperatingSystem(OperatingSystem.LINUX)
            .withCapacity(1)
            .create();

        StorageAccount storageAccount = appServiceManager.storageManager().storageAccounts()
            .define(storageAccountName)
            .withRegion(Region.US_WEST3)
            .withExistingResourceGroup(rgName)
            .create();

        BlobContainer blobContainer = appServiceManager.storageManager().blobContainers()
            .defineContainer(containerName)
            .withExistingStorageAccount(storageAccount)
            .withPublicAccess(PublicAccess.NONE)
            .create();

        String connectionString = ResourceManagerUtils
            .getStorageConnectionString(storageAccountName, storageAccount.getKeys().get(0).value(), AzureEnvironment.AZURE);

        // create FunctionApp of Flex Consumption Plan
        SiteInner site = appServiceManager.webApps()
            .manager()
            .serviceClient()
            .getWebApps()
            .createOrUpdate(rgName, functionAppName, new SiteInner().withLocation(Region.US_WEST3.name())
                .withKind("functionapp,linux")
                .withHttpsOnly(true)
                .withServerFarmId(appServicePlan.id())
                .withSiteConfig(new SiteConfigInner().withAppSettings(Arrays.asList(
                    new NameValuePair()
                        .withName("AzureWebJobsStorage")
                        .withValue(connectionString),
                    new NameValuePair()
                        .withName("DEPLOYMENT_STORAGE_CONNECTION_STRING")
                        .withValue(connectionString))))
                .withFunctionAppConfig(new FunctionAppConfig()
                .withDeployment(new FunctionsDeployment().withStorage(
                    new FunctionsDeploymentStorage()
                        .withType(FunctionsDeploymentStorageType.BLOB_CONTAINER)
                        .withValue(storageAccount.endPoints().primary().blob() + containerName)
                        .withAuthentication(new FunctionsDeploymentStorageAuthentication()
                            .withType(AuthenticationType.STORAGE_ACCOUNT_CONNECTION_STRING)
                            .withStorageAccountConnectionStringName("DEPLOYMENT_STORAGE_CONNECTION_STRING"))))
                .withRuntime(new FunctionsRuntime().withName(RuntimeName.JAVA).withVersion("11"))
                .withScaleAndConcurrency(new FunctionsScaleAndConcurrency()
                    .withMaximumInstanceCount(100)
                    .withInstanceMemoryMB(2048))),
                com.azure.core.util.Context.NONE);

        FunctionApp functionApp = appServiceManager.functionApps().getByResourceGroup(rgName, functionAppName);

        // test one deploy
        File zipFile = new File(OneDeployTests.class.getResource("/java-functions.zip").getPath());
        if (!isPlaybackMode()) {
            if (pushDeploy) {
                KuduDeploymentResult deploymentResult = functionApp.pushDeploy(DeployType.ZIP, zipFile, null);

                String deploymentId = deploymentResult.deploymentId();
                Assertions.assertNotNull(deploymentId);

                // waitForRuntimeSuccess(functionApp, deploymentId);
                CsmDeploymentStatus deploymentStatus = functionApp.getDeploymentStatus(deploymentId);
                Assertions.assertNotNull(deploymentStatus.deploymentId());
            } else {
                functionApp.deploy(DeployType.ZIP, zipFile);
            }

            assertFunctionAppRunning(functionApp);
        }

        // Flex does not support slot
    }

    @DoNotRecord(skipInPlayback = true)
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void canDeployFunctionApp(boolean pushDeploy) {
        String functionAppName = generateRandomResourceName("functionapp", 20);

        FunctionApp functionApp =
            appServiceManager
                .functionApps()
                .define(functionAppName)
                .withRegion(Region.US_WEST2)
                .withNewResourceGroup(rgName)
                // zipDeploy does not work for LinuxConsumptionPlan
                // Use "WEBSITE_RUN_FROM_PACKAGE" in AppSettings for LinuxConsumptionPlan
                .withNewLinuxAppServicePlan(PricingTier.STANDARD_S1)
                .withBuiltInImage(FunctionRuntimeStack.JAVA_11)
                .withHttpsOnly(true)
                .create();

        File zipFile = new File(OneDeployTests.class.getResource("/java-functions.zip").getPath());
        // test deploy (currently it would call "zipDeploy", as one deploy is not supported for non-Flex consumption plan)
        if (!isPlaybackMode()) {
            if (pushDeploy) {
                KuduDeploymentResult deploymentResult = functionApp.pushDeploy(DeployType.ZIP, zipFile, null);

                String deploymentId = deploymentResult.deploymentId();
                Assertions.assertNotNull(deploymentId);

                // waitForRuntimeSuccess(functionApp, deploymentId);
                CsmDeploymentStatus deploymentStatus = functionApp.getDeploymentStatus(deploymentId);
                Assertions.assertNotNull(deploymentStatus.deploymentId());
            } else {
                functionApp.deploy(DeployType.ZIP, zipFile);
            }

            assertFunctionAppRunning(functionApp);
        }

        String slotName = generateRandomResourceName("slot", 10);
        FunctionDeploymentSlot slot = functionApp.deploymentSlots()
            .define(slotName)
            .withConfigurationFromParent()
            .create();

        if (!isPlaybackMode()) {
            if (pushDeploy) {
                KuduDeploymentResult deploymentResult = slot.pushDeploy(DeployType.ZIP, zipFile, null);

                String deploymentId = deploymentResult.deploymentId();
                Assertions.assertNotNull(deploymentId);

                // waitForRuntimeSuccess(functionApp, deploymentId);
                CsmDeploymentStatus deploymentStatus = functionApp.getDeploymentStatus(deploymentId);
                Assertions.assertNotNull(deploymentStatus.deploymentId());
            } else {
                slot.deploy(DeployType.ZIP, zipFile);
            }
        }
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

            // use "deploymentId" from response
            deploymentId = deploymentStatus.deploymentId();
        }
        Assertions.assertEquals(DeploymentBuildStatus.RUNTIME_SUCCESSFUL, buildStatus);
    }
}
