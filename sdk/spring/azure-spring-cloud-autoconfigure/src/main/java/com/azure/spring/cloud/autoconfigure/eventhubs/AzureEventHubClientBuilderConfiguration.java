// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventHubClientBuilderFactory;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Configuration for Event Hub client builder, which provides {@link EventHubClientBuilder}.
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.event-hub-name:}')"
                             + " or !T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.connection-string:}')"
                             + " or !T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.namespace:}')")
class AzureEventHubClientBuilderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientBuilder eventHubClientBuilder(EventHubClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientBuilderFactory eventHubClientBuilderFactory(AzureEventHubProperties properties,
        ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
        final EventHubClientBuilderFactory builderFactory = new EventHubClientBuilderFactory(properties);

        builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_EVENT_HUB);
        return builderFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    @ConditionalOnProperty("spring.cloud.azure.eventhubs.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.EventHub> eventHubStaticConnectionStringProvider(
        AzureEventHubProperties eventHubProperties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUB,
                                                    eventHubProperties.getConnectionString());
    }

}
