// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
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
import com.azure.spring.messaging.servicebus.support.converter.ServiceBusMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
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

    /**
     * Configure the {@link ServiceBusProcessorFactory}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ProcessorContainerConfiguration {

        /**
         * Creates a default Service Bus namespace processor factory.
         *
         * @param properties Service Bus namespace properties.
         * @param suppliers ObjectProvider suppliers.
         * @return A default Service Bus namespace processor factory.
         */
        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorFactory defaultServiceBusNamespaceProcessorFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>> suppliers) {
            return new DefaultServiceBusNamespaceProcessorFactory(properties, suppliers.getIfAvailable());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ConsumerContainerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusConsumerFactory defaultServiceBusNamespaceConsumerFactory(
            NamespaceProperties properties,
            ObjectProvider<PropertiesSupplier<ConsumerIdentifier, ConsumerProperties>> suppliers,
            ObjectProvider<AzureTokenCredentialResolver> tokenCredentialResolvers,
            ObjectProvider<TokenCredential> defaultTokenCredentials,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder>> sessionReceiverCustomizers) {
            DefaultServiceBusNamespaceConsumerFactory factory = new DefaultServiceBusNamespaceConsumerFactory(properties, suppliers.getIfAvailable());
            factory.setDefaultCredential(defaultTokenCredentials.getIfAvailable());
            factory.setTokenCredentialResolver(tokenCredentialResolvers.getIfAvailable());
            sessionReceiverCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }
    }

    /**
     * Configure the {@link ServiceBusTemplate}
     */
    @Configuration(proxyBeanMethods = false)
    public static class ServiceBusTemplateConfiguration {

        /**
         * Creates a default Service Bus namespace producer factory.
         *
         * @param properties Service Bus namespace properties.
         * @param suppliers ObjectProvider suppliers.
         * @return A default Service Bus namespace producer factory.
         */
        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProducerFactory defaultServiceBusNamespaceProducerFactory(
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

        /**
         * Creates a Service Bus template.
         *
         * @param producerFactory A Service Bus producer factory.
         * @param messageConverter A Service Bus message converter.
         * @return A Service Bus template.
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusProducerFactory.class)
        public ServiceBusTemplate serviceBusTemplate(ServiceBusProducerFactory producerFactory,
                                                     ServiceBusMessageConverter messageConverter) {
            ServiceBusTemplate serviceBusTemplate = new ServiceBusTemplate(producerFactory);
            serviceBusTemplate.setMessageConverter(messageConverter);
            return serviceBusTemplate;
        }

        @Bean
        static ServiceBusTemplatePostProcessor serviceBusTemplatePostProcessor() {
            return new ServiceBusTemplatePostProcessor();
        }

        static class ServiceBusTemplatePostProcessor implements BeanPostProcessor, BeanFactoryAware {

            private BeanFactory beanFactory;

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof ServiceBusTemplate) {
                    ServiceBusTemplate serviceBusTemplate = (ServiceBusTemplate) bean;
                    AzureServiceBusProperties properties = beanFactory.getBean(AzureServiceBusProperties.class);
                    if (properties.getProducer().getEntityType() != null) {
                        serviceBusTemplate.setDefaultEntityType(properties.getProducer().getEntityType());
                    } else {
                        serviceBusTemplate.setDefaultEntityType(properties.getEntityType());
                    }
                }
                return bean;
            }

            @Override
            public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
                this.beanFactory = beanFactory;
            }
        }
    }
}
