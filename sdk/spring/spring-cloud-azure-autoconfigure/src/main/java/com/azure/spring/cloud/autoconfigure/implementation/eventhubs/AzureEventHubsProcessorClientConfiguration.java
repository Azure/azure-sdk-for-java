// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsBatchMessageListener;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.listener.MessageListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Configures a {@link EventProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventProcessorClientBuilder.class)
@ConditionalOnBean({ MessageListener.class, CheckpointStore.class, EventHubsErrorHandler.class })
@Conditional(AzureEventHubsProcessorClientConfiguration.ProcessorAvailableCondition.class)
class AzureEventHubsProcessorClientConfiguration {


    private final AzureEventHubsProperties.Processor processorProperties;

    AzureEventHubsProcessorClientConfiguration(AzureEventHubsProperties eventHubsProperties) {
        this.processorProperties = eventHubsProperties.buildProcessorProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    EventProcessorClient eventProcessorClient(EventProcessorClientBuilder builder) {
        return builder.buildEventProcessorClient();
    }

    @Bean
    @ConditionalOnMissingBean
    EventProcessorClientBuilderFactory eventProcessorClientBuilderFactory(
        CheckpointStore checkpointStore,
        EventHubsErrorHandler errorHandler,
        ObjectProvider<EventHubsRecordMessageListener> recordMessageListeners,
        ObjectProvider<EventHubsBatchMessageListener> batchMessageListeners,
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.EventHubs>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> customizers) {

        MessageListener<?> listener = getMessageListener(recordMessageListeners, batchMessageListeners);
        Assert.notNull(listener, "Expect only one record / batch message listener for Event Hubs.");

        final EventProcessorClientBuilderFactory factory =
            new EventProcessorClientBuilderFactory(this.processorProperties, checkpointStore, listener, errorHandler);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    EventProcessorClientBuilder eventProcessorClientBuilder(EventProcessorClientBuilderFactory factory) {
        return factory.build();
    }

    private MessageListener<?> getMessageListener(ObjectProvider<EventHubsRecordMessageListener> recordListeners,
                                                  ObjectProvider<EventHubsBatchMessageListener> batchListeners) {

        boolean isRecordListenerPresent = recordListeners.stream().findAny().isPresent();
        boolean isBatchListenerPresent = batchListeners.stream().findAny().isPresent();
        if (isRecordListenerPresent && isBatchListenerPresent) {
            throw new IllegalArgumentException("Only one type of Event Hubs message listener can be provided, either a "
                + "'EventHubsRecordMessageListener'' or a 'EventHubsBatchMessageListener', but found both.");
        }
        if (!isRecordListenerPresent && !isBatchListenerPresent) {
            throw new IllegalArgumentException("One listener of type 'EventHubsRecordMessageListener' or "
                + "'EventHubsBatchMessageListener' must be provided.");
        }
        if (isRecordListenerPresent) {
            return recordListeners.getIfUnique();
        }

        return batchListeners.getIfUnique();
    }

    static class ProcessorAvailableCondition extends AllNestedConditions {

        ProcessorAvailableCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnAnyProperty(
            prefix = "spring.cloud.azure.eventhubs",
            name = { "event-hub-name", "processor.event-hub-name" })
        static class EventHubName {
            EventHubName() {
            }
        }

        @ConditionalOnProperty(
            prefix = "spring.cloud.azure.eventhubs.processor",
            name = "consumer-group")
        static class ConsumerGroup {
            ConsumerGroup() {
            }
        }

        @ConditionalOnAnyProperty(
            prefix = "spring.cloud.azure.eventhubs",
            name = { "namespace", "connection-string", "processor.namespace", "processor.connection-string" })
        static class ConnectionInfo {

        }
    }
}
