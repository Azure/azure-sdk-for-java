// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.core.management.Region;
import com.azure.resourcemanager.appplatform.fluent.models.BuildServiceInner;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceGitProperty;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceGitRepository;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnterpriseTierTest extends AppPlatformTest {

    private static final String GIT_CONFIG_URI = "https://github.com/XiaofeiCao/spring-petclinic-microservices-config";

    @Test
    public void canCRUDService() {
        String serviceName = generateRandomResourceName("springsvc", 15);
        Region region = Region.US_EAST;
        // define spring cloud service for enterprise tier
        List<String> filePatterns = new ArrayList<>();
        filePatterns.add("api-gateway");
        SpringService springService = appPlatformManager.springServices()
            .define(serviceName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withEnterpriseTierSku()
            .withDefaultGitRepository(GIT_CONFIG_URI, "master", filePatterns)
            .withGitRepository("config1", GIT_CONFIG_URI, "master", filePatterns)
            .create();

        // tanzu components
        Assertions.assertEquals(springService.getDefaultConfigurationService().gitUri(), GIT_CONFIG_URI);
        Assertions.assertEquals(springService.getDefaultConfigurationService().filePatterns(), filePatterns);
        Assertions.assertNotNull(springService.getDefaultConfigurationService().getGitRepository("config1"));
        BuildServiceInner buildServiceInner = appPlatformManager.serviceClient().getBuildServices().getBuildService(rgName, serviceName, "default");
        Assertions.assertNotNull(buildServiceInner);

        springService.update()
            .withoutGitRepository("config1")
            .apply();

        // default is not cleared
        Assertions.assertNotNull(springService.getDefaultConfigurationService().gitUri());
        // config1 is cleared
        Assertions.assertNull(springService.getDefaultConfigurationService().getGitRepository("config1"));

        springService.update()
            .withGitRepositoryConfig(new ConfigurationServiceGitProperty()
                .withRepositories(Arrays.asList(new ConfigurationServiceGitRepository()
                    .withName("config2")
                    .withLabel("master")
                    .withPatterns(filePatterns)
                    .withUri(GIT_CONFIG_URI)
                )))
            .apply();

        // default is overridden
        Assertions.assertNull(springService.getDefaultConfigurationService().gitUri());
        Assertions.assertNotNull(springService.getDefaultConfigurationService().getGitRepository("config2"));

        springService.update()
            .withoutGitRepositories()
            .apply();

        // config2 is cleared
        Assertions.assertNull(springService.getDefaultConfigurationService().getGitRepository("config2"));

        // begin with no default configuration service, then create it
        String serviceName2 = generateRandomResourceName("springsvc", 15);
        SpringService springService2 = appPlatformManager.springServices()
            .define(serviceName2)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withEnterpriseTierSku()
            .create();
        Assertions.assertNull(springService2.getDefaultConfigurationService());

        springService2.update()
            .withGitRepository("config2", GIT_CONFIG_URI, "master", filePatterns)
            .apply();

        Assertions.assertNotNull(springService2.getDefaultConfigurationService());
        Assertions.assertNotNull(springService2.getDefaultConfigurationService().getGitRepository("config2"));
    }

    @Test
    public void canCRUDApp() {
        String serviceName = generateRandomResourceName("springsvc", 15);
        Region region = Region.US_EAST;
        // define spring cloud service for enterprise tier
        List<String> filePatterns = new ArrayList<>();
        filePatterns.add("api-gateway");
        SpringService springService = appPlatformManager.springServices()
            .define(serviceName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withEnterpriseTierSku()
            .withDefaultGitRepository(GIT_CONFIG_URI, "master", filePatterns)
            .create();

        String appName = generateRandomResourceName("springapp", 15);
        SpringApp app = springService.apps()
            .define(appName)
            .withDefaultActiveDeployment()
            .withConfigurationServiceBinding()
            .withServiceRegistryBinding()
            .withHttpsOnly()
            .withDefaultPublicEndpoint()
            .create();

        Assertions.assertNotNull(app.url());
        Assertions.assertTrue(app.isHttpsOnly());
        Assertions.assertTrue(app.isPublic());
        Assertions.assertTrue(app.hasConfigurationServiceBinding());
        Assertions.assertTrue(app.hasServiceRegistryBinding());
        Assertions.assertTrue(springService.getDefaultConfigurationService().getAppBindings().stream().anyMatch(SpringApp::hasConfigurationServiceBinding));
        Assertions.assertTrue(springService.getDefaultServiceRegistry().getAppBindings().stream().anyMatch(SpringApp::hasServiceRegistryBinding));

        app.update()
            .withoutHttpsOnly()
            .withoutConfigurationServiceBinding()
            .withoutServiceRegistryBinding()
            .withoutDefaultPublicEndpoint()
            .apply();

        Assertions.assertFalse(app.isHttpsOnly());
        Assertions.assertFalse(app.isPublic());
        Assertions.assertFalse(app.hasConfigurationServiceBinding());
        Assertions.assertFalse(app.hasServiceRegistryBinding());
        Assertions.assertFalse(springService.getDefaultConfigurationService().getAppBindings().stream().anyMatch(SpringApp::hasConfigurationServiceBinding));
        Assertions.assertFalse(springService.getDefaultServiceRegistry().getAppBindings().stream().anyMatch(SpringApp::hasServiceRegistryBinding));
    }
}
