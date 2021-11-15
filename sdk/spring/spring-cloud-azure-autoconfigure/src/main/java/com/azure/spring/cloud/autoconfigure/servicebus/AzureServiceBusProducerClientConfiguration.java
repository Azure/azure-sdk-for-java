// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.service.servicebus.factory.ServiceBusSenderClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusSenderClient} and a {@link ServiceBusSenderAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-name", "producer.entity-name" })
class AzureServiceBusProducerClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "entity-type", "producer.entity-type" })
    public ServiceBusSenderClientBuilderFactory serviceBusSenderClientBuilderFactory(
        AzureServiceBusProperties serviceBusProperties,
        ObjectProvider<ServiceBusClientBuilder> serviceBusClientBuilders) {

        ServiceBusSenderClientBuilderFactory builderFactory;
        if (isDedicatedConnection(serviceBusProperties.getProducer())) {
            builderFactory = new ServiceBusSenderClientBuilderFactory(serviceBusProperties.buildProducerProperties());
        } else {
            builderFactory = new ServiceBusSenderClientBuilderFactory(
                serviceBusClientBuilders.getIfAvailable(), serviceBusProperties.buildProducerProperties());
        }
        builderFactory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
        return builderFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusSenderClientBuilderFactory.class)
    public ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClientBuilder(
        ServiceBusSenderClientBuilderFactory builderFactory) {
        return builderFactory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderAsyncClient serviceBusSenderAsyncClient(
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
        return senderClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderClient serviceBusSenderClient(
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
        return senderClientBuilder.buildClient();
    }

    private boolean isDedicatedConnection(AzureServiceBusProperties.Producer producer) {
        return StringUtils.hasText(producer.getNamespace()) || StringUtils.hasText(producer.getConnectionString());
    }
}
