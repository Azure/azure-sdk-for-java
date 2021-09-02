// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.spring.cloud.actuate.eventhub.EventHubHealthIndicator;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubOperationAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for Storage actuator.
 */
@Configuration
@ConditionalOnClass({ EventHubClientBuilder.class, HealthIndicator.class })
@AutoConfigureAfter(AzureEventHubOperationAutoConfiguration.class)
public class EventHubHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-eventhub")
    public EventHubHealthIndicator eventHubHealthIndicator(
        ObjectProvider<EventHubProducerAsyncClient> producerAsyncClients,
        ObjectProvider<EventHubConsumerAsyncClient> consumerAsyncClients) {

        return new EventHubHealthIndicator(producerAsyncClients.getIfAvailable(),
                                           consumerAsyncClients.getIfAvailable());
    }

}
