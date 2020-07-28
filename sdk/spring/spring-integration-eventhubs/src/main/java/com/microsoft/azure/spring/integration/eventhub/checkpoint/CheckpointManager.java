/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.eventhub.util.EventDataHelper;
import org.slf4j.Logger;

/**
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public abstract class CheckpointManager {
    private static final String CHECKPOINT_FAIL_MSG = "Consumer group '%s' failed to checkpoint %s on partition %s";
    private static final String CHECKPOINT_SUCCESS_MSG =
            "Consumer group '%s' checkpointed %s on partition %s in %s " + "mode";
    final CheckpointConfig checkpointConfig;

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
        }

        throw new IllegalArgumentException("Illegal checkpoint mode when building CheckpointManager");
    }

    CheckpointManager(CheckpointConfig checkpointConfig) {
        this.checkpointConfig = checkpointConfig;
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
