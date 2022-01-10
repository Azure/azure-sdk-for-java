// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.Map;

public class OneDeployTests extends AppServiceTest {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canDeployZip() throws Exception {
        String webAppName1 = generateRandomResourceName("webapp", 10);
        WebApp webApp1 = createWebApp(webAppName1);

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
    public void canPushDeployZip() throws Exception {
        String webAppName1 = generateRandomResourceName("webapp", 10);
        WebApp webApp1 = createWebApp(webAppName1);

        File zipFile = new File(OneDeployTests.class.getResource("/webapps.zip").getPath());
        KuduDeploymentResult deployResult =
            webApp1.pushDeploy(DeployType.ZIP, zipFile, new DeployOptions().withTrackDeployment(true));

        Assertions.assertNotNull(deployResult.deploymentId());

        String deploymentStatusUrl = AzureEnvironment.AZURE.getResourceManagerEndpoint()
            + "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/sites/{name}/deploymentStatus/{deploymentId}";
        deploymentStatusUrl = deploymentStatusUrl
            .replace("{subscriptionId}", Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID))
            .replace("{resourceGroupName}", rgName)
            .replace("{name}", webAppName1)
            .replace("{deploymentId}", deployResult.deploymentId());

        // wait for RUNTIME_SUCCESSFUL
        String buildStatus = null;
        SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        while (!"RuntimeSuccessful".equals(buildStatus)) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));

            HttpRequest request = new HttpRequest(HttpMethod.GET, deploymentStatusUrl);
            HttpResponse response = appServiceManager.httpPipeline().send(request).block();
            Assertions.assertEquals(200, response.getStatusCode());

            String body = response.getBodyAsString().block();
            Assertions.assertNotNull(body);
            Map<String, Object> bodyJson = serializerAdapter.deserialize(body,
                new TypeReference<Map<String, Object>>() {}.getJavaType(),
                SerializerEncoding.JSON);
            Assertions.assertNotNull(bodyJson);
            if (bodyJson.containsKey("properties")) {
                Map<String, Object> propertiesJson = (Map<String, Object>) bodyJson.get("properties");
                if (propertiesJson.containsKey("status")) {
                    buildStatus = (String) propertiesJson.get("status");
                }
            }
        }

        String response = curl("https://" + webAppName1 + ".azurewebsites.net/" + "helloworld/").getValue();
        Assertions.assertTrue(response.contains("Hello"));
    }

    private WebApp createWebApp(String webAppName1) {
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
        return webApp1;
    }
}
