// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureContextAutoConfigurationTest {

    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureContextAutoConfiguration.class));

    @Test
    public void testAzureDisabled() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureProperties.class));
    }

    @Test
    public void testWithoutAzureClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(AzureResourceManager.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testLocationRequiredWhenAutoCreateResources() {
        this.contextRunner.withPropertyValues(AZURE_PROPERTY_PREFIX + "resourceGroup=group1")
            .withPropertyValues(AZURE_PROPERTY_PREFIX + "auto-create-resources=true")
            .run(context -> context.getBean(AzureProperties.class));
    }

    @Test
    public void testAzurePropertiesConfigured() {
        this.contextRunner
            .withPropertyValues(
                AZURE_PROPERTY_PREFIX + "client-id=client1",
                AZURE_PROPERTY_PREFIX + "client-secret=secret1",
                AZURE_PROPERTY_PREFIX + "tenant-id=tenant1",
                AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                AZURE_PROPERTY_PREFIX + "region=region1",
                AZURE_PROPERTY_PREFIX + "subscriptionId=sub1")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureProperties.class);
                assertThat(context.getBean(AzureProperties.class).getClientId()).isEqualTo("client1");
                assertThat(context.getBean(AzureProperties.class).getClientSecret()).isEqualTo("secret1");
                assertThat(context.getBean(AzureProperties.class).getTenantId()).isEqualTo("tenant1");
                assertThat(context.getBean(AzureProperties.class).getResourceGroup()).isEqualTo("rg1");
                assertThat(context.getBean(AzureProperties.class).getRegion()).isEqualTo("region1");
                assertThat(context.getBean(AzureProperties.class).getSubscriptionId()).isEqualTo("sub1");
                assertThat(context.getBean(AzureProperties.class).getEnvironment().getAzureEnvironment())
                    .isEqualTo(AzureEnvironment.AZURE);
            });
    }

    @Test
    public void testAutoConfigureEnabled() {
        this.contextRunner.withPropertyValues(AZURE_PROPERTY_PREFIX + "resource-group=rg1")
            .withUserConfiguration(TestConfigurationWithResourceManager.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureProperties.class);
                assertThat(context).hasSingleBean(AzureProfile.class);
            });
    }

    @Configuration
    static class TestConfigurationWithResourceManager {

        @Bean
        AzureResourceManager azureResourceManager() {
            return mock(AzureResourceManager.class);
        }

        @Bean
        CredentialsProvider credentialsProvider() {
            return mock(CredentialsProvider.class);
        }

    }
}
