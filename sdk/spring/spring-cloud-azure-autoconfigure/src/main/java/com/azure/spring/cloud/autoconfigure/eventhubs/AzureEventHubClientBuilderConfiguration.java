// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.eventhubs.factory.EventHubClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Event Hub client builder, which provides {@link EventHubClientBuilder}.
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhubs", name = "event-hub-name")
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
    @ConditionalOnProperty("spring.cloud.azure.eventhubs.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.EventHub> eventHubStaticConnectionStringProvider(
        AzureEventHubProperties eventHubProperties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUB,
                                                    eventHubProperties.getConnectionString());
    }

}
