// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.eventhubs.support.EventDataHelper;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public abstract class CheckpointManager {
    private static final Logger LOG = LoggerFactory.getLogger(CheckpointManager.class);
    private static final String CHECKPOINT_FAIL_MSG = "Consumer group '%s' failed to checkpoint %s on partition %s";
    private static final String CHECKPOINT_SUCCESS_MSG =
        "Consumer group '%s' checkpointed %s on partition %s in %s " + "mode";
    final CheckpointConfig checkpointConfig;

    CheckpointManager(CheckpointConfig checkpointConfig) {
        this.checkpointConfig = checkpointConfig;
    }

    /**
     * Mapping checkpoint mode in config to a {@link CheckpointManager}.
     * @param checkpointConfig The configured checkpoint config.
     * @return CheckpointManager.
     * @throws IllegalArgumentException If no checkpoint manager could be found.
     */
    public static CheckpointManager of(CheckpointConfig checkpointConfig) {
        switch (checkpointConfig.getCheckpointMode()) {
            case TIME:
                return new TimeCheckpointManager(checkpointConfig);
            case RECORD:
                return new RecordCheckpointManager(checkpointConfig);
            case BATCH:
                return new BatchCheckpointManager(checkpointConfig);
            case PARTITION_COUNT:
                return new PartitionCountCheckpointManager(checkpointConfig);
            case MANUAL:
                return new ManualCheckpointManager(checkpointConfig);
            default:
                LOG.warn("Does not support checkpoint mode: "
                    + checkpointConfig.getCheckpointMode().name());
        }

        throw new IllegalArgumentException("Illegal checkpoint mode when building CheckpointManager");
    }

    public void onMessage(EventContext context, EventData eventData) {
        // no-op
    }

    @Deprecated
    public void completeBatch(EventContext context) {
        // no-op
    }

    void logCheckpointFail(EventContext context, EventData eventData, Throwable t) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(String
                .format(CHECKPOINT_FAIL_MSG, context.getPartitionContext().getConsumerGroup(),
                    EventDataHelper.toString(eventData),
                    context.getPartitionContext().getPartitionId()), t);
        }
    }

    void logCheckpointSuccess(EventContext context, EventData eventData) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String
                .format(CHECKPOINT_SUCCESS_MSG, context.getPartitionContext().getConsumerGroup(),
                    EventDataHelper.toString(eventData),
                    context.getPartitionContext().getPartitionId(),
                    this.checkpointConfig.getCheckpointMode()));
        }
    }

    abstract Logger getLogger();
}
