// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.eventhubs.EventData;

@Immutable
public class PartitionEvent {
    private final PartitionContext partitionContext;
    private final EventData eventData;

    public PartitionEvent(PartitionContext partitionContext, EventData eventData) {
        this.partitionContext = partitionContext;
        this.eventData = eventData;
    }

    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    public EventData getEventData() {
        return eventData;
    }
}
