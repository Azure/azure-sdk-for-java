// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Do checkpoint after each batch. Effective when {@link CheckpointMode#BATCH}
 */
public class BatchCheckpointManager extends EventCheckpointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchCheckpointManager.class);
    private static final String CHECKPOINT_FAIL_MSG = "Consumer group '%s' failed to checkpoint %s messages "
        + "on partition %s";
    private static final String CHECKPOINT_SUCCESS_MSG =
        "Consumer group '%s' succeed to checkpoint %s messages %s on partition %s in batch mode";

    private final ConcurrentHashMap<String, EventData> lastEventByPartition = new ConcurrentHashMap<>();

    BatchCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(checkpointConfig.getMode() == CheckpointMode.BATCH,
            () -> "BatchCheckpointManager should have checkpointMode batch");
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    void logCheckpointFail(EventBatchContext context, Throwable t) {
        getLogger().warn(String
            .format(CHECKPOINT_FAIL_MSG, context.getPartitionContext().getConsumerGroup(), context.getEvents().size(),
                context.getPartitionContext().getPartitionId()), t);
    }

    void logCheckpointSuccess(EventBatchContext context) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String
                .format(CHECKPOINT_SUCCESS_MSG, context.getPartitionContext().getConsumerGroup(),
                    context.getEvents().size(), context.getPartitionContext().getPartitionId()));
        }
    }

    EventData getLastEnqueuedEvent(EventBatchContext context) {
        List<EventData> events = context.getEvents();
        return events.get(events.size() - 1);

    }

    @Override
    public void checkpoint(EventBatchContext context) {
        EventData lastEvent = getLastEnqueuedEvent(context);
        context.updateCheckpointAsync()
            .doOnError(t -> logCheckpointFail(context, t))
            .doOnSuccess(v -> {
                this.lastEventByPartition.put(context.getPartitionContext().getPartitionId(), lastEvent);
                logCheckpointSuccess(context);
            })
            .subscribe();
    }
}
