// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.AzureStorageQueueMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.messaging.storage.queue.implementation.support.converter.StorageQueueMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureStorageQueueMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueMessagingAutoConfiguration.class));

    @Test
    void withoutJacksonAutoConfigurationShouldUseDefaultConverter() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.enabled=true")
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasBean("defaultStorageQueueMessageConverter");
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
                assertThat(context).doesNotHaveBean("storageQueueMessageConverter");
            });
    }

    @Test
    void withIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.enabled=true")
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasBean("defaultStorageQueueMessageConverter");
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
                assertThat(context).doesNotHaveBean("storageQueueMessageConverter");
            });
    }

    @Test
    void withNonIsolatedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.enabled=true",
                "spring.cloud.azure.message-converter.isolated-object-mapper=false")
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasBean("storageQueueMessageConverter");
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
                assertThat(context).doesNotHaveBean("defaultStorageQueueMessageConverter");
            });
    }

    @Test
    void withUserProvidedObjectMapper() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.enabled=true",
                "spring.cloud.azure.message-converter.isolated-object-mapper=false")
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .withBean("userObjectMapper", JsonMapper.class, () -> JsonMapper.builder().build())
            .run(context -> {
                assertThat(context).hasBean("userObjectMapper");
                assertThat(context).hasSingleBean(ObjectMapper.class);
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
            });
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
