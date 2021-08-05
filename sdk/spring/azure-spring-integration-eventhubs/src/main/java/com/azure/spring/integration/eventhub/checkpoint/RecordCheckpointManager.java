// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.checkpoint;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Do checkpoint after each message successfully processed.
 * Effective when {@link CheckpointMode#RECORD}
 *
 * @author Warren Zhu
 */
class RecordCheckpointManager extends CheckpointManager {
    private static final Logger LOG = LoggerFactory.getLogger(RecordCheckpointManager.class);

    RecordCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD,
            () -> "RecordCheckpointManager should have checkpointMode record");
    }

    public void onMessage(EventContext context, EventData eventData) {
        context.updateCheckpointAsync()
            .doOnError(t -> logCheckpointFail(context, eventData, t))
            .doOnSuccess(v -> logCheckpointSuccess(context, eventData))
            .subscribe();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
