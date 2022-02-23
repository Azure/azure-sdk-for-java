// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.eventhubs;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;

import static com.azure.spring.cloud.actuate.util.implementation.Constants.DEFAULT_HEALTH_CHECK_TIMEOUT;

/**
 * Health indicator for Azure Event Hubs.
 */
public class EventHubsHealthIndicator implements HealthIndicator {

    private final EventHubProducerAsyncClient producerAsyncClient;
    private final EventHubConsumerAsyncClient consumerAsyncClient;

    private Duration timeout = DEFAULT_HEALTH_CHECK_TIMEOUT;

    /**
     * Creates a new instance of {@link EventHubsHealthIndicator}.
     * @param producerAsyncClient the producer client
     * @param consumerAsyncClient the consumer client
     */
    public EventHubsHealthIndicator(EventHubProducerAsyncClient producerAsyncClient,
                                    EventHubConsumerAsyncClient consumerAsyncClient) {
        this.producerAsyncClient = producerAsyncClient;
        this.consumerAsyncClient = consumerAsyncClient;
    }

    @Override
    public Health health() {
        if (this.producerAsyncClient == null && this.consumerAsyncClient == null) {
            return Health.unknown()
                         .withDetail("No client configured", "No Event Hub producer or consumer clients found.")
                         .build();
        }

        try {
            if (this.producerAsyncClient != null) {
                return producerAsyncClient.getEventHubProperties()
                                          .map(p -> Health.up().build())
                                          .block(timeout);
            }
            return consumerAsyncClient.getEventHubProperties()
                                      .map(p -> Health.up().build())
                                      .block(timeout);
        } catch (Exception e) {
            return Health.down(e)
                         .withDetail("Failed to retrieve event hub information", "")
                         .build();
        }
    }

    /**
     * Set health check request timeout.
     * @param timeout the duration value.
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
