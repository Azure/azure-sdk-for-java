/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.microsoft.azure.spring.integration.eventhub.support.EventHubTestOperation;
import com.microsoft.azure.spring.integration.test.support.InboundChannelAdapterTest;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventHubInboundAdapterTest extends InboundChannelAdapterTest<EventHubInboundChannelAdapter> {

    @Mock
    EventContext partitionContext;

    @Mock
    CheckpointStore checkpointStore;

    @Override
    public void setUp() {
        this.adapter = new EventHubInboundChannelAdapter(destination, new EventHubTestOperation(null,
                () -> partitionContext), consumerGroup);
    }
}
