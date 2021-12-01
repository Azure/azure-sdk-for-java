// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.properties;

import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.StartPosition;

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

    /**
     * Effectively only when spring.cloud.stream.binding.&lt;name&gt;.consumer.batch-mode is set to true.
     * Decides the maximum number of events that will be in the message payload {@link java.util.List} when the consumer callback is invoked.
     * It's required for the batching consumer mode.
     *
     * <p>
     * Default : 10
     */
    private int maxBatchSize = 10;

    /**
     * Effectively only when spring.cloud.stream.binding.&lt;name&gt;.consumer.batch-mode is set to true.
     * Decides the max time duration to wait to receive a batch of events upto the max batch size before invoking the consumer callback.
     * It's optional for the batching consumer mode.
     *
     * <p>
     * Default : null
     */
    private Duration maxWaitTime = null;

    /**
     *
     * @return The start position.
     */
    public StartPosition getStartPosition() {
        return startPosition;
    }

    /**
     *
     * @param startPosition The start position.
     */
    public void setStartPosition(StartPosition startPosition) {
        this.startPosition = startPosition;
    }

    /**
     *
     * @return The check point mode.
     */
    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    /**
     *
     * @param checkpointMode The check point mode.
     */
    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    /**
     *
     * @return The check point mode.
     */
    public int getCheckpointCount() {
        return checkpointCount;
    }

    /**
     *
     * @param checkpointCount The check point mode.
     */
    public void setCheckpointCount(int checkpointCount) {
        this.checkpointCount = checkpointCount;
    }

    /**
     *
     * @return The check point interval.
     */
    public Duration getCheckpointInterval() {
        return checkpointInterval;
    }

    /**
     *
     * @param checkpointInterval The check point interval.
     */
    public void setCheckpointInterval(Duration checkpointInterval) {
        this.checkpointInterval = checkpointInterval;
    }

    /**
     *
     * @return The max batch size.
     */
    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    /**
     *
     * @param maxBatchSize The max batch size.
     */
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    /**
     *
     * @return The max wait time.
     */
    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     *
     * @param maxWaitTime The max wait time.
     */
    public void setMaxWaitTime(Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }
}
