// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.checkpoint;

import com.azure.spring.messaging.ListenerMode;

/**
 * The offset commit behavior enumeration.
 */
public enum CheckpointMode {

    /**
     * Checkpoint after each processed record.
     * Makes sense only if {@link ListenerMode#RECORD} is used.
     */
    RECORD,

    /**
     * Checkpoint after each processed batch of records.
     */
    BATCH,

    /**
     * User decide when to checkpoint manually
     */
    MANUAL,

    /**
     * Checkpoint once for number of message specified by {@link CheckpointConfig#getCount()} ()}
     * in each partition
     */
    PARTITION_COUNT,

    /**
     * Checkpoint once for each time interval specified by {@link CheckpointConfig#getInterval()} ()}
     */
    TIME,
}
