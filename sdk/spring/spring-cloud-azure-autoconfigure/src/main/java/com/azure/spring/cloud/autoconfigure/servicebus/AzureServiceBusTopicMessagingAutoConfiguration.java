// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.processor.ServiceBusTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.sender.DefaultServiceBusNamespaceTopicSenderClientFactory;
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
import reactor.util.function.Tuple2;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME;

/**
 * An auto-configuration for Service Bus topic
 */
@Configuration
@ConditionalOnClass(ServiceBusTopicProcessorClientFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
public class AzureServiceBusTopicMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NamespaceProperties serviceBusNamespaceTopicProperties(AzureServiceBusProperties properties) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        BeanUtils.copyProperties(properties, namespaceProperties);
        return namespaceProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicProcessorClientFactory defaultServiceBusNamespaceTopicProcessorFactory(NamespaceProperties properties,
                                                                              ObjectProvider<PropertiesSupplier<Tuple2<String, String>, ProcessorProperties>> suppliers) {
        return new DefaultServiceBusNamespaceTopicProcessorClientFactory(properties, suppliers.getIfAvailable());
    }

    @Bean(SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME)
    public ServiceBusSenderClientFactory defaultServiceBusNamespaceTopicProducerFactory(
        NamespaceProperties properties,
        ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
        return new DefaultServiceBusNamespaceTopicSenderClientFactory(properties, suppliers.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageConverter messageConverter() {
        return new ServiceBusMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ ServiceBusTopicProcessorClientFactory.class, ServiceBusSenderClientFactory.class })
    public ServiceBusTopicOperation topicOperation(
        @Qualifier(SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME) ServiceBusSenderClientFactory senderClientfactory,
                                                   ServiceBusTopicProcessorClientFactory processorClientFactory,
                                                   ServiceBusMessageConverter messageConverter) {
        return new ServiceBusTopicTemplate(senderClientfactory, processorClientFactory, messageConverter);
    }
}
