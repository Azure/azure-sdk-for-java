// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.checkpoint;

import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class CheckpointManagers {
    private static final Logger LOG = LoggerFactory.getLogger(CheckpointManagers.class);

    private CheckpointManagers() {
    }

    /**
     * Mapping checkpoint mode in config to a {@link EventCheckpointManager}.
     *
     * @param checkpointConfig The configured checkpoint config.
     * @param listenerMode The configured consumer listener config.
     * @return CheckpointManager.
     * @throws IllegalArgumentException If no checkpoint manager could be found.
     */
    public static EventCheckpointManager of(CheckpointConfig checkpointConfig, ListenerMode listenerMode) {
        if (listenerMode == ListenerMode.BATCH) {
            switch (checkpointConfig.getMode()) {
                case BATCH:
                    return new BatchCheckpointManager(checkpointConfig);
                case MANUAL:
                    return new ManualCheckpointManager(checkpointConfig);
                default:
                    throw new IllegalArgumentException("Illegal checkpoint mode when building "
                        + "CheckpointManager in batch consuming mode!");
            }
        } else {
            switch (checkpointConfig.getMode()) {
                case TIME:
                    return new TimeCheckpointManager(checkpointConfig);
                case RECORD:
                    return new RecordCheckpointManager(checkpointConfig);
                case PARTITION_COUNT:
                    return new PartitionCountCheckpointManager(checkpointConfig);
                case MANUAL:
                    return new ManualCheckpointManager(checkpointConfig);
                default:
                    LOG.warn("Does not support checkpoint mode:{} while consume mode is:{} ",
                        checkpointConfig.getMode().name(), listenerMode);
            }
        }

        throw new IllegalArgumentException("Illegal checkpoint mode when building CheckpointManager");
    }

}
