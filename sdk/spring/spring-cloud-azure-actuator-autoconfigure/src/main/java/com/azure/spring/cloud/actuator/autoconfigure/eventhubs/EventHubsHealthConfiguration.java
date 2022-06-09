// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.spring.cloud.actuator.eventhubs.EventHubsHealthIndicator;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for Event actuator.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ EventHubClientBuilder.class, HealthIndicator.class })
@AutoConfigureAfter(AzureEventHubsAutoConfiguration.class)
@ConditionalOnBean(EventHubClientBuilder.class)
@ConditionalOnEnabledHealthIndicator("azure-eventhubs")
public class EventHubsHealthConfiguration {

    @Bean
    EventHubsHealthIndicator eventHubsHealthIndicator(
        ObjectProvider<EventHubProducerAsyncClient> producerAsyncClients,
        ObjectProvider<EventHubConsumerAsyncClient> consumerAsyncClients) {

        return new EventHubsHealthIndicator(producerAsyncClients.getIfAvailable(),
                                           consumerAsyncClients.getIfAvailable());
    }
}
