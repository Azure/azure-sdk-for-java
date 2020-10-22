// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.appplatform.models.ConfigServerProperties;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class SpringCloudTest extends AppPlatformTest {
    private static final String PIGGYMETRICS_CONFIG_URL = "https://github.com/Azure-Samples/piggymetrics-config";

    @Test
    public void canCRUDServie() {
        String serviceName = generateRandomResourceName("springsvc", 15);
        Region region = Region.US_EAST;

        Assertions.assertTrue(appPlatformManager.springServices().checkNameAvailability(serviceName, region).nameAvailable());

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withSku("B0")
            .withGitUri(PIGGYMETRICS_CONFIG_URL)
            .create();

        Assertions.assertEquals("B0", service.sku().name());
        Assertions.assertEquals(PIGGYMETRICS_CONFIG_URL, service.getServerProperties().configServer().gitProperty().uri());

        service.update()
            .withSku("S0", 2)
            .withoutGitConfig()
            .apply();

        Assertions.assertEquals("S0", service.sku().name());

        ConfigServerProperties serverProperties = service.getServerProperties();
        Assertions.assertTrue(serverProperties == null
            || serverProperties.configServer() == null
            || serverProperties.configServer().gitProperty() == null
            || serverProperties.configServer().gitProperty().uri() == null
            || serverProperties.configServer().gitProperty().uri().isEmpty());

        Assertions.assertEquals(1, appPlatformManager.springServices().list().stream().filter(s -> s.name().equals(serviceName)).count());

        appPlatformManager.springServices().deleteById(service.id());
        Assertions.assertEquals(404,
            appPlatformManager.springServices().getByIdAsync(service.id()).map(o -> 200)
                .onErrorResume(e ->
                    Mono.just(e instanceof ManagementException ? ((ManagementException) e).getResponse().getStatusCode() : 400))
                .block());
    }

    @Test
    public void canCRUDApp() throws Exception {
        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = "gateway";
        Region region = Region.US_EAST;

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        SpringApp app = service.apps().define(appName)
            .withDefaultActiveDeployment()
            .withDefaultPublicEndpoint()
            .withHttpsOnly()
            .withPersistentDisk(10, "/per")
            .withTemporaryDisk(4, "/tmp")
            .create();

        Assertions.assertNotNull(app.url());
        Assertions.assertNotNull(app.activeDeploymentName());
        Assertions.assertTrue(app.isPublic());
        Assertions.assertTrue(app.isHttpsOnly());
        Assertions.assertEquals("/per", app.persistentDisk().mountPath());
        Assertions.assertEquals(10, app.persistentDisk().sizeInGB());
        Assertions.assertEquals("/tmp", app.temporaryDisk().mountPath());
        Assertions.assertEquals(4, app.temporaryDisk().sizeInGB());

        if (!isPlaybackMode()) {
            Assertions.assertTrue(requestSuccess(app.url()));
        }

        app.update()
            .withoutDefaultPublicEndpoint()
            .withoutHttpsOnly()
            .apply();

        Assertions.assertFalse(app.isPublic());
        Assertions.assertFalse(app.isHttpsOnly());

        Assertions.assertEquals(1, service.apps().list().stream().filter(s -> s.name().equals(appName)).count());

        service.apps().deleteById(app.id());
        Assertions.assertEquals(404,
            service.apps().getByIdAsync(app.id()).map(o -> 200)
                .onErrorResume(
                    e -> Mono.just((e instanceof ManagementException) ? ((ManagementException) e).getResponse().getStatusCode() : 400)
                ).block());
    }
}
