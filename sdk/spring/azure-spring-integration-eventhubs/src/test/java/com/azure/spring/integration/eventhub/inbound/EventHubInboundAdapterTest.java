// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.inbound;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.integration.eventhub.support.EventHubTestOperation;
import com.azure.spring.integration.test.support.InboundChannelAdapterTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventHubInboundAdapterTest extends InboundChannelAdapterTest<EventHubInboundChannelAdapter> {

    @Mock
    EventContext partitionContext;

    @Mock
    CheckpointStore checkpointStore;

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.adapter = new EventHubInboundChannelAdapter(destination, new EventHubTestOperation(null,
            () -> partitionContext),
            consumerGroup);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }
}
