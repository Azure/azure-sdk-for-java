// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.eventhubs.core.EventHubProcessorContainer;
import com.azure.spring.eventhubs.core.EventHubsTemplate;
import com.azure.spring.eventhubs.core.processor.DefaultEventHubNamespaceProcessorFactory;
import com.azure.spring.eventhubs.core.processor.EventHubProcessorFactory;
import com.azure.spring.eventhubs.core.producer.DefaultEventHubNamespaceProducerFactory;
import com.azure.spring.eventhubs.core.producer.EventHubProducerFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.eventhubs.core.properties.ProducerProperties;
import com.azure.spring.messaging.PropertiesSupplier;
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
import reactor.util.function.Tuple2;

import static com.azure.spring.core.properties.AzurePropertiesUtils.copyAzureCommonProperties;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubsTemplate} and {@link
 * EventHubProcessorContainer}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventHubsTemplate.class)
@AutoConfigureAfter(AzureEventHubAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
@ConditionalOnBean(AzureEventHubProperties.class)
@Import({
    AzureEventHubMessagingAutoConfiguration.EventHubsTemplateConfiguration.class,
    AzureEventHubMessagingAutoConfiguration.ProcessorContainerConfiguration.class
})
public class AzureEventHubMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NamespaceProperties eventHubNamespaceProperties(AzureEventHubProperties properties) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        BeanUtils.copyProperties(properties, namespaceProperties);
        copyAzureCommonProperties(properties, namespaceProperties);
        return namespaceProperties;
    }

    /**
     * Configure the {@link EventHubProcessorContainer}
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(CheckpointStore.class)
    public static class ProcessorContainerConfiguration {


        @Bean
        @ConditionalOnMissingBean
        public EventHubProcessorFactory defaultEventProcessorFactory(
            NamespaceProperties properties, CheckpointStore checkpointStore,
            ObjectProvider<PropertiesSupplier<Tuple2<String, String>, ProcessorProperties>> suppliers) {
            return new DefaultEventHubNamespaceProcessorFactory(checkpointStore, properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubProcessorContainer eventProcessorContainer(EventHubProcessorFactory processorFactory) {
            return new EventHubProcessorContainer(processorFactory);
        }

    }

    /**
     * Configure the {@link EventHubsTemplate}
     */
    @Configuration(proxyBeanMethods = false)
    public static class EventHubsTemplateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EventHubProducerFactory defaultEventHubProducerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
            return new DefaultEventHubNamespaceProducerFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubsTemplate eventHubsTemplate(EventHubProducerFactory producerFactory) {
            return new EventHubsTemplate(producerFactory);
        }

    }

}
