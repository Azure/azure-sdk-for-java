// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Do checkpoint after each batch. Effective when {@link CheckpointMode#BATCH}
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public class BatchCheckpointManager extends CheckpointManager {
    private static final Logger LOG = LoggerFactory.getLogger(BatchCheckpointManager.class);
    private static final String CHECKPOINT_FAIL_MSG = "Consumer group '%s' failed to checkpoint offset %s of message "
        + "%s on partition %s, last checkpointed message is %s";
    private static final String CHECKPOINT_SUCCESS_MSG =
        "Consumer group '%s' checkpointed offset %s of message %s on partition %s in %s " + "mode";

    private final ConcurrentHashMap<String, EventData> lastEventByPartition = new ConcurrentHashMap<>();

    BatchCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH,
            () -> "BatchCheckpointManager should have checkpointMode batch");
    }

    @Override
    public void onMessage(EventContext context, EventData eventData) {
        // no-op
    }

    public void onMessages(EventBatchContext context) {
        completeBatch(context);

    }

    @Override
    public void completeBatch(EventBatchContext context) {
        EventData lastEvent = getLastEnqueuedEvent(context);
        Long offset = context.getLastEnqueuedEventProperties().getOffset();
        context.updateCheckpointAsync()
            .doOnError(t -> logCheckpointFail(context, offset, lastEvent,
                lastEventByPartition.get(context.getPartitionContext().getPartitionId()), t))
            .doOnSuccess(v -> {
                this.lastEventByPartition.put(context.getPartitionContext().getPartitionId(), lastEvent);
                logCheckpointSuccess(context, offset, lastEvent);
            })
            .subscribe();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    void logCheckpointFail(EventBatchContext context, Long offset, EventData lastEnqueuedEvent,
                           EventData lastCheckpointedEvent, Throwable t) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(String
                .format(CHECKPOINT_FAIL_MSG, context.getPartitionContext().getConsumerGroup(), offset,
                    lastEnqueuedEvent, lastCheckpointedEvent, context.getPartitionContext().getPartitionId()), t);
        }
    }

    void logCheckpointSuccess(EventBatchContext context, Long offset, EventData lastEnqueuedEvent) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String
                .format(CHECKPOINT_SUCCESS_MSG, context.getPartitionContext().getConsumerGroup(), offset,
                    lastEnqueuedEvent, context.getPartitionContext().getPartitionId(),
                    this.checkpointConfig.getCheckpointMode()));
        }
    }

    EventData getLastEnqueuedEvent(EventBatchContext context) {
        List<EventData> events = context.getEvents();
        return events.get(events.size() - 1);

    }
}
