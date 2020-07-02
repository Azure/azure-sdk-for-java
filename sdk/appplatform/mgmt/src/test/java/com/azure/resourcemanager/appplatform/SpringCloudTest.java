// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.resourcemanager.appplatform.models.RuntimeVersion;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SpringCloudTest extends AppPlatformTest {

    SpringCloudTest() {
        super(RunCondition.LIVE_ONLY); // need storage data-plane
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
            .withSku("Basic")
            .withGitUri("https://github.com/Azure-Samples/piggymetrics-config")
            .create();

        Assertions.assertEquals("Basic", service.sku().tier());
        Assertions.assertEquals("https://github.com/Azure-Samples/piggymetrics-config", service.serverProperties().configServer().gitProperty().uri());

        service.update()
            .withSku("Standard")
            .withoutGitConfig()
            .apply();

        Assertions.assertEquals("Standard", service.sku().tier());
        Assertions.assertNull(service.serverProperties().configServer().gitProperty());

        File jarFile = new File("gateway.jar");
        if (!jarFile.exists()) {
            URLConnection connection = new URL("https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/gateway.jar").openConnection();
            connection.connect();
            IOUtils.copy(connection.getInputStream(), new FileOutputStream(jarFile));
        }

        SpringApp app = service.apps().define(appName)
            .withPublicEndpoint()
            .create();

        Assertions.assertNotNull(app.url());
        Assertions.assertNotNull(app.activeDeployment());

        HttpURLConnection connection = ((HttpURLConnection) new URL(app.url()).openConnection());
        connection.connect();
        Assertions.assertEquals(200, connection.getResponseCode());

        app.update()
            .deployJar(deploymentName, jarFile)
            .withoutDeployment(app.activeDeployment())
            .apply();

        Assertions.assertNotNull(app.url());
        Assertions.assertEquals(deploymentName, app.activeDeployment());

        connection = ((HttpURLConnection) new URL(app.url()).openConnection());
        connection.connect();
        Assertions.assertEquals(200, connection.getResponseCode());

        SpringAppDeployment deployment = app.deploy().getByName(app.activeDeployment());
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

        deployment = app.deploy().define(deploymentName1)
            .withSourceCodeFolder(new File(this.getClass().getResource("/piggymetrics").getFile()))
            .withTargetModule("gateway")
            .withCurrentActiveSetting()
            .create();
        app.refresh();

        Assertions.assertEquals(deploymentName1, app.activeDeployment());
        Assertions.assertEquals(2, deployment.settings().cpu());

        connection = ((HttpURLConnection) new URL(app.url()).openConnection());
        connection.connect();
        Assertions.assertEquals(200, connection.getResponseCode());

        app.update()
            .withoutPublicEndpoint()
            .apply();
        Assertions.assertFalse(app.isPublic());
    }
}
