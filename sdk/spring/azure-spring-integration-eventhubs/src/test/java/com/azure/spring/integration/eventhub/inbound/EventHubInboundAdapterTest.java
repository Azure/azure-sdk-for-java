// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.inbound;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.integration.eventhub.factory.DefaultEventHubClientFactory;
import com.azure.spring.integration.eventhub.support.EventHubTestOperation;
import com.azure.spring.integration.test.support.InboundChannelAdapterTest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class EventHubInboundAdapterTest extends InboundChannelAdapterTest<EventHubInboundChannelAdapter> {

    @Mock
    EventContext partitionContext;

    @Mock
    CheckpointStore checkpointStore;

    @Mock
    DefaultEventHubClientFactory defaultEventHubClientFactory;

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.defaultEventHubClientFactory.getMeterRegistry()).thenReturn(this.meterRegistry);
        this.adapter = new EventHubInboundChannelAdapter(destination,
            new EventHubTestOperation(defaultEventHubClientFactory, () -> partitionContext), consumerGroup);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }
}
