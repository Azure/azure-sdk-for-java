// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class AzureEnvironmentConfigurationTest {

    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEnvironmentAutoConfiguration.class));

    @Test
    public void testAutoConfigure() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EnvironmentProvider.class);
            assertThat(context.getBean(EnvironmentProvider.class).getEnvironment()).isEqualTo(AzureEnvironment.AZURE);
        });
    }

    @Test
    public void testWithAnotherEnvironment() {
        this.contextRunner
            .withUserConfiguration(TestConfigurationWithProperty.class)
            .withPropertyValues(AZURE_PROPERTY_PREFIX + "environment=AzureChina")
            .run(context -> {
                assertThat(context).hasSingleBean(EnvironmentProvider.class);
                assertThat(context.getBean(EnvironmentProvider.class).getEnvironment())
                    .isEqualTo(AzureEnvironment.AZURE_CHINA);
            });
    }


    @Configuration
    @EnableConfigurationProperties(AzureProperties.class)
    static class TestConfigurationWithProperty {

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
