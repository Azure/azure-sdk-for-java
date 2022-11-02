// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.messaging.storage.queue.support.converter.StorageQueueMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureStorageQueueMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueMessagingAutoConfiguration.class));

    @Test
    void withoutObjectMapperShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ObjectMapper.class))
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .run(context -> assertThat(context).doesNotHaveBean(StorageQueueMessageConverter.class));
    }

    @Test
    void withObjectMapperShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .withBean(ObjectMapper.class)
            .run(context -> assertThat(context).hasSingleBean(StorageQueueMessageConverter.class));
    }

    @Test
    void withCustomizeObjectMapperShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true",
                "spring.jackson.serialization.write-dates-as-timestamps=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .withBean(JacksonAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
                ObjectMapper mapper = context.getBean(ObjectMapper.class);
                assertTrue(mapper.writeValueAsString(new Date()).matches("^\\d+") );
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
