// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ServiceBusConditions.ConditionalOnServiceBusClient
class AzureServiceBusClientBuilderConfiguration {

    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusClientBuilderConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.serviceBusProperties = serviceBusProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @Primary
    public ServiceBusClientBuilderFactory serviceBusClientBuilderFactory(
        ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders) {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.serviceBusProperties);
        builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);
        return builderFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    @Primary
    public ServiceBusClientBuilder serviceBusClientBuilder(ServiceBusClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    @ConditionalOnProperty("spring.cloud.azure.servicebus.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.ServiceBus> staticServiceBusConnectionStringProvider() {

        return new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                    this.serviceBusProperties.getConnectionString());
    }

}
