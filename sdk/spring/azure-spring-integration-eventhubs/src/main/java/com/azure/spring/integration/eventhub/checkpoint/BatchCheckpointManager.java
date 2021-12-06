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
import org.springframework.util.CollectionUtils;

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
        + "on partition %s in batch mode";
    private static final String CHECKPOINT_SUCCESS_MSG =
        "Consumer group '%s' succeed to checkpoint offset %s of message on partition %s in batch mode";

    private final ConcurrentHashMap<String, EventData> lastEventByPartition = new ConcurrentHashMap<>();

    BatchCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH,
            () -> "BatchCheckpointManager should have checkpointMode batch");
    }

    @Override
    public void onMessage(EventContext context, EventData eventData) {
        this.lastEventByPartition.put(context.getPartitionContext().getPartitionId(), eventData);
    }

    @Override
    public void completeBatch(EventContext context) {
        EventData eventData = this.lastEventByPartition.get(context.getPartitionContext().getPartitionId());

        context.updateCheckpointAsync()
               .doOnError(t -> logCheckpointFail(context, eventData, t))
               .doOnSuccess(v -> logCheckpointSuccess(context, eventData))
               .subscribe();
    }

    public void onMessages(EventBatchContext context) {
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
            .subscribe();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
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

    private EventData getLastEventFromBatch(EventBatchContext context) {
        List<EventData> events = context.getEvents();
        if (CollectionUtils.isEmpty(events)) {
            return null;
        }
        EventData lastEvent = events.get(events.size() - 1);
        return lastEvent;

    }
}
