// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.AbstractArmConnectionStringProvider;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.eventhubs.core.EventProcessorListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configures a {@link EventProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventProcessorClientBuilder.class)
@ConditionalOnBean({ EventProcessorListener.class, CheckpointStore.class })
@Conditional(AzureEventProcessorClientConfiguration.ProcessorAvailableCondition.class)
class AzureEventProcessorClientConfiguration {


    private final AzureEventHubProperties.Processor processorProperties;

    AzureEventProcessorClientConfiguration(AzureEventHubProperties eventHubProperties) {
        this.processorProperties = eventHubProperties.buildProcessorProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClient eventProcessorClient(EventProcessorClientBuilder builder) {
        return builder.buildEventProcessorClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClientBuilderFactory factory(CheckpointStore checkpointStore,
                                                      EventProcessorListener listener,
                                                      ObjectProvider<AbstractArmConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
        final EventProcessorClientBuilderFactory factory = new EventProcessorClientBuilderFactory(this.processorProperties,
                                                                                                  checkpointStore,
                                                                                                  listener);

        if (StringUtils.hasText(this.processorProperties.getConnectionString())) {
            factory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUB,
                                                                                     this.processorProperties.getConnectionString()));
        } else {
            factory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        }
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClientBuilder evenProcessorClientBuilder(EventProcessorClientBuilderFactory factory) {
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
