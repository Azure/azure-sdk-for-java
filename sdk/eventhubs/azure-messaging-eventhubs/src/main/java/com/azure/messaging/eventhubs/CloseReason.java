// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;

/**
 * Enumeration of all possible reasons a {@link PartitionProcessor} may be closed.
 */
public enum CloseReason {
    /**
     * If another event processor instance stole the ownership of a partition, this reason will be provided to {@link
     * PartitionProcessor#close(PartitionContext, CloseReason)}.
     */
    LOST_PARTITION_OWNERSHIP,

    /**
     * If the event processor is shutting down by calling {@link EventProcessor#stop()}, the {@link
     * PartitionProcessor#close(PartitionContext, CloseReason)} will be called with this reason.
     */
    EVENT_PROCESSOR_SHUTDOWN,

    /**
     * If a non-retryable exception occured when receiving events from Event Hub, this reason will be provided when
     * {@link PartitionProcessor#close(PartitionContext, CloseReason)} is called.
     */
    EVENT_HUB_EXCEPTION
}
