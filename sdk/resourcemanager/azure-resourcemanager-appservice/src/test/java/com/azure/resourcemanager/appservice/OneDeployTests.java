// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

public class OneDeployTests extends AppServiceTest {

    private static final String GATEWAY_JAR_URL = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/gateway.jar";

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

    @SuppressWarnings("unchecked")
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
        File jarFile = new File("gateway.jar");
        if (!jarFile.exists()) {
            HttpURLConnection connection = (HttpURLConnection) new URL(GATEWAY_JAR_URL).openConnection();
            connection.connect();
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = new FileOutputStream(jarFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
            connection.disconnect();
        }
        KuduDeploymentResult deployResult =
            webApp1.pushDeploy(DeployType.JAR, jarFile, new DeployOptions().withTrackDeployment(true));

        Assertions.assertNotNull(deployResult.deploymentId());

        // poll deployment status
        String deploymentStatusUrl = AzureEnvironment.AZURE.getResourceManagerEndpoint()
            + "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/sites/{name}/deploymentStatus/{deploymentId}?api-version=2021-03-01";
        deploymentStatusUrl = deploymentStatusUrl
            .replace("{subscriptionId}", appServiceManager.subscriptionId())
            .replace("{resourceGroupName}", rgName)
            .replace("{name}", webAppName1)
            .replace("{deploymentId}", deployResult.deploymentId());

        // wait for RuntimeSuccessful
        String buildStatus = null;
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        while (!"RuntimeSuccessful".equals(buildStatus)) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(10));

            HttpRequest request = new HttpRequest(HttpMethod.GET, deploymentStatusUrl);
            HttpResponse response = appServiceManager.httpPipeline().send(request).block();
            Assertions.assertTrue(response.getStatusCode() / 100 == 2);

            String body = response.getBodyAsString().block();
            Assertions.assertNotNull(body);
            Map<String, Object> bodyJson = serializerAdapter.deserialize(body,
                new TypeReference<Map<String, Object>>() {
                }.getJavaType(),
                SerializerEncoding.JSON);
            Assertions.assertNotNull(bodyJson);
            if (bodyJson.containsKey("properties")) {
                Map<String, Object> propertiesJson = (Map<String, Object>) bodyJson.get("properties");
                if (propertiesJson.containsKey("status")) {
                    buildStatus = (String) propertiesJson.get("status");
                }
            }

            if (buildStatus != null && buildStatus.contains("Failed")) {
                // failed
                break;
            }
        }
        Assertions.assertEquals("RuntimeSuccessful", buildStatus);
    }
}
