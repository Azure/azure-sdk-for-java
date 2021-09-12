// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.eventhub.factory.EventHubClientBuilderFactory;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventHubClientBuilder.class)
class AzureEventHubClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventHubConsumerAsyncClient eventHubConsumerAsyncClient(EventHubClientBuilder builder) {
        return builder.buildAsyncConsumerClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubConsumerClient eventHubConsumerClient(EventHubClientBuilder builder) {
        return builder.buildConsumerClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubProducerAsyncClient eventHubProducerAsyncClient(EventHubClientBuilder builder) {
        return builder.buildAsyncProducerClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubProducerClient eventHubProducerClient(EventHubClientBuilder builder) {
        return builder.buildProducerClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientBuilder eventHubClientBuilder(EventHubClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientBuilderFactory factory(
        AzureEventHubProperties properties,
        ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
        final EventHubClientBuilderFactory builderFactory = new EventHubClientBuilderFactory(properties);

        builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        return builderFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    @ConditionalOnProperty(prefix = AzureEventHubProperties.PREFIX, name = "connection-string")
    public StaticConnectionStringProvider<AzureServiceType.EventHub> eventHubStaticConnectionStringProvider(
        AzureEventHubProperties eventHubProperties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUB,
                                                    eventHubProperties.getConnectionString());
    }
}
