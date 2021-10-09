// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.eventhubs;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;

/**
 * Health indicator for Event Hubs.
 */
public class EventHubHealthIndicator implements HealthIndicator {

    private static final int DEFAULT_TIMEOUT = 30;

    private final EventHubProducerAsyncClient producerAsyncClient;
    private final EventHubConsumerAsyncClient consumerAsyncClient;

    private int timeout = DEFAULT_TIMEOUT;

    public EventHubHealthIndicator(EventHubProducerAsyncClient producerAsyncClient,
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
                                          .block(Duration.ofSeconds(timeout));
            }
            return consumerAsyncClient.getEventHubProperties()
                                      .map(p -> Health.up().build())
                                      .block(Duration.ofSeconds(timeout));
        } catch (Exception e) {
            return Health.down(e)
                         .withDetail("Failed to retrieve event hub information", "")
                         .build();
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
