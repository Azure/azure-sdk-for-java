// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.inbound;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.integration.eventhub.support.EventHubTestOperation;
import com.azure.spring.integration.test.support.InboundChannelAdapterTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventHubInboundAdapterTest extends InboundChannelAdapterTest<EventHubInboundChannelAdapter> {

    @Mock
    EventContext partitionContext;

    @Mock
    CheckpointStore checkpointStore;

    @BeforeAll
    @Override
    public void setUp() {
        this.adapter = new EventHubInboundChannelAdapter(destination, new EventHubTestOperation(null,
            () -> partitionContext), consumerGroup);
    }
}
