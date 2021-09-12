// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusClientBuilder.class)
class AzureServiceBusClientBuilderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusClientBuilderFactory factory(
        AzureServiceBusProperties properties,
        ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders) {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(properties);
        builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        return builderFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusClientBuilder serviceBusClientBuilder(ServiceBusClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public StaticConnectionStringProvider<AzureServiceType.ServiceBus> staticServiceBusConnectionStringProvider(
        AzureServiceBusProperties serviceBusProperties) {

        return new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                    serviceBusProperties.getConnectionString());
    }


}