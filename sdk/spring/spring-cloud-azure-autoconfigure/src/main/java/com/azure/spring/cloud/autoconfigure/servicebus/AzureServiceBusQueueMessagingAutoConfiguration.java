// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceQueueProcessorClientFactory;
import com.azure.spring.servicebus.core.processor.ServiceBusQueueProcessorClientFactory;
import com.azure.spring.servicebus.core.processor.ServiceBusTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueTemplate;
import com.azure.spring.servicebus.core.sender.DefaultServiceBusNamespaceQueueSenderClientFactory;
import com.azure.spring.servicebus.core.sender.ServiceBusSenderClientFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicOperation;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicTemplate;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME;

/**
 * An auto-configuration for Service Bus Queue.
 */
@Configuration
@ConditionalOnClass(ServiceBusQueueProcessorClientFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(ServiceBusClientBuilder.class)
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
public class AzureServiceBusQueueMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NamespaceProperties serviceBusNamespaceTopicProperties(AzureServiceBusProperties properties) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        BeanUtils.copyProperties(properties, namespaceProperties);
        return namespaceProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueProcessorClientFactory defaultServiceBusNamespaceQueueProcessorFactory(NamespaceProperties properties,
                                                                                                 ObjectProvider<PropertiesSupplier<String, ProcessorProperties>> suppliers) {
        return new DefaultServiceBusNamespaceQueueProcessorClientFactory(properties, suppliers.getIfAvailable());
    }

    @Bean(SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME)
    public ServiceBusSenderClientFactory defaultServiceBusNamespaceQueueProducerFactory(
        NamespaceProperties properties,
        ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
        return new DefaultServiceBusNamespaceQueueSenderClientFactory(properties, suppliers.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageConverter messageConverter() {
        return new ServiceBusMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ ServiceBusQueueProcessorClientFactory.class, ServiceBusSenderClientFactory.class })
    public ServiceBusQueueOperation queueOperation(
        @Qualifier(SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME) ServiceBusSenderClientFactory senderClientfactory,
        ServiceBusQueueProcessorClientFactory processorClientFactory,
        ServiceBusMessageConverter messageConverter) {
        return new ServiceBusQueueTemplate(senderClientfactory, processorClientFactory, messageConverter);
    }

}
