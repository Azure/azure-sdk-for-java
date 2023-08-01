// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.messaging.eventhubs.implementation.support.EventDataHelper;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import org.slf4j.Logger;

/**
 *
 */
public abstract class EventCheckpointManager implements EventCheckpoint, EventBatchCheckpoint {
    private static final String CHECKPOINT_FAIL_MSG = "Consumer group '%s' failed to checkpoint %s on partition %s";
    private static final String CHECKPOINT_SUCCESS_MSG =
        "Consumer group '%s' checkpointed %s on partition %s in %s " + "mode";
    final CheckpointConfig checkpointConfig;

    EventCheckpointManager(CheckpointConfig checkpointConfig) {
        this.checkpointConfig = checkpointConfig;
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
                    this.checkpointConfig.getMode()));
        }
    }

    @Override
    public void checkpoint(EventContext context) {
    }

    @Override
    public void checkpoint(EventBatchContext context) {
    }

    abstract Logger getLogger();
}
