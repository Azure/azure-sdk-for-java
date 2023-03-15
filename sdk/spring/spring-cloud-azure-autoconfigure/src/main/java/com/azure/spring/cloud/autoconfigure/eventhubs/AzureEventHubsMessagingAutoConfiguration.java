// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsProducerFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.messaging.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.messaging.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.implementation.converter.ObjectMapperHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyAzureCommonProperties;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Messaging Azure Event Hubs support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventHubsTemplate.class)
@AutoConfigureAfter(AzureEventHubsAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = {"connection-string", "namespace"})
@ConditionalOnBean(AzureEventHubsProperties.class)
@Import({
    AzureEventHubsMessagingAutoConfiguration.EventHubsTemplateConfiguration.class,
    AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class
})
public class AzureEventHubsMessagingAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEventHubsMessagingAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    NamespaceProperties eventHubsNamespaceProperties(AzureEventHubsProperties properties,
                                                      ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.EventHubs>> connectionStringProviders) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        BeanUtils.copyProperties(properties, namespaceProperties);
        copyAzureCommonProperties(properties, namespaceProperties);
        if (namespaceProperties.getConnectionString() == null) {
            ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider =
                connectionStringProviders.getIfAvailable();
            if (connectionStringProvider != null) {
                namespaceProperties.setConnectionString(connectionStringProvider.getConnectionString());
                LOGGER.info("Event Hubs connection string is set from {} now.", connectionStringProvider.getClass().getName());
            }
        }
        return namespaceProperties;
    }

    /**
     * Configure the {@link EventHubsProcessorFactory}
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(CheckpointStore.class)
    public static class ProcessorContainerConfiguration {

        /**
         * Creates the default Event Hubs namespace processor factory.
         *
         * @param properties Event Hubs namespace related properties.
         * @param checkpointStore Checkpoint store for storing and retrieving partition ownership information and
         * checkpoint details for each partition.
         * @param suppliers Object provider suppliers.
         * @return A default Event Hubs namespace processor factory.
         */
        @Bean
        @ConditionalOnMissingBean
        public EventHubsProcessorFactory defaultEventHubsNamespaceProcessorFactory(
            NamespaceProperties properties, CheckpointStore checkpointStore,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>> suppliers) {
            return new DefaultEventHubsNamespaceProcessorFactory(checkpointStore, properties,
                suppliers.getIfAvailable());
        }

    }

    /**
     * Configure the {@link EventHubsTemplate}
     */
    @Configuration(proxyBeanMethods = false)
    public static class EventHubsTemplateConfiguration {

        /**
         * Creates a default Event Hubs namespace producer factory.
         *
         * @param properties Event Hubs namespace related properties.
         * @param suppliers Object provider suppliers.
         * @return A default Event Hubs namespace producer factory.
         */
        @Bean
        @ConditionalOnMissingBean
        public EventHubsProducerFactory defaultEventHubsNamespaceProducerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
            return new DefaultEventHubsNamespaceProducerFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "true", matchIfMissing = true)
        EventHubsMessageConverter defaultEventHubsMessageConverter() {
            return new EventHubsMessageConverter(ObjectMapperHolder.OBJECT_MAPPER);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "false")
        EventHubsMessageConverter eventHubsMessageConverter(ObjectMapper objectMapper) {
            return new EventHubsMessageConverter(objectMapper);
        }

        /**
         * Creates an Event Hubs template.
         *
         * @param producerFactory An Event Hubs producer factory.
         * @param messageConverter An Event Hubs message converter.
         * @return An Event Hubs template.
         */
        @Bean
        @ConditionalOnMissingBean
        public EventHubsTemplate eventHubsTemplate(EventHubsProducerFactory producerFactory,
                                                   EventHubsMessageConverter messageConverter) {
            EventHubsTemplate eventHubsTemplate = new EventHubsTemplate(producerFactory);
            eventHubsTemplate.setMessageConverter(messageConverter);
            return eventHubsTemplate;
        }

    }

}
