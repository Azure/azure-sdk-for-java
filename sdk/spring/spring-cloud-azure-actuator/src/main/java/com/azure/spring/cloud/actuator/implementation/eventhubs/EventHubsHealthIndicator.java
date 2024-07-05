// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuator.implementation.util.ActuateConstants.DEFAULT_HEALTH_CHECK_TIMEOUT;

/**
 * Health indicator for Azure Event Hubs.
 */
public class EventHubsHealthIndicator extends AbstractHealthIndicator {

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
    protected void doHealthCheck(Health.Builder builder) {
        if (this.producerAsyncClient == null && this.consumerAsyncClient == null) {
            builder.withDetail("No client configured", "No Event Hub producer or consumer clients found.");
            return;
        }

        if (this.producerAsyncClient != null) {
            producerAsyncClient.getEventHubProperties()
                               .map(p -> builder.up())
                               .block(timeout);
        } else {
            consumerAsyncClient.getEventHubProperties()
                               .map(p -> builder.up())
                               .block(timeout);
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
