// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import java.time.Duration;

/**
 * Batch consumer config.
 */
public class BatchConfig {

    /**
     * The maximum number of events that will be in the list when this callback is invoked.
     */
    private final int batchSize;

    /**
     * The max time duration to wait to receive a batch of events upto the max batch size before.
     * invoking this callback.
     */
    private final Duration maxWaitTime;

    public BatchConfig(int batchSize, Duration maxWaitTime) {
        this.batchSize = batchSize;
        this.maxWaitTime = maxWaitTime;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public static BatchConfigBuilder builder() {
        return new BatchConfigBuilder();
    }

    public static class BatchConfigBuilder {
        private int batchSize;

        private Duration maxWaitTime;

        public BatchConfigBuilder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public BatchConfigBuilder maxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
            return this;
        }

        public BatchConfig build() {
            return new BatchConfig(this.batchSize, this.maxWaitTime);
        }
    }
}
