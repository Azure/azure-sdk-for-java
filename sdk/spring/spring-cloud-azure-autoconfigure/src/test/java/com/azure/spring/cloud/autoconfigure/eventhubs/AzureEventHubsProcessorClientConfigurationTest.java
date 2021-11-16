// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.service.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsProcessorClientConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsProcessorClientConfiguration.class));

    @Test
    void noEventHubNameProvidedShouldNotConfigure() {
        contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.consumer-group=test-cg")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProcessorClientConfiguration.class));
    }

    @Test
    void noConsumerGroupProvidedShouldNotConfigure() {
        contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.event-hub-name=test-eventhub")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProcessorClientConfiguration.class));
    }

    @Test
    void eventHubNameAndConsumerGroupProvidedShouldConfigure() {
        contextRunner
            .withBean(EventProcessingListener.class, TestEventProcessorListener::new)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.processor.consumer-group=test-consumer-group"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(EventProcessorClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(EventProcessorClient.class);
            });
    }

    private static class TestEventProcessorListener implements RecordEventProcessingListener {

        @Override
        public void onEvent(EventContext eventContext) {

        }
    }
    
}
