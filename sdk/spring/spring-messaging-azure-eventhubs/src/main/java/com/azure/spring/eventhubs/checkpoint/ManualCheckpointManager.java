// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.checkpoint;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * No need to do checkpoint in manual mode. Effective when {@link CheckpointMode#MANUAL}
 *
 */
public class ManualCheckpointManager extends EventCheckpointManager {
    private static final Logger LOG = LoggerFactory.getLogger(ManualCheckpointManager.class);

    ManualCheckpointManager(CheckpointConfig checkpointConfig) {
        super(checkpointConfig);
        Assert.isTrue(this.checkpointConfig.getMode() == CheckpointMode.MANUAL,
            () -> "ManualCheckpointManager should have checkpointMode manual");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public void checkpoint(EventContext context) {

    }
}
