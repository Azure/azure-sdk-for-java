// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.properties;

import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.eventhubs.support.StartPosition;

import java.time.Duration;

/**
 * @author Warren Zhu
 */
public class EventHubConsumerProperties {
    /**
     * Whether the consumer receives messages from the beginning or end of event hub.
     * If {@link StartPosition#EARLIEST}, from beginning. If {@link StartPosition#LATEST}, from end.
     * <p>
     * Default: {@link StartPosition#LATEST}
     */
    private StartPosition startPosition = StartPosition.LATEST;

    /**
     * Checkpoint mode used when consumer decide how to checkpoint message
     * <p>
     * Default: {@link CheckpointMode#BATCH}
     */
    private CheckpointMode checkpointMode = CheckpointMode.BATCH;

    /**
     * Effectively only when {@link CheckpointMode#PARTITION_COUNT}.
     * Decides the amount of message for each partition to do one checkpoint
     *
     * <p>
     * Default : 10
     */
    private int checkpointCount = 10;

    /**
     * Effectively only when {@link CheckpointMode#TIME}.
     * Decides the time interval to do one checkpoint
     *
     * <p>
     * Default : 5s
     */
    private Duration checkpointInterval = Duration.ofSeconds(5);

    public StartPosition getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(StartPosition startPosition) {
        this.startPosition = startPosition;
    }

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    public int getCheckpointCount() {
        return checkpointCount;
    }

    public void setCheckpointCount(int checkpointCount) {
        this.checkpointCount = checkpointCount;
    }

    public Duration getCheckpointInterval() {
        return checkpointInterval;
    }

    public void setCheckpointInterval(Duration checkpointInterval) {
        this.checkpointInterval = checkpointInterval;
    }
}
