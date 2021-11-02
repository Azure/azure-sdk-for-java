// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.processor.container.ServiceBusQueueProcessorContainer;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueTemplate;
import com.azure.spring.servicebus.core.producer.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
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
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_QUEUE_TEMPLATE_BEAN_NAME;

/**
 * An auto-configuration for Service Bus Queue.
 */
@Configuration
@ConditionalOnClass(ServiceBusProcessorFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(ServiceBusClientBuilder.class)
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
@Import({
    AzureServiceBusQueueMessagingAutoConfiguration.ServiceBusQueueTemplateConfiguration.class,
    AzureServiceBusQueueMessagingAutoConfiguration.ProcessorContainerConfiguration.class
})
public class AzureServiceBusQueueMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NamespaceProperties serviceBusNamespaceTopicProperties(AzureServiceBusProperties properties) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        BeanUtils.copyProperties(properties, namespaceProperties);
        return namespaceProperties;
    }

    /**
     * Configure the {@link ServiceBusQueueProcessorContainer}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ProcessorContainerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorFactory defaultServiceBusNamespaceQueueProcessorFactory(NamespaceProperties properties,
                                                                                          ObjectProvider<PropertiesSupplier<String, ProcessorProperties>> suppliers) {
            return new DefaultServiceBusNamespaceProcessorFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusQueueProcessorContainer eventProcessorContainer(ServiceBusProcessorFactory processorFactory) {
            return new ServiceBusQueueProcessorContainer(processorFactory);
        }
    }

    /**
     * Configure the {@link ServiceBusQueueOperation}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ServiceBusQueueTemplateConfiguration {

        @Bean(SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME)
        public ServiceBusProducerFactory defaultServiceBusNamespaceQueueProducerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
            return new DefaultServiceBusNamespaceProducerFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusMessageConverter messageConverter() {
            return new ServiceBusMessageConverter();
        }

        @Bean(SERVICE_BUS_QUEUE_TEMPLATE_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_QUEUE_TEMPLATE_BEAN_NAME)
        @ConditionalOnBean(name = SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME)
        public ServiceBusTemplate queueOperation(
            @Qualifier(SERVICE_BUS_QUEUE_SENDER_CLIENT_FACTORY_BEAN_NAME) ServiceBusProducerFactory senderClientfactory,
            ServiceBusMessageConverter messageConverter) {
            return new ServiceBusQueueTemplate(senderClientfactory, messageConverter);
        }
    }
}
