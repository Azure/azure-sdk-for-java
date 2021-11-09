// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.batch;

import java.time.Duration;

/**
 * Batch sending related config.
 */
public class BatchSendingConfig {

    /**
     * The maximum size, in bytes, of a batch of events. The default value is 256 * 1024
     */
    private int maxSizeInBytes;

    /**
     * The maximum time duration to wait to send a batch of events when maxSizeInBytes is not reached. The default value
     * is 5min.
     */
    private Duration maxWaitTime;

    /**
     * Whether to enable batch sending mode.
     */
    private boolean batchMode;

    public BatchSendingConfig() {
        this.maxSizeInBytes = 256 * 1024;
        this.maxWaitTime = Duration.ofMinutes(5);
        this.batchMode = false;
    }

    public int getMaxSizeInBytes() {
        return maxSizeInBytes;
    }

    public void setMaxSizeInBytes(int maxSizeInBytes) {
        this.maxSizeInBytes = maxSizeInBytes;
    }

    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public boolean isBatchMode() {
        return batchMode;
    }

    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }
}
