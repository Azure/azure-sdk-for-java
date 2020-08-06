// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureContextAutoConfigurationTest {
    private ApplicationContextRunner contextRunner =
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AzureContextAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzurePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
            .withPropertyValues("spring.cloud.azure.resourceGroup=group1")
            .withPropertyValues("spring.cloud.azure.region=westUS").run(context -> {
                assertThat(context).hasSingleBean(AzureProperties.class);
                assertThat(context.getBean(AzureProperties.class).getCredentialFilePath()).isEqualTo("credential");
                assertThat(context.getBean(AzureProperties.class).getResourceGroup()).isEqualTo("group1");
                assertThat(context.getBean(AzureProperties.class).getRegion()).isEqualTo("westUS");
                assertThat(context.getBean(AzureProperties.class).getEnvironment()).isEqualTo(AzureEnvironment.AZURE);
            });
    }

    @Test
    public void testRequiredAzureProperties() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
            .withPropertyValues("spring.cloud.azure.resourceGroup=group1").run(context -> {
                assertThat(context).hasSingleBean(AzureProperties.class);
                assertThat(context.getBean(AzureProperties.class).getCredentialFilePath()).isEqualTo("credential");
                assertThat(context.getBean(AzureProperties.class).getResourceGroup()).isEqualTo("group1");
            });
    }

    @Test
    public void testAzureDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureProperties.class));
    }

    @Test
    public void testWithoutAzureClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(Azure.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testLocationRequiredWhenAutoCreateResources() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credentialFilePath=credential")
            .withPropertyValues("spring.cloud.azure.resourceGroup=group1")
            .withPropertyValues("spring.cloud.azure.auto-create-resources=true")
            .run(context -> context.getBean(AzureProperties.class));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        Azure azure() {
            return mock(Azure.class);
        }

        @Bean
        CredentialsProvider credentialsProvider() {
            return mock(CredentialsProvider.class);
        }

        @Bean
        ResourceManagerProvider resourceManagerProvider() {
            return mock(ResourceManagerProvider.class);
        }

        @Bean
        AzureTokenCredentials credentials() {
            return mock(AzureTokenCredentials.class);
        }
    }
}
