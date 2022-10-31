package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.messaging.storage.queue.support.converter.StorageQueueMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                "spring.cloud.azure.storage.queue.enabled=true"
            )
            .withUserConfiguration(AzureStorageQueuePropertiesTestConfiguration.class)
            .withBean(ObjectMapper.class)
            .withUserConfiguration(AzureStorageQueueMessagingAutoConfigurationTests.UserCustomizeObjectMapperConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(StorageQueueMessageConverter.class);
                ObjectMapper mapper = (ObjectMapper) context.getBean("UserObjectMapper");
                assertTrue(mapper instanceof ObjectMapper);
                assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(ObjectMapper.class));
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

    @Configuration
    static class UserCustomizeObjectMapperConfiguration {
        @Bean(name = "UserObjectMapper")
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

}
