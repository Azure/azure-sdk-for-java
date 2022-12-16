// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureStorageQueueMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueMessagingAutoConfiguration.class, JacksonAutoConfiguration.class));

    @Test
    void withoutObjectMapperShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ObjectMapper.class))
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .run(context -> assertThatIllegalStateException());
    }

    @Test
    void withIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .run(context -> {
                assertNotNull(context.getBean("storageQueueMessageConverter"));
                assertThrows(NoSuchBeanDefinitionException.class,() -> context.getBean("nonIsolateStorageQueueMessageConverter"));
            });
    }

    @Test
    void withNonIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.enabled=true",
                "spring.cloud.azure.message-converter.isolated-object-mapper.enabled=false")
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .run(context -> {
                    assertNotNull(context.getBean("nonIsolateStorageQueueMessageConverter"));
                    assertThrows(NoSuchBeanDefinitionException.class,() -> context.getBean("storageQueueMessageConverter"));
                }
            );
    }

    @Configuration
    static class AzureStorageQueuePropertiesTestConfiguration {
        @Bean
        @ConfigurationProperties(AzureStorageQueueProperties.PREFIX)
        public AzureStorageQueueProperties azureStorageQueueProperties() {
            return new AzureStorageQueueProperties();
        }
    }

}