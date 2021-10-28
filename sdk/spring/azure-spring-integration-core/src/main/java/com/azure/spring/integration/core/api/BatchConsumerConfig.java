// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import java.time.Duration;

/**
 * Batch consumer config.
 */
public class BatchConsumerConfig {

    /**
     * The maximum number of events that will be in the list when this callback is invoked.
     */
    private final int maxBatchSize;

    /**
     * The max time duration to wait to receive a batch of events upto the max batch size before.
     * invoking this callback.
     */
    private final Duration maxWaitTime;

    public BatchConsumerConfig(int maxBatchSize, Duration maxWaitTime) {
        this.maxBatchSize = maxBatchSize;
        this.maxWaitTime = maxWaitTime;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public static BatchConsumerConfigBuilder builder() {
        return new BatchConsumerConfigBuilder();
    }

    /**
     * Builder class for {@link BatchConsumerConfig}.
     */
    public static class BatchConsumerConfigBuilder {
        private int maxBatchSize;

        private Duration maxWaitTime;

        public BatchConsumerConfigBuilder batchSize(int batchSize) {
            this.maxBatchSize = batchSize;
            return this;
        }

        public BatchConsumerConfigBuilder maxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
            return this;
        }

        public BatchConsumerConfig build() {
            return new BatchConsumerConfig(this.maxBatchSize, this.maxWaitTime);
        }
    }
}
