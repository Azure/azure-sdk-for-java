// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
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
@ConditionalOnBean({ ServiceBusRecordMessageListener.class, ServiceBusErrorHandler.class })
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

        @Bean
        @ConditionalOnMissingBean
        ServiceBusProcessorClientBuilderFactory serviceBusProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusRecordMessageListener messageListener,
            ServiceBusErrorHandler errorHandler,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> customizers) {

            ServiceBusProcessorClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getProcessor())) {
                factory = new ServiceBusProcessorClientBuilderFactory(
                    serviceBusProperties.buildProcessorProperties(),
                    messageListener,
                    errorHandler);
            } else {
                factory = new ServiceBusProcessorClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(),
                    serviceBusProperties.buildProcessorProperties(),
                    messageListener,
                    errorHandler);
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
            return builderFactory.build();
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

        @Bean
        @ConditionalOnMissingBean
        ServiceBusSessionProcessorClientBuilderFactory serviceBusSessionProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusRecordMessageListener messageListener,
            ServiceBusErrorHandler errorHandler,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> customizers) {

            ServiceBusSessionProcessorClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getProcessor())) {
                factory = new ServiceBusSessionProcessorClientBuilderFactory(
                    serviceBusProperties.buildProcessorProperties(),
                    messageListener,
                    errorHandler);
            } else {
                factory = new ServiceBusSessionProcessorClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(),
                    serviceBusProperties.buildProcessorProperties(),
                    messageListener,
                    errorHandler);
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
            return builderFactory.build();
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
