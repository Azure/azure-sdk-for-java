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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Do checkpoint when the time since last successful checkpoint exceeds {@link CheckpointConfig#getCheckpointInterval()}
 * for one partition. Effective when {@link CheckpointMode#PARTITION_COUNT}
 *
 * @author Warren Zhu
 */
class TimeCheckpointManager extends CheckpointManager {
    private static final Logger LOG = LoggerFactory.getLogger(TimeCheckpointManager.class);
    private final AtomicReference<LocalDateTime> lastCheckpointTime = new AtomicReference<>(LocalDateTime.now());

    TimeCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getCheckpointMode() == CheckpointMode.TIME,
            () -> "TimeCheckpointManager should have checkpointMode time");
    }

    public void onMessage(EventContext context, EventData eventData) {
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(this.lastCheckpointTime.get(), now)
            .compareTo(this.checkpointConfig.getCheckpointInterval()) > 0) {
            context.updateCheckpointAsync()
                .doOnError(t -> logCheckpointFail(context, eventData, t))
                .doOnSuccess(v -> {
                    logCheckpointSuccess(context, eventData);
                    lastCheckpointTime.set(now);
                })
                .subscribe();
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
