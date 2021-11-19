// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.servicebus.factory.ServiceBusReceiverClientBuilderFactory;
import com.azure.spring.service.servicebus.factory.ServiceBusSessionReceiverClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.util.List;

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
class AzureServiceBusConsumerClientConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-aware", havingValue = "false",
        matchIfMissing = true)
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "consumer.entity-type" })
    static class NoneSessionConsumerClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusReceiverClientBuilderFactory serviceBusReceiverClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder>>> customizers) {

            ServiceBusReceiverClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getConsumer())) {
                factory = new ServiceBusReceiverClientBuilderFactory(serviceBusProperties.buildConsumerProperties());
            } else {
                factory = new ServiceBusReceiverClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(), serviceBusProperties.buildConsumerProperties());
            }
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            connectionStringProviders.ifAvailable(factory::setConnectionStringProvider);
            customizers.ifAvailable(cs -> cs.forEach(factory::addBuilderCustomizer));
            return factory;
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilder(
            ServiceBusReceiverClientBuilderFactory builderFactory) {
            return builderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusReceiverAsyncClient serviceBusReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusReceiverClient serviceBusReceiverClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-aware", havingValue = "true")
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "consumer.entity-type" })
    static class SessionConsumerClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionReceiverClientBuilderFactory serviceBusSessionReceiverClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
            ObjectProvider<List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder>>> customizers) {
            ServiceBusSessionReceiverClientBuilderFactory factory;
            if (isDedicatedConnection(serviceBusProperties.getConsumer())) {
                factory = new ServiceBusSessionReceiverClientBuilderFactory(serviceBusProperties.buildConsumerProperties());
            } else {
                factory = new ServiceBusSessionReceiverClientBuilderFactory(
                    serviceBusClientBuilders.getIfAvailable(), serviceBusProperties.buildConsumerProperties());
            }

            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            connectionStringProviders.ifAvailable(factory::setConnectionStringProvider);
            customizers.ifAvailable(cs -> cs.forEach(factory::addBuilderCustomizer));
            return factory;
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilder(
            ServiceBusSessionReceiverClientBuilderFactory builderFactory) {
            return builderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionReceiverClient serviceBusSessionReceiverClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }

    private static boolean isDedicatedConnection(AzureServiceBusProperties.Consumer consumer) {
        return StringUtils.hasText(consumer.getNamespace()) || StringUtils.hasText(consumer.getConnectionString());
    }

}
