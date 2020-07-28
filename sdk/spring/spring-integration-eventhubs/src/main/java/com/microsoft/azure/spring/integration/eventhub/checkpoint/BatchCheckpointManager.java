/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Do checkpoint after each batch. Effective when {@link CheckpointMode#BATCH}
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
@Deprecated
class BatchCheckpointManager extends CheckpointManager {
    private static final Logger log = LoggerFactory.getLogger(BatchCheckpointManager.class);
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

    @Override
    protected Logger getLogger() {
        return log;
    }
}
