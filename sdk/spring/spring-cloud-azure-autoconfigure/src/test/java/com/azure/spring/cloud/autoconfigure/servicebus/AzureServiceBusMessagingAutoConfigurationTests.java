// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.support.converter.ServiceBusMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Date;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureServiceBusMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusMessagingAutoConfiguration.class, JacksonAutoConfiguration.class));

    @Test
    void disableServiceBusShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.enabled=false",
                "spring.cloud.azure.servicebus.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutServiceBusTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutServiceBusConnectionShouldNotConfigure() {
        this.contextRunner
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void connectionInfoAndCheckpointStoreProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusProcessorFactory.class);
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                assertThat(context).hasSingleBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
            });
    }

    @Test
    void withoutObjectMapperShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ObjectMapper.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> assertThatIllegalStateException());
    }

    @Test
    void withObjectMapperShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> assertThat(context).hasSingleBean(ServiceBusMessageConverter.class));
    }

    @Test
    void withCustomizeObjectMapperShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.jackson.serialization.write-dates-as-timestamps=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                ObjectMapper mapper = context.getBean(ObjectMapper.class);
                assertTrue(mapper.writeValueAsString(new Date()).matches("^\\d+"));
            });
    }

    @Test
    void withMultipleObjectMapperWithPrimaryShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withUserConfiguration(UserCustomizeObjectMapperConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                ObjectMapper mapper = context.getBean(ObjectMapper.class);
                assertTrue(mapper.writeValueAsString(new Date()).matches("^\\d+"));
            });
    }

    @Test
    void withMultipleObjectMapperWithoutPrimaryShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
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
}
