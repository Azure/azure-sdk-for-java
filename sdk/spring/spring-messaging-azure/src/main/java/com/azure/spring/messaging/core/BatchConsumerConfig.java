// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import java.time.Duration;

public class BatchConsumerConfig {
    private final int maxBatchSize;
    private final Duration maxWaitTime;

    public BatchConsumerConfig(int maxBatchSize, Duration maxWaitTime) {
        this.maxBatchSize = maxBatchSize;
        this.maxWaitTime = maxWaitTime;
    }

    public int getMaxBatchSize() {
        return this.maxBatchSize;
    }

    public Duration getMaxWaitTime() {
        return this.maxWaitTime;
    }

    public static BatchConsumerConfigBuilder builder() {
        return new BatchConsumerConfigBuilder();
    }

    public static class BatchConsumerConfigBuilder {
        private int maxBatchSize;
        private Duration maxWaitTime;

        public BatchConsumerConfigBuilder() {
        }

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

