// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.checkpoint;

import java.time.Duration;

/**
 * Checkpoint related config.
 */
public class CheckpointConfig {

    /**
     * Checkpoint mode used when consumer decide how to checkpoint message
     */
    private CheckpointMode mode;
    /**
     * Effectively only when {@link CheckpointMode#PARTITION_COUNT}. Decides the amount of message for each partition to
     * do one checkpoint
     */
    private Integer count;

    /**
     * Effectively only when {@link CheckpointMode#TIME}. Decides the time interval to do one checkpoint
     */
    private Duration interval;


    public CheckpointConfig() {
        this(CheckpointMode.RECORD, null, null);
    }

    public CheckpointConfig(CheckpointMode mode) {
        this(mode, null, null);
    }

    public CheckpointConfig(CheckpointMode mode, Integer count) {
        this(mode, count, null);
    }

    public CheckpointConfig(CheckpointMode mode, Duration duration) {
        this(mode, null, duration);
    }

    public CheckpointConfig(CheckpointMode mode, Integer count, Duration interval) {
        this.mode = mode;
        this.count = count;
        this.interval = interval;
    }

    public CheckpointMode getMode() {
        return mode;
    }

    public void setMode(CheckpointMode mode) {
        this.mode = mode;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Duration getInterval() {
        return interval;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }
}
