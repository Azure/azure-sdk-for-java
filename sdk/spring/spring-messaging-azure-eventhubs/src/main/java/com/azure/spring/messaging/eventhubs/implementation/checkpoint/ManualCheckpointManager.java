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
 * No need to do checkpoint in manual mode. Effective when {@link CheckpointMode#MANUAL}
 *
 */
class ManualCheckpointManager extends EventCheckpointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManualCheckpointManager.class);

    ManualCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getMode() == CheckpointMode.MANUAL,
            () -> "ManualCheckpointManager should have checkpointMode manual");
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void checkpoint(EventContext context) {
        // Do nothing.
    }
}
