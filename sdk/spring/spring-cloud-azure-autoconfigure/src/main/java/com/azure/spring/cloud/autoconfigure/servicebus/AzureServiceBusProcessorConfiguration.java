// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.service.servicebus.factory.ServiceBusProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for a {@link ServiceBusProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(MessageProcessingListener.class)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus.processor", name = { "queue-name", "topic-name" })
@Import({
    AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class,
    AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class
})
class AzureServiceBusProcessorConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-aware", havingValue = "false",
        matchIfMissing = true)
    static class NoneSessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorClientBuilderFactory serviceBusProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            MessageProcessingListener listener) {
            ServiceBusProcessorClientBuilderFactory builderFactory = new ServiceBusProcessorClientBuilderFactory(
                serviceBusClientBuilders.getIfAvailable(),
                serviceBusProperties.buildProcessorProperties(),
                listener);
            builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);
            return builderFactory;
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilder(
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
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-aware", havingValue = "true")
    static class SessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionProcessorClientBuilderFactory serviceBusSessionProcessorClientBuilderFactory(
            AzureServiceBusProperties serviceBusProperties,
            ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders,
            MessageProcessingListener listener) {
            ServiceBusSessionProcessorClientBuilderFactory builderFactory =
                new ServiceBusSessionProcessorClientBuilderFactory(serviceBusClientBuilders.getIfAvailable(),
                    serviceBusProperties.buildProcessorProperties(), listener);
            builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);
            return builderFactory;
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilder(
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

}
