// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.servicebus.factory.ServiceBusClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_DEFAULT_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_DEFAULT_CLIENT_BUILDER_FACTORY_BEAN_NAME;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = { "connection-string", "namespace" })
class AzureServiceBusClientBuilderConfiguration {

    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusClientBuilderConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.serviceBusProperties = serviceBusProperties;
    }

    @Bean(SERVICE_BUS_DEFAULT_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_DEFAULT_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    public ServiceBusClientBuilderFactory serviceBusClientBuilderFactory(
        ObjectProvider<ConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders) {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.serviceBusProperties);
        builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);
        return builderFactory;
    }

    @Bean(SERVICE_BUS_DEFAULT_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_DEFAULT_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder serviceBusClientBuilder(
        @Qualifier(SERVICE_BUS_DEFAULT_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.servicebus.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.ServiceBus> staticServiceBusConnectionStringProvider() {

        return new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                    this.serviceBusProperties.getConnectionString());
    }

}
