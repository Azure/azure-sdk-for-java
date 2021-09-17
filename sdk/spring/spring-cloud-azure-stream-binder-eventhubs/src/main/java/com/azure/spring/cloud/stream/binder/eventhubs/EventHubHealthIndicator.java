// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.spring.eventhubs.core.EventHubClientFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;
import java.util.Optional;

/**
 * Health indicator for Event Hubs.
 */
public class EventHubHealthIndicator implements HealthIndicator {

    private static final int DEFAULT_TIMEOUT = 30;

    private final EventHubMessageChannelBinder binder;

    private final EventHubClientFactory clientFactory;

    private EventHubProducerAsyncClient producerAsyncClient;

    private int timeout = DEFAULT_TIMEOUT;

    public EventHubHealthIndicator(EventHubMessageChannelBinder binder, EventHubClientFactory clientFactory) {
        this.binder = binder;
        this.clientFactory = clientFactory;
    }

    private synchronized void initProducerClient() {
        if (this.producerAsyncClient == null) {
            final Optional<String> eventHubName = binder.getEventHubsInUse().keySet().stream().findFirst();
            eventHubName.ifPresent(n -> this.producerAsyncClient = clientFactory.getOrCreateProducerClient(n));
        }
    }

    @Override
    public Health health() {
        if (binder.getEventHubsInUse().isEmpty()) {
            return Health.unknown().withDetail("No bindings found",
                "EventHubs binder may not be bound to destinations on the broker").build();
        }

        try {
            initProducerClient();
            synchronized (this.producerAsyncClient) {
                return producerAsyncClient.getEventHubProperties()
                                          .map(p -> Health.up().build())
                                          .block(Duration.ofSeconds(timeout));
            }
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
