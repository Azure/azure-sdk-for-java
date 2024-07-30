// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.servicebus.lifecycle.ServiceBusProcessorClientLifecycleManager;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
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
@Conditional(AzureServiceBusProcessorCondition.class)
class AzureServiceBusProcessorClientConfiguration {

    @Bean
    @ConditionalOnBean(ServiceBusProcessorClient.class)
    @ConditionalOnMissingBean(ServiceBusProcessorClientLifecycleManager.class)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.auto-startup", havingValue = "true",
        matchIfMissing = true)
    @ConditionalOnClass(ServiceBusProcessorClientLifecycleManager.class)
    ServiceBusProcessorClientLifecycleManager processorClientLifecycleManager(ServiceBusProcessorClient processorClient) {
        return new ServiceBusProcessorClientLifecycleManager(processorClient);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-enabled", havingValue = "false",
        matchIfMissing = true)
    static class NoneSessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusProcessorClientBuilderFactory serviceBusProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusRecordMessageListener messageListener,
            ServiceBusErrorHandler errorHandler,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> customizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorCustomizers) {

            ServiceBusProcessorClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getProcessor())) {
                factory = new ServiceBusProcessorClientBuilderFactory(
                    serviceBusProperties.buildProcessorProperties(),
                    customizers.orderedStream().toList(),
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
            processorCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
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
        ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder) {
            return processorClientBuilder.buildProcessorClient();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-enabled", havingValue = "true")
    static class SessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusSessionProcessorClientBuilderFactory serviceBusSessionProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusRecordMessageListener messageListener,
            ServiceBusErrorHandler errorHandler,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> customizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorCustomizers) {

            ServiceBusSessionProcessorClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getProcessor())) {
                factory = new ServiceBusSessionProcessorClientBuilderFactory(
                    serviceBusProperties.buildProcessorProperties(),
                    customizers.orderedStream().toList(),
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
            sessionProcessorCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
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
        ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder processorClientBuilder) {
            return processorClientBuilder.buildProcessorClient();
        }

    }

    private static boolean isDedicatedConnection(AzureServiceBusProperties.Processor processor) {
        return StringUtils.hasText(processor.getNamespace()) || StringUtils.hasText(processor.getConnectionString());
    }

}
