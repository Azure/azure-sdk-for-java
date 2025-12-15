// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.implementation.converter.ObjectMapperHolder;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceConsumerFactory;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusConsumerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.core.properties.ConsumerProperties;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
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
            ApplicationContext applicationContext,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>> suppliers,
            ObjectProvider<AzureTokenCredentialResolver> tokenCredentialResolvers,
            @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) ObjectProvider<TokenCredential> defaultTokenCredentials,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorClientBuilderCustomizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorClientBuilderCustomizers) {
            DefaultServiceBusNamespaceProcessorFactory factory = new DefaultServiceBusNamespaceProcessorFactory(properties, suppliers.getIfAvailable());
            factory.setApplicationContext(applicationContext);
            factory.setDefaultCredential(defaultTokenCredentials.getIfAvailable());
            factory.setTokenCredentialResolver(tokenCredentialResolvers.getIfAvailable());
            clientBuilderCustomizers.orderedStream().forEach(factory::addServiceBusClientBuilderCustomizer);
            processorClientBuilderCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            sessionProcessorClientBuilderCustomizers.orderedStream().forEach(factory::addSessionBuilderCustomizer);
            return factory;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ConsumerContainerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusConsumerFactory defaultServiceBusNamespaceConsumerFactory(
            NamespaceProperties properties,
            ApplicationContext applicationContext,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ConsumerProperties>> suppliers,
            ObjectProvider<AzureTokenCredentialResolver> tokenCredentialResolvers,
            @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) ObjectProvider<TokenCredential> defaultTokenCredentials,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> customizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder>> sessionReceiverCustomizers) {
            DefaultServiceBusNamespaceConsumerFactory factory = new DefaultServiceBusNamespaceConsumerFactory(properties, suppliers.getIfAvailable());
            factory.setApplicationContext(applicationContext);
            factory.setDefaultCredential(defaultTokenCredentials.getIfAvailable());
            factory.setTokenCredentialResolver(tokenCredentialResolvers.getIfAvailable());
            customizers.orderedStream().forEach(factory::addServiceBusClientBuilderCustomizer);
            sessionReceiverCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ServiceBusTemplateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusProducerFactory defaultServiceBusNamespaceProducerFactory(
            NamespaceProperties properties,
            ApplicationContext applicationContext,
            ObjectProvider<PropertiesSupplier<String, ProducerProperties>> suppliers,
            ObjectProvider<AzureTokenCredentialResolver> tokenCredentialResolvers,
            @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) ObjectProvider<TokenCredential> defaultTokenCredentials,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderClientBuilderCustomizers) {
            DefaultServiceBusNamespaceProducerFactory factory = new DefaultServiceBusNamespaceProducerFactory(properties, suppliers.getIfAvailable());
            factory.setApplicationContext(applicationContext);
            factory.setDefaultCredential(defaultTokenCredentials.getIfAvailable());
            factory.setTokenCredentialResolver(tokenCredentialResolvers.getIfAvailable());
            clientBuilderCustomizers.orderedStream().forEach(factory::addServiceBusClientBuilderCustomizer);
            senderClientBuilderCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "true", matchIfMissing = true)
        AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> defaultServiceBusMessageConverter() {
            return new ServiceBusMessageConverter(ObjectMapperHolder.OBJECT_MAPPER);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "false")
        AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> serviceBusMessageConverter(ObjectMapper objectMapper) {
            return new ServiceBusMessageConverter(objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusProducerFactory.class)
        ServiceBusTemplate serviceBusTemplate(AzureServiceBusProperties properties,
                                              ServiceBusProducerFactory producerFactory,
                                              ServiceBusConsumerFactory consumerFactory,
                                              AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter) {
            ServiceBusTemplate serviceBusTemplate = new ServiceBusTemplate(producerFactory, consumerFactory);
            serviceBusTemplate.setMessageConverter(messageConverter);
            if (properties.getProducer().getEntityType() != null) {
                serviceBusTemplate.setDefaultEntityType(properties.getProducer().getEntityType());
            } else {
                serviceBusTemplate.setDefaultEntityType(properties.getEntityType());
            }
            return serviceBusTemplate;
        }
    }
}
