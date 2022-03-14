// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import com.azure.spring.messaging.servicebus.implementation.core.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.messaging.servicebus.implementation.core.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.messaging.servicebus.support.converter.ServiceBusMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyAzureCommonProperties;


/**
 * An auto-configuration for Service Bus Queue.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "connection-string", "namespace" })
@ConditionalOnBean(AzureServiceBusProperties.class)
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
@Import({
    AzureServiceBusMessagingAutoConfiguration.ServiceBusTemplateConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class
})
public class AzureServiceBusMessagingAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusMessagingAutoConfiguration.class);
    @Bean
    @ConditionalOnMissingBean
    NamespaceProperties serviceBusNamespaceProperties(AzureServiceBusProperties properties,
                                                      ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders) {
        NamespaceProperties namespaceProperties = new NamespaceProperties();
        BeanUtils.copyProperties(properties, namespaceProperties);
        copyAzureCommonProperties(properties, namespaceProperties);
        if (namespaceProperties.getConnectionString() == null) {
            ServiceConnectionStringProvider<AzureServiceType.ServiceBus> connectionStringProvider =
                connectionStringProviders.getIfAvailable();
            if (connectionStringProvider != null) {
                namespaceProperties.setConnectionString(connectionStringProvider.getConnectionString());
                LOGGER.info("Service Bus connection string is set from {} now.", connectionStringProvider.getClass().getName());
            }
        }
        return namespaceProperties;
    }

    /**
     * Configure the {@link ServiceBusProcessorFactory}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ProcessorContainerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorFactory defaultServiceBusNamespaceProcessorFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>> suppliers) {
            return new DefaultServiceBusNamespaceProcessorFactory(properties, suppliers.getIfAvailable());
        }
    }

    /**
     * Configure the {@link ServiceBusTemplate}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ServiceBusTemplateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProducerFactory defaultServiceBusNamespaceProducerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
            return new DefaultServiceBusNamespaceProducerFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusMessageConverter serviceBusMessageConverter() {
            return new ServiceBusMessageConverter();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusProducerFactory.class)
        public ServiceBusTemplate serviceBusTemplate(ServiceBusProducerFactory senderClientfactory,
                                                     ServiceBusMessageConverter messageConverter) {
            ServiceBusTemplate serviceBusTemplate = new ServiceBusTemplate(senderClientfactory);
            serviceBusTemplate.setMessageConverter(messageConverter);
            return serviceBusTemplate;
        }
    }
}
