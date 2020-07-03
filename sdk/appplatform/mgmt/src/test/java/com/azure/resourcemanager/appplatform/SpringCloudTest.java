// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.resourcemanager.appplatform.models.RuntimeVersion;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpringCloudTest extends AppPlatformTest {

    SpringCloudTest() {
        super(RunCondition.LIVE_ONLY); // need storage data-plane and url check
    }

    @Test
    public void canCRUDSpringAppWithDeployment() throws IOException {
        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = "gateway";
        String deploymentName = generateRandomResourceName("deploy", 15);
        String deploymentName1 = generateRandomResourceName("deploy", 15);

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withSku("B0")
            .create();

        Assertions.assertEquals("B0", service.sku().name());

        service.update()
            .withSku("S0")
            .apply();

        Assertions.assertEquals("S0", service.sku().name());

        service.update()
            .withGitUri("https://github.com/Azure-Samples/piggymetrics-config")
            .apply();
        Assertions.assertEquals("https://github.com/Azure-Samples/piggymetrics-config", service.serverProperties().configServer().gitProperty().uri());

        File jarFile = new File("gateway.jar");
        if (!jarFile.exists()) {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/gateway.jar").openConnection();
            connection.connect();
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = new FileOutputStream(jarFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
            connection.disconnect();
        }

        SpringApp app = service.apps().define(appName)
            .withPublicEndpoint()
            .create();

        Assertions.assertNotNull(app.url());
        Assertions.assertNotNull(app.activeDeployment());

        Assertions.assertTrue(requestSuccess(app.url()));

        app.update()
            .withoutDeployment(app.activeDeployment())
            .deployJar(deploymentName, jarFile)
            .apply();

        Assertions.assertNotNull(app.url());
        Assertions.assertEquals(deploymentName, app.activeDeployment());
        Assertions.assertEquals(1, app.deployments().list().stream().count());

        Assertions.assertTrue(requestSuccess(app.url()));

        SpringAppDeployment deployment = app.deployments().getByName(app.activeDeployment());
        deployment.update()
            .withCpu(2)
            .withMemory(4)
            .withRuntime(RuntimeVersion.JAVA_11)
            .withInstance(2)
            .apply();

        Assertions.assertEquals(2, deployment.settings().cpu());
        Assertions.assertEquals(4, deployment.settings().memoryInGB());
        Assertions.assertEquals(RuntimeVersion.JAVA_11, deployment.settings().runtimeVersion());
        Assertions.assertEquals(2, deployment.instances().size());

        deployment = app.deployments().define(deploymentName1)
            .withSourceCodeFolder(new File(this.getClass().getResource("/piggymetrics").getFile()))
            .withTargetModule("gateway")
            .withSettingsFromActiveDeployment()
            .activate()
            .create();
        app.refresh();

        Assertions.assertEquals(deploymentName1, app.activeDeployment());
        Assertions.assertEquals(2, deployment.settings().cpu());
        Assertions.assertNotNull(deployment.getLogFileUrl());

        Assertions.assertTrue(requestSuccess(app.url()));

        app.update()
            .withoutPublicEndpoint()
            .apply();
        Assertions.assertFalse(app.isPublic());
    }

    private boolean requestSuccess(String url) throws IOException {
        for (int i = 0; i < 60; ++i) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            try {
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    return true;
                }
                System.out.printf("Do request to %s with response code %d%n", url, connection.getResponseCode());
            } finally {
                connection.getInputStream().close();
                connection.disconnect();
                SdkContext.sleep(5000);
            }
        }
        return false;
    }
}
