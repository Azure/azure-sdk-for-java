// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.checkpoint;

import java.time.Duration;

/**
 * Checkpoint config to describe different checkpoint strategies.
 */
public class CheckpointConfig {

    /**
     * Checkpoint mode used when consumer decide how to checkpoint message.
     */
    private CheckpointMode mode;
    /**
     * Effectively only when {@link CheckpointMode#PARTITION_COUNT}. Decides the amount of messages for each partition to
     * do one checkpoint.
     */
    private Integer count;

    /**
     * Effectively only when {@link CheckpointMode#TIME}. Decides the time interval to do one checkpoint.
     */
    private Duration interval;

    /**
     * Construct a {@link CheckpointConfig} with the {@link CheckpointMode#RECORD} mode.
     */
    public CheckpointConfig() {
        this(CheckpointMode.RECORD, null, null);
    }

    /**
     * Construct a {@link CheckpointConfig} with the specified {@link CheckpointMode} mode.
     * @param mode the checkpoint mode.
     */
    public CheckpointConfig(CheckpointMode mode) {
        this(mode, null, null);
    }

    /**
     * Construct a {@link CheckpointConfig} with the specified {@link CheckpointMode} mode and the amount of
     * messages for each partition to do one checkpoint.
     * @param mode the specified {@link CheckpointMode} mode.
     * @param count the amount of messages for each partition to do one checkpoint, it will be effective only when
     * {@link CheckpointMode#PARTITION_COUNT} is configured.
     */
    public CheckpointConfig(CheckpointMode mode, Integer count) {
        this(mode, count, null);
    }

    /**
     * Construct a {@link CheckpointConfig} with the specified {@link CheckpointMode} mode and the time interval
     * to do one checkpoint.
     * @param mode the specified {@link CheckpointMode} mode.
     * @param duration the time interval to do one checkpoint, it will be effective only when
     * {@link CheckpointMode#TIME} is configured.
     */
    public CheckpointConfig(CheckpointMode mode, Duration duration) {
        this(mode, null, duration);
    }

    /**
     * Construct a {@link CheckpointConfig} with the specified {@link CheckpointMode} mode, the amount of
     * messages for each partition to do one checkpoint and the time interval to do one checkpoint.
     * @param mode the specified {@link CheckpointMode} mode.
     * @param count the amount of messages for each partition to do one checkpoint, it will be effective only when
     * {@link CheckpointMode#PARTITION_COUNT} is configured.
     * @param interval the time interval to do one checkpoint, it will be effective only when
     * {@link CheckpointMode#TIME} is configured.
     */
    public CheckpointConfig(CheckpointMode mode, Integer count, Duration interval) {
        this.mode = mode;
        this.count = count;
        this.interval = interval;
    }

    /**
     * Get the checkpoint mode.
     * @return the checkpoint mode.
     */
    public CheckpointMode getMode() {
        return mode;
    }

    /**
     * Set the checkpoint mode.
     * @param mode the checkpoint mode.
     */
    public void setMode(CheckpointMode mode) {
        this.mode = mode;
    }

    /**
     * Get the amount of message for each partition to do one checkpoint.
     * @return the amount of message for each partition to do one checkpoint.
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Set the amount of message for each partition to do one checkpoint.
     * @param count the amount of message for each partition to do one checkpoint.
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Get the time interval to do one checkpoint.
     * @return the time interval to do one checkpoint.
     */
    public Duration getInterval() {
        return interval;
    }

    /**
     * Set the time interval to do one checkpoint.
     * @param interval the time interval to do one checkpoint.
     */
    public void setInterval(Duration interval) {
        this.interval = interval;
    }
}
