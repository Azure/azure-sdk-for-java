// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import com.azure.spring.messaging.eventhubs.support.converter.EventHubsMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Date;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureEventHubsMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsMessagingAutoConfiguration.class));

    @Test
    void disableEventHubsShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.enabled=false",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutEventHubsTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubsTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutEventHubConnectionShouldNotConfigure() {
        this.contextRunner
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutCheckpointStoreShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(ObjectMapper.class)
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void connectionInfoAndCheckpointStoreProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .withBean(ObjectMapper.class)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsProcessorFactory.class);
                assertThat(context).hasSingleBean(AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
            });
    }

    @Test
    void withoutObjectMapperShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ObjectMapper.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .run(context -> assertThat(context).doesNotHaveBean(EventHubsMessageConverter.class));
    }

    @Test
    void withObjectMapperShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(ObjectMapper.class)
            .run(context -> assertThat(context).hasSingleBean(EventHubsMessageConverter.class));
    }

    @Test
    void withCustomizeObjectMapperShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.jackson.serialization.write-dates-as-timestamps=true"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(JacksonAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsMessageConverter.class);
                ObjectMapper mapper = context.getBean(ObjectMapper.class);
                assertTrue(mapper.writeValueAsString(new Date()).matches("^\\d+") );
            });
    }

}
