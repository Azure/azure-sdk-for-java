// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.resourcemanager.appplatform.models.RuntimeVersion;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

import java.io.File;

public class SpringCloudTest extends AppPlatformTest {
    public void test() {
        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = generateRandomResourceName("app", 15);
        String deploymentName = generateRandomResourceName("deploy", 15);
        String deploymentName1 = generateRandomResourceName("deploy", 15);

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withGitUri("https://github.com/Azure-Samples/piggymetrics-config")
            .create();

        SpringApp app = service.apps().define(appName)
            .withPublicEndpoint()
            .withHttpsOnly()
            .deployJar(deploymentName, new File("<jar-path>").toPath())
            .create();

        Assertions.assertTrue(app.isPublic());
        Assertions.assertTrue(app.isHttpsOnly());

        app.deploy().define(deploymentName1)
            .withSourceCodePath(new File("<source-code>").toPath())
            .withTargetModule("test-module")
            .withActivation()
            .withCustomSetting()
            .withCpu(2)
            .withMemory(4)
            .withRuntime(RuntimeVersion.JAVA_11)
            .withEnvironment("automation", "javasdk")
            .create();

        app.refresh();

        Assertions.assertEquals(deploymentName1, app.activeDeployment());
    }
}
