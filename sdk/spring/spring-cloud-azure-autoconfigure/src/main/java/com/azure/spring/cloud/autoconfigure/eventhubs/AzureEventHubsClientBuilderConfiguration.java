// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.CONFIGURATION_BUILDER_BEAN_NAME;

/**
 * Configuration for Event Hub client builder, which provides {@link EventHubClientBuilder}.
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhubs", name = "event-hub-name")
class AzureEventHubsClientBuilderConfiguration {

    private final com.azure.core.util.Configuration configuration;
    public AzureEventHubsClientBuilderConfiguration(@Qualifier(CONFIGURATION_BUILDER_BEAN_NAME) ConfigurationBuilder configurationBuilder) {
        this.configuration = configurationBuilder.buildSection("eventhubs");
    }

    @Bean
    @ConditionalOnMissingBean
    EventHubClientBuilder eventHubClientBuilder(EventHubClientBuilderFactory factory) {
        return factory.build(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    EventHubClientBuilderFactory eventHubClientBuilderFactory(AzureEventHubsProperties properties,
        ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHubs>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> customizers) {
        final EventHubClientBuilderFactory factory = new EventHubClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

}
