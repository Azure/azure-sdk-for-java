// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsMessageListener;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsProcessorClientConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsProcessorClientConfiguration.class));


    @Test
    void noMessageListenerAndErrorHandlerShouldNotConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProcessorClientConfiguration.class));
    }

    @Test
    void noMessageListenerShouldNotConfigure() {
        contextRunner
            .withBean(EventHubsErrorHandler.class, () -> errorContext -> { })
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProcessorClientConfiguration.class));
    }

    @Test
    void noErrorHandlerShouldNotConfigure() {
        contextRunner
            .withBean(EventHubsMessageListener.class, TestEventHubsRecordMessageListener::new)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProcessorClientConfiguration.class));
    }

    @Test
    void noEventHubNameProvidedShouldNotConfigure() {
        contextRunner
            .withBean(EventHubsErrorHandler.class, () -> errorContext -> { })
            .withBean(EventHubsMessageListener.class, TestEventHubsRecordMessageListener::new)
            .withPropertyValues("spring.cloud.azure.eventhubs.consumer-group=test-cg")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProcessorClientConfiguration.class));
    }

    @Test
    void noConsumerGroupProvidedShouldNotConfigure() {
        contextRunner
            .withBean(EventHubsErrorHandler.class, () -> errorContext -> { })
            .withBean(EventHubsMessageListener.class, TestEventHubsRecordMessageListener::new)
            .withPropertyValues("spring.cloud.azure.eventhubs.event-hub-name=test-eventhub")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProcessorClientConfiguration.class));
    }

    @Test
    void eventHubNameAndConsumerGroupProvidedShouldConfigure() {
        contextRunner
            .withBean(EventHubsErrorHandler.class, () -> errorContext -> { })
            .withBean(EventHubsMessageListener.class, TestEventHubsRecordMessageListener::new)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
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

    @Test
    void customizerShouldBeCalled() {
        EventProcessorBuilderCustomizer customizer = new EventProcessorBuilderCustomizer();
        this.contextRunner
            .withBean(EventHubsErrorHandler.class, () -> errorContext -> { })
            .withBean(EventHubsMessageListener.class, TestEventHubsRecordMessageListener::new)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.processor.consumer-group=test-consumer-group"
            )
            .withBean("customizer1", EventProcessorBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventProcessorBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        EventProcessorBuilderCustomizer customizer = new EventProcessorBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withBean(EventHubsErrorHandler.class, () -> errorContext -> { })
            .withBean(EventHubsMessageListener.class, TestEventHubsRecordMessageListener::new)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.processor.consumer-group=test-consumer-group"
            )
            .withBean("customizer1", EventProcessorBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventProcessorBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class EventProcessorBuilderCustomizer extends TestBuilderCustomizer<EventProcessorClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

    private static class TestEventHubsRecordMessageListener implements EventHubsRecordMessageListener {

        @Override
        public void onEvent(EventContext eventContext) {

        }
    }

}
