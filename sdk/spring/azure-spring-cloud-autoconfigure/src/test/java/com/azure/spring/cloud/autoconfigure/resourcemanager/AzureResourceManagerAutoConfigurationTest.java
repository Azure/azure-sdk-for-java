// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureResourceManagerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureResourceManagerAutoConfiguration.class));

    @Test
    void testAzureResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.resource-manager.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureResourceManager.class);
                assertThat(context).doesNotHaveBean(AzureProfile.class);
            });
    }

    @Test
    void configureWithoutTenantId() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.resource-manager.enabled=true")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureResourceManager.class);
                assertThat(context).doesNotHaveBean(AzureProfile.class);
            });
    }

    @Test
    void configureWithTenantId() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.profile.tenant-id=test-tenant")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureResourceManager.class);
                assertThat(context).doesNotHaveBean(AzureProfile.class);
            });
    }
    
    

    @Test
    void testWithoutAzureResourceManagerClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(AzureResourceManager.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureProfile.class));
    }

    @Test
    void testWithoutAzureResourceMetadataClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(AzureResourceMetadata.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureProfile.class));
    }


    /*@Test
    void testAzurePropertiesConfigured() {
        this.contextRunner
            .withPropertyValues(
                AZURE_PROPERTY_PREFIX + ".credential.client-id=client1",
                AZURE_PROPERTY_PREFIX + ".credential.client-secret=secret1",
                AZURE_PROPERTY_PREFIX + ".profile.tenant-id=tenant1")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureResourceManagerProperties.class);
                assertThat(context.getBean(AzureResourceManagerProperties.class).getClientId()).isEqualTo("client1");
                assertThat(context.getBean(AzureResourceManagerProperties.class).getClientSecret()).isEqualTo("secret1");
                assertThat(context.getBean(AzureResourceManagerProperties.class).getTenantId()).isEqualTo("tenant1");
                assertThat(context.getBean(AzureResourceManagerProperties.class).getResourceGroup()).isEqualTo("rg1");
                assertThat(context.getBean(AzureResourceManagerProperties.class).getRegion()).isEqualTo("region1");
                assertThat(context.getBean(AzureResourceManagerProperties.class).getSubscriptionId()).isEqualTo("sub1");
            });
    }

    @Test
    void testAutoConfigureEnabled() {
        this.contextRunner.withPropertyValues(AZURE_PROPERTY_PREFIX + "resource-group=rg1")
                          .withUserConfiguration(TestConfigurationWithResourceManager.class)
                          .run(context -> {
                              assertThat(context).hasSingleBean(AzureResourceManagerProperties.class);
                              assertThat(context).hasSingleBean(AzureProfile.class);
                          });
    }

    @Configuration
    static class TestConfigurationWithResourceManager {

        @Bean
        AzureResourceManager azureResourceManager() {
            return mock(AzureResourceManager.class);
        }

    }*/
}
