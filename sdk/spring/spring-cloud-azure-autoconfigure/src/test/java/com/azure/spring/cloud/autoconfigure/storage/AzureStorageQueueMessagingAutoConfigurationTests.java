// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.messaging.storage.queue.support.converter.StorageQueueMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void withObjectMapperShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
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
            .run(context -> {
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
                ObjectMapper mapper = context.getBean(ObjectMapper.class);
                assertTrue(mapper.writeValueAsString(new Date()).matches("^\\d+"));
            });
    }

    @Test
    void withMultipleObjectMapperWithPrimaryShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .withUserConfiguration(UserCustomizeObjectMapperConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
                ObjectMapper mapper = context.getBean(ObjectMapper.class);
                assertTrue(mapper.writeValueAsString(new Date()).matches("^\\d+"));
            });
    }

    @Test
    void withMultipleObjectMapperWithoutPrimaryShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .withUserConfiguration(UserCustomizeObjectMapperConfiguration1.class)
            .run(context -> {
                assertThrows(NoUniqueBeanDefinitionException.class, () -> context.getBean(ObjectMapper.class));
            });
    }

    @Configuration
    static class UserCustomizeObjectMapperConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

        @Bean
        @Primary
        ObjectMapper objectMapper1() {
            return new ObjectMapper();
        }
    }

    @Configuration
    static class UserCustomizeObjectMapperConfiguration1 {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

        @Bean
        ObjectMapper objectMapper1() {
            return new ObjectMapper();
        }
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
