// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.checkpoint;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Do checkpoint after each message successfully processed.
 * Effective when {@link CheckpointMode#RECORD}
 *
 */
class RecordCheckpointManager extends EventCheckpointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordCheckpointManager.class);

    RecordCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getMode() == CheckpointMode.RECORD,
            () -> "RecordCheckpointManager should have checkpointMode record");
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }


    @Override
    public void checkpoint(EventContext context) {
        context.updateCheckpointAsync()
            .doOnError(t -> logCheckpointFail(context, context.getEventData(), t))
            .doOnSuccess(v -> logCheckpointSuccess(context, context.getEventData()))
            .block();
    }
}
