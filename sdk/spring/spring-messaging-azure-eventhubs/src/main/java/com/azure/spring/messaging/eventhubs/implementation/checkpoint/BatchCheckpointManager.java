// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Do checkpoint after each batch. Effective when {@link CheckpointMode#BATCH}
 */
class BatchCheckpointManager extends EventCheckpointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchCheckpointManager.class);
    private static final String CHECKPOINT_FAIL_MSG = "Consumer group '%s' failed to checkpoint offset %s of message "
        + "on partition %s in batch mode";
    private static final String CHECKPOINT_SUCCESS_MSG =
        "Consumer group '%s' succeed to checkpoint offset %s of message on partition %s in batch mode";

    BatchCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(checkpointConfig.getMode() == CheckpointMode.BATCH,
            () -> "BatchCheckpointManager should have checkpointMode batch");
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    void logCheckpointFail(String consumerGroup, String partitionId, Long offset, Throwable t) {
        getLogger().warn(String
            .format(CHECKPOINT_FAIL_MSG, consumerGroup, offset, partitionId), t);
    }

    void logCheckpointSuccess(String consumerGroup, String partitionId, Long offset) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String
                .format(CHECKPOINT_SUCCESS_MSG, consumerGroup, offset, partitionId));
        }
    }

    @Override
    public void checkpoint(EventBatchContext context) {
        EventData lastEvent = getLastEventFromBatch(context);
        if (lastEvent == null) {
            return;
        }
        Long offset = lastEvent.getOffset();
        String partitionId = context.getPartitionContext().getPartitionId();
        String consumerGroup = context.getPartitionContext().getConsumerGroup();
        context.updateCheckpointAsync()
            .doOnError(t -> logCheckpointFail(consumerGroup, partitionId, offset, t))
            .doOnSuccess(v -> logCheckpointSuccess(consumerGroup, partitionId, offset))
            .block();
    }

    private EventData getLastEventFromBatch(EventBatchContext context) {
        List<EventData> events = context.getEvents();
        if (CollectionUtils.isEmpty(events)) {
            return null;
        }
        return events.get(events.size() - 1);
    }
}
