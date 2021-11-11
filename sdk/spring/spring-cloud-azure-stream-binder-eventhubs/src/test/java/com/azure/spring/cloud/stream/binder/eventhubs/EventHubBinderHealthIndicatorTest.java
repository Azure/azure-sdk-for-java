// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.EventHubProperties;
import com.azure.spring.eventhubs.core.producer.BatchableProducerAsyncClient;
import com.azure.spring.eventhubs.core.producer.EventHubProducerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubBinderHealthIndicatorTest {

    @Mock
    private EventHubMessageChannelBinder binder;

    @Mock
    private EventHubProducerFactory producerFactory;

    @Mock
    private BatchableProducerAsyncClient producerAsyncClient;

    private EventHubHealthIndicator healthIndicator;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        when(producerFactory.createProducer(anyString())).thenReturn(producerAsyncClient);
        healthIndicator = new EventHubHealthIndicator(binder, producerFactory);
    }

    @Test
    public void testNoEventHubsInUse() {
        when(binder.getEventHubsInUse()).thenReturn(new HashMap<>());
        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @Test
    public void testEventHubIsUp() {
        Map<String, EventHubMessageChannelBinder.EventHubInformation> eventHubsInUse = new HashMap<>();
        eventHubsInUse.put("eventhub-1", new EventHubMessageChannelBinder.EventHubInformation(null));

        EventHubProperties eventHubProperties = mock(EventHubProperties.class);

        when(binder.getEventHubsInUse()).thenReturn(eventHubsInUse);
        when(producerAsyncClient.getEventHubProperties()).thenReturn(Mono.just(eventHubProperties));

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void testEventHubIsDown() {
        Map<String, EventHubMessageChannelBinder.EventHubInformation> eventHubsInUse = new HashMap<>();
        eventHubsInUse.put("eventhub-1", new EventHubMessageChannelBinder.EventHubInformation(null));

        when(binder.getEventHubsInUse()).thenReturn(eventHubsInUse);
        when(producerAsyncClient.getEventHubProperties()).thenThrow(new IllegalStateException());

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @Timeout(5)
    public void testGetEventHubInfoTimeout() {
        healthIndicator.setTimeout(1);

        Map<String, EventHubMessageChannelBinder.EventHubInformation> eventHubsInUse = new HashMap<>();
        eventHubsInUse.put("eventhub-1", new EventHubMessageChannelBinder.EventHubInformation(null));

        EventHubProperties eventHubProperties = mock(EventHubProperties.class);

        when(binder.getEventHubsInUse()).thenReturn(eventHubsInUse);
        when(producerAsyncClient.getEventHubProperties()).then(e -> {
            Thread.sleep(2_000L);
            return eventHubProperties;
        });

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

}
