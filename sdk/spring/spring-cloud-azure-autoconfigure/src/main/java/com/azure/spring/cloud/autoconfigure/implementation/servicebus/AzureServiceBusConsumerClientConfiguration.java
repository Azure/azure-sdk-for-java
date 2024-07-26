// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusReceiverClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSessionReceiverClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusReceiverClient} and a {@link ServiceBusReceiverAsyncClient} or a
 * {@link ServiceBusSessionReceiverAsyncClient} and a {@link ServiceBusSessionReceiverClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-name", "consumer.entity-name" })
@Import({
    AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class
})
@Conditional(AzureServiceBusConsumerCondition.class)
class AzureServiceBusConsumerClientConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-enabled", havingValue = "false",
        matchIfMissing = true)
    static class NoneSessionConsumerClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusReceiverClientBuilderFactory serviceBusReceiverClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> customizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder>> receiveCustomizers) {

            ServiceBusReceiverClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getConsumer())) {
                factory = new ServiceBusReceiverClientBuilderFactory(serviceBusProperties.buildConsumerProperties());
            } else {
                factory = new ServiceBusReceiverClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(), serviceBusProperties.buildConsumerProperties());
            }
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
            customizers.orderedStream().forEach(factory::addClientBuilderCustomizer);
            receiveCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilder(
            ServiceBusReceiverClientBuilderFactory builderFactory) {
            return builderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusReceiverAsyncClient serviceBusReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusReceiverClient serviceBusReceiverClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-enabled", havingValue = "true")
    static class SessionConsumerClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        ServiceBusSessionReceiverClientBuilderFactory serviceBusSessionReceiverClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> customizers,
            ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder>> sessionReceiverCustomizers) {
            ServiceBusSessionReceiverClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getConsumer())) {
                factory = new ServiceBusSessionReceiverClientBuilderFactory(serviceBusProperties.buildConsumerProperties());
            } else {
                factory = new ServiceBusSessionReceiverClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(), serviceBusProperties.buildConsumerProperties());
            }

            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
            customizers.orderedStream().forEach(factory::addClientBuilderCustomizer);
            sessionReceiverCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilder(
            ServiceBusSessionReceiverClientBuilderFactory builderFactory) {
            return builderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceBusSessionReceiverClient serviceBusSessionReceiverClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }

    private static boolean isDedicatedConnection(AzureServiceBusProperties.Consumer consumer) {
        return StringUtils.hasText(consumer.getNamespace()) || StringUtils.hasText(consumer.getConnectionString());
    }

}
