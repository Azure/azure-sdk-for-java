// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for Event Hub client builder, which provides {@link EventHubClientBuilder}.
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhubs", name = "event-hub-name")
class AzureEventHubsClientBuilderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    EventHubClientBuilder eventHubClientBuilder(@Qualifier(EVENT_HUB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
                                                    EventHubClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean(EVENT_HUB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean
    EventHubClientBuilderFactory eventHubClientBuilderFactory(AzureEventHubsProperties properties,
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.EventHubs>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> customizers) {
        final EventHubClientBuilderFactory factory = new EventHubClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

}
