// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventHubsHealthIndicatorTests {

    private static final String MOCK_URL = "https://example.org/bigly_fake_url";
    private EventHubProducerAsyncClient producerAsyncClient;
    private EventHubConsumerAsyncClient consumerAsyncClient;

    @BeforeEach
    void setUp() {
        producerAsyncClient = mock(EventHubProducerAsyncClient.class);
        consumerAsyncClient = mock(EventHubConsumerAsyncClient.class);
    }

    @Test
    void eventHubsIsUnknown() {
        EventHubsHealthIndicator indicator = new EventHubsHealthIndicator(null, null);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @Test
    void eventHubsIsUpWhenProducerGetEventHubPropertiesNormal() {
        EventHubProperties mockProperties = mock(EventHubProperties.class);
        Mockito.when(producerAsyncClient.getEventHubProperties()).thenReturn(Mono.just(mockProperties));

        EventHubsHealthIndicator indicator = new EventHubsHealthIndicator(producerAsyncClient, consumerAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void eventHubsIsUpWhenConsumerGetEventHubPropertiesNormal() {
        EventHubProperties mockProperties = mock(EventHubProperties.class);
        Mockito.when(consumerAsyncClient.getEventHubProperties()).thenReturn(Mono.just(mockProperties));
        EventHubsHealthIndicator indicator = new EventHubsHealthIndicator(null, consumerAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void eventHubsIsDownWhenProducerGetEventHubPropertiesAbnormal() {
        when(producerAsyncClient.getEventHubProperties())
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
        EventHubsHealthIndicator indicator = new EventHubsHealthIndicator(producerAsyncClient, consumerAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void eventHubsIsDownWhenConsumerGetEventHubPropertiesAbnormal() {
        when(consumerAsyncClient.getEventHubProperties())
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
        EventHubsHealthIndicator indicator = new EventHubsHealthIndicator(null, consumerAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
