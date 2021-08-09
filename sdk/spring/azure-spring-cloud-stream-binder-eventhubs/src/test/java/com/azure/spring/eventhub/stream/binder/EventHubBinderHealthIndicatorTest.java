// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProperties;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
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
    private EventHubClientFactory clientFactory;

    @Mock
    private EventHubProducerAsyncClient producerAsyncClient;

    private EventHubHealthIndicator healthIndicator;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        when(clientFactory.getOrCreateProducerClient(anyString())).thenReturn(producerAsyncClient);
        healthIndicator = new EventHubHealthIndicator(binder, clientFactory);
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
