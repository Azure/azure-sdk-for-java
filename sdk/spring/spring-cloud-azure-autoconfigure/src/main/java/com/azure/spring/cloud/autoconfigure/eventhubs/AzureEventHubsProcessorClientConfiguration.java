// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.AzureSpringIdentifier;
import com.azure.spring.cloud.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.service.eventhubs.consumer.EventHubsMessageListener;
import com.azure.spring.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a {@link EventProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventProcessorClientBuilder.class)
@ConditionalOnBean({ EventHubsMessageListener.class, CheckpointStore.class, EventHubsErrorHandler.class })
@Conditional(AzureEventHubsProcessorClientConfiguration.ProcessorAvailableCondition.class)
class AzureEventHubsProcessorClientConfiguration {


    private final AzureEventHubsProperties.Processor processorProperties;

    AzureEventHubsProcessorClientConfiguration(AzureEventHubsProperties eventHubsProperties) {
        this.processorProperties = eventHubsProperties.buildProcessorProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClient eventProcessorClient(EventProcessorClientBuilder builder) {
        return builder.buildEventProcessorClient();
    }

    @Bean
    @ConditionalOnMissingBean
    EventProcessorClientBuilderFactory eventProcessorClientBuilderFactory(
        CheckpointStore checkpointStore,
        EventHubsMessageListener messageListener,
        EventHubsErrorHandler errorHandler,
        ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHubs>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> customizers) {
        final EventProcessorClientBuilderFactory factory =
            new EventProcessorClientBuilderFactory(this.processorProperties, checkpointStore, messageListener, errorHandler);

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
