// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.implementation.servicebus.factory.ServiceBusClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "connection-string", "namespace" })
class AzureServiceBusClientBuilderConfiguration {

    private final com.azure.core.util.Configuration configuration;
    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusClientBuilderConfiguration(AzureServiceBusProperties serviceBusProperties, ConfigurationBuilder configurationBuilder) {
        this.serviceBusProperties = serviceBusProperties;
        configuration = configurationBuilder.section("servicebus").build();
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceBusClientBuilderFactory serviceBusClientBuilderFactory(
        ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> customizers) {

        final ServiceBusClientBuilderFactory factory = new ServiceBusClientBuilderFactory(this.serviceBusProperties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceBusClientBuilder serviceBusClientBuilder(ServiceBusClientBuilderFactory factory) {
        return factory.build(configuration);
    }

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.servicebus.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.ServiceBus> staticServiceBusConnectionStringProvider() {

        return new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                    this.serviceBusProperties.getConnectionString());
    }

}
