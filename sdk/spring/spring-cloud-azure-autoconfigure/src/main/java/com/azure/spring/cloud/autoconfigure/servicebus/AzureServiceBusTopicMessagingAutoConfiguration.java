// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.processor.ServiceBusTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.processor.container.ServiceBusTopicProcessorContainer;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.core.producer.DefaultServiceBusNamespaceTopicProducerClientFactory;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
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
import org.springframework.context.annotation.Import;
import reactor.util.function.Tuple2;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_TOPIC_TEMPLATE_BEAN_NAME;

/**
 * An auto-configuration for Service Bus topic
 */
@Configuration
@ConditionalOnClass(ServiceBusTopicProcessorClientFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
@Import({
    AzureServiceBusTopicMessagingAutoConfiguration.ServiceBusTopicTemplateConfiguration.class,
    AzureServiceBusTopicMessagingAutoConfiguration.ProcessorContainerConfiguration.class
})
public class AzureServiceBusTopicMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NamespaceProperties serviceBusNamespaceTopicProperties(AzureServiceBusProperties properties) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        BeanUtils.copyProperties(properties, namespaceProperties);
        return namespaceProperties;
    }
    /**
     * Configure the {@link ServiceBusTopicProcessorClientFactory}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ProcessorContainerConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ServiceBusTopicProcessorClientFactory defaultServiceBusNamespaceTopicProcessorFactory(NamespaceProperties properties,
                                                                                                     ObjectProvider<PropertiesSupplier<Tuple2<String, String>, ProcessorProperties>> suppliers) {
            return new DefaultServiceBusNamespaceTopicProcessorClientFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusTopicProcessorContainer eventProcessorContainer(ServiceBusTopicProcessorClientFactory processorFactory) {
            return new ServiceBusTopicProcessorContainer(processorFactory);
        }
    }

    /**
     * Configure the {@link ServiceBusQueueOperation}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ServiceBusTopicTemplateConfiguration {
        @Bean(SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME)
        public ServiceBusProducerFactory defaultServiceBusNamespaceTopicProducerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
            return new DefaultServiceBusNamespaceTopicProducerClientFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusMessageConverter messageConverter() {
            return new ServiceBusMessageConverter();
        }

        @Bean(SERVICE_BUS_TOPIC_TEMPLATE_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_TOPIC_TEMPLATE_BEAN_NAME)
        @ConditionalOnBean(name = SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME)
        public ServiceBusTopicOperation topicOperation(
            @Qualifier(SERVICE_BUS_TOPIC_SENDER_CLIENT_FACTORY_BEAN_NAME) ServiceBusProducerFactory senderClientfactory,
            ServiceBusMessageConverter messageConverter) {
            return new ServiceBusTopicTemplate(senderClientfactory, messageConverter);
        }
    }
}
