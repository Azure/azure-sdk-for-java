// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.eventhubs.core.EventProcessorListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureEventProcessorClientConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventProcessorClientConfiguration.class));

    @Test
    void noEventHubNameProvidedShouldNotConfigure() {
        contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.consumer-group=test-cg")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventProcessorClientConfiguration.class));
    }

    @Test
    void noConsumerGroupProvidedShouldNotConfigure() {
        contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.event-hub-name=test-eventhub")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventProcessorClientConfiguration.class));
    }

    @Test
    void eventHubNameAndConsumerGroupProvidedShouldConfigure() {
        contextRunner
            .withBean(EventProcessorListener.class, TestEventProcessorListener::new)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.processor.consumer-group=test-consumer-group"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(EventProcessorClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(EventProcessorClient.class);
            });
    }

    private static class TestEventProcessorListener implements EventProcessorListener {

        @Override
        public void onError(ErrorContext errorContext) {

        }

        @Override
        public void onEvent(EventContext eventContext) {

        }

        @Override
        public void onEventBatch(EventBatchContext eventBatchContext) {

        }

        @Override
        public void onPartitionClose(CloseContext closeContext) {

        }

        @Override
        public void onInitialization(InitializationContext initializationContext) {

        }
    }
    
}
