// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(MessageProcessingListener.class)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-name", "processor.entity-name" })
@Import({
    AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class,
    AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class
})
class AzureServiceBusProcessorClientConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-enabled", havingValue = "false",
        matchIfMissing = true)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "processor.entity-type" })
    static class NoneSessionProcessorClientConfiguration {

        private final com.azure.core.util.Configuration configuration;
        public NoneSessionProcessorClientConfiguration(ConfigurationBuilder configurationBuilder) {
            this.configuration = configurationBuilder.buildSection("servicebus");
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusProcessorClientBuilderFactory serviceBusProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            MessageProcessingListener listener,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> customizers) {

            ServiceBusProcessorClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getProcessor())) {
                factory = new ServiceBusProcessorClientBuilderFactory(serviceBusProperties.buildProcessorProperties(), listener);
            } else {
                factory = new ServiceBusProcessorClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(), serviceBusProperties.buildProcessorProperties(), listener);
            }
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
            customizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilder(
            ServiceBusProcessorClientBuilderFactory builderFactory) {
            return builderFactory.build(configuration);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder) {
            return processorClientBuilder.buildProcessorClient();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-enabled", havingValue = "true")
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "processor.entity-type" })
    static class SessionProcessorClientConfiguration {

        private final com.azure.core.util.Configuration configuration;
        public SessionProcessorClientConfiguration(ConfigurationBuilder configurationBuilder) {
            this.configuration = configurationBuilder.buildSection("servicebus");
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusSessionProcessorClientBuilderFactory serviceBusSessionProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            MessageProcessingListener listener,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> customizers) {

            ServiceBusSessionProcessorClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getProcessor())) {
                factory = new ServiceBusSessionProcessorClientBuilderFactory(
                    serviceBusProperties.buildProcessorProperties(), listener);
            } else {
                factory = new ServiceBusSessionProcessorClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(), serviceBusProperties.buildProcessorProperties(), listener);
            }
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
            customizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilder(
            ServiceBusSessionProcessorClientBuilderFactory builderFactory) {
            return builderFactory.build(configuration);
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder processorClientBuilder) {
            return processorClientBuilder.buildProcessorClient();
        }

    }
    private static boolean isDedicatedConnection(AzureServiceBusProperties.Processor processor) {
        return StringUtils.hasText(processor.getNamespace()) || StringUtils.hasText(processor.getConnectionString());
    }

}
