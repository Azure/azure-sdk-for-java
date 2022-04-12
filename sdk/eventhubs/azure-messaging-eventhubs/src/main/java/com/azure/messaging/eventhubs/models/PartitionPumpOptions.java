// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import java.util.Objects;

/**
 * Set of options that can be specified when build each partition pump.
 */
public class PartitionPumpOptions {
    private int schedulerSize;
    private int maxQueueSize;

    private static final int MAXIMUM_QUEUE_SIZE = 10000;

    public PartitionPumpOptions() {
        this.schedulerSize = Runtime.getRuntime().availableProcessors() * 4;
        this.maxQueueSize = MAXIMUM_QUEUE_SIZE;
    }

    public PartitionPumpOptions(PartitionPumpOptions options) {
        Objects.requireNonNull(options, "'partitionPumpOptions' cannot be null.");
        this.schedulerSize = options.getSchedulerSize();
        this.maxQueueSize = options.getMaxQueueSize();
    }

    public int getSchedulerSize() {
        return schedulerSize;
    }

    public void setSchedulerSize(int schedulerSize) {
        this.schedulerSize = schedulerSize;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }
}
