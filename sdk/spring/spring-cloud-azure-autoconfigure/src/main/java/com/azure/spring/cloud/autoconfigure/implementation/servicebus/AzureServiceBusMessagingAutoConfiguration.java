// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.implementation.converter.ObjectMapperHolder;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
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
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Messaging Azure Service Bus support.
 *
 * @since 4.0.0
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

    @Configuration(proxyBeanMethods = false)
    static class ProcessorContainerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusProcessorFactory defaultServiceBusNamespaceProcessorFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>> suppliers) {
            return new DefaultServiceBusNamespaceProcessorFactory(properties, suppliers.getIfAvailable());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ServiceBusTemplateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusProducerFactory defaultServiceBusNamespaceProducerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers) {
            return new DefaultServiceBusNamespaceProducerFactory(properties, suppliers.getIfAvailable());
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "true", matchIfMissing = true)
        ServiceBusMessageConverter defaultServiceBusMessageConverter() {
            return new ServiceBusMessageConverter(ObjectMapperHolder.OBJECT_MAPPER);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "false")
        ServiceBusMessageConverter serviceBusMessageConverter(ObjectMapper objectMapper) {
            return new ServiceBusMessageConverter(objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusProducerFactory.class)
        ServiceBusTemplate serviceBusTemplate(ServiceBusProducerFactory senderClientfactory,
                                                     ServiceBusMessageConverter messageConverter) {
            ServiceBusTemplate serviceBusTemplate = new ServiceBusTemplate(senderClientfactory);
            serviceBusTemplate.setMessageConverter(messageConverter);
            return serviceBusTemplate;
        }
    }
}
