// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.core.util.AzurePropertiesUtils;
import com.azure.spring.eventhubs.core.EventHubsProcessorContainer;
import com.azure.spring.eventhubs.core.EventHubsTemplate;
import com.azure.spring.eventhubs.core.processor.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.producer.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.eventhubs.core.producer.EventHubsProducerFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.ConsumerIdentifier;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.core.util.AzurePropertiesUtils.copyAzureCommonProperties;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubsTemplate} and {@link
 * EventHubsProcessorContainer}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventHubsTemplate.class)
@AutoConfigureAfter(AzureEventHubsAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
@ConditionalOnBean(AzureEventHubsProperties.class)
@Import({
    AzureEventHubsMessagingAutoConfiguration.EventHubsTemplateConfiguration.class,
    AzureEventHubsMessagingAutoConfiguration.ProcessorContainerConfiguration.class
})
public class AzureEventHubsMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NamespaceProperties eventHubNamespaceProperties(AzureEventHubsProperties properties) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        AzurePropertiesUtils.copyAzureCommonProperties(properties, namespaceProperties);
        BeanUtils.copyProperties(properties, namespaceProperties);
        copyAzureCommonProperties(properties, namespaceProperties);
        return namespaceProperties;
    }

    /**
     * Configure the {@link EventHubsProcessorContainer}
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(CheckpointStore.class)
    public static class ProcessorContainerConfiguration {


        @Bean
        @ConditionalOnMissingBean
        public EventHubsProcessorFactory defaultEventHubsNamespaceProcessorFactory(
            NamespaceProperties properties, CheckpointStore checkpointStore,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>> suppliers) {
            return new DefaultEventHubsNamespaceProcessorFactory(checkpointStore, properties,
                suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubsProcessorContainer eventHubsProcessorContainer(EventHubsProcessorFactory processorFactory) {
            return new EventHubsProcessorContainer(processorFactory);
        }

    }

    /**
     * Configure the {@link EventHubsTemplate}
     */
    @Configuration(proxyBeanMethods = false)
    public static class EventHubsTemplateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EventHubsProducerFactory defaultEventHubsNamespaceProducerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
            return new DefaultEventHubsNamespaceProducerFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubsMessageConverter messageConverter() {
            return new EventHubsMessageConverter();
        }

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
