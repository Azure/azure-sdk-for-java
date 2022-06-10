// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.checkpoint;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Do checkpoint when the time since last successful checkpoint exceeds {@link CheckpointConfig#getInterval()} ()}
 * for one partition. Effective when {@link CheckpointMode#PARTITION_COUNT}
 *
 */
class TimeCheckpointManager extends EventCheckpointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeCheckpointManager.class);
    private final AtomicReference<LocalDateTime> lastCheckpointTime = new AtomicReference<>(LocalDateTime.now());

    TimeCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getMode() == CheckpointMode.TIME,
            () -> "TimeCheckpointManager should have checkpointMode time");
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void checkpoint(EventContext context) {
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(this.lastCheckpointTime.get(), now)
            .compareTo(this.checkpointConfig.getInterval()) > 0) {
            context.updateCheckpointAsync()
                .doOnError(t -> logCheckpointFail(context, context.getEventData(), t))
                .doOnSuccess(v -> {
                    logCheckpointSuccess(context, context.getEventData());
                    lastCheckpointTime.set(now);
                })
                .block();
        }
    }
}
