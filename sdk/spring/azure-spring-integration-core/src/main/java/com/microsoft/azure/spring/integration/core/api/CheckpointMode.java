// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api;

/**
 * The Checkpoint mode.
 *
 * @author Warren Zhu
 */
public enum CheckpointMode {

    /**
     * Checkpoint after each processed record.
     * Makes sense only if {@link ListenerMode#RECORD} is used.
     */
    RECORD,

    /**
     * Checkpoint after each processed batch of records.
     * @deprecated Please use {@code RECORD}
     */
    @Deprecated
    BATCH,

    /**
     * User decide when to checkpoint manually
     */
    MANUAL,

    /**
     * Checkpoint once for number of message specified by {@link CheckpointConfig#getCheckpointCount()}
     * in each partition
     */
    PARTITION_COUNT,

    /**
     * Checkpoint once for each time interval specified by {@link CheckpointConfig#getCheckpointInterval()}
     */
    TIME,
}
