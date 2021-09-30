package com.azure.spring.integration.core.api;

import java.time.Duration;

/**
 * Batch consumer config.
 */
public class BatchConfig {

    /**
     * The maximum number of events that will be in the list when this callback is invoked.
     */
    private final int maxBatchSize;

    /**
     * The max time duration to wait to receive a batch of events upto the max batch size before.
     * invoking this callback.
     */
    private final Duration maxWaitTime;

    public BatchConfig(int maxBatchSize, Duration maxWaitTime) {
        this.maxBatchSize = maxBatchSize;
        this.maxWaitTime = maxWaitTime;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public static BatchConfigBuilder builder() {
        return new BatchConfigBuilder();
    }

    public static class BatchConfigBuilder {
        private int maxBatchSize;

        private Duration maxWaitTime;

        public BatchConfigBuilder maxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
            return this;
        }

        public BatchConfigBuilder maxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
            return this;
        }

        public BatchConfig build() {
            return new BatchConfig(this.maxBatchSize, this.maxWaitTime);
        }
    }
}
