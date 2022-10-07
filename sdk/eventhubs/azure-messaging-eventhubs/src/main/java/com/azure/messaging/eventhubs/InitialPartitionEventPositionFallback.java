// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;

/**
 * The Fallback used when the {@link PartitionPumpManager} has no checkpoint and the Partition is not set in the
 * initialPartitionEventPosition map
 */
public enum InitialPartitionEventPositionFallback {
    /**
     * {@link EventPosition} latest()
     */
    LATEST,

    /**
     * {@link EventPosition} earliest()
     */
    EARLIEST
}
