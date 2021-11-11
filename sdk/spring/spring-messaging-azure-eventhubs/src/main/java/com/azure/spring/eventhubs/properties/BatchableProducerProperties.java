// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.properties;

import com.azure.spring.eventhubs.core.properties.ProducerProperties;

import java.time.Duration;

public class BatchableProducerProperties extends ProducerProperties implements EventHubBatchableProducerDescriptor {

    /**
     * The maximum size, in bytes, of a batch of events. The default value is 256 * 1024.
     */
    private Integer maxBatchInBytes;

    /**
     * The maximum time duration to wait to send a batch of events when maxSizeInBytes is not reached. The default value
     * is 5min.
     */
    private Duration maxWaitTime;

    public BatchableProducerProperties() {
        this.maxWaitTime = Duration.ofMinutes(5);
    }

    public Integer getMaxBatchInBytes() {
        return maxBatchInBytes;
    }

    public void setMaxBatchInBytes(Integer maxBatchInBytes) {
        this.maxBatchInBytes = maxBatchInBytes;
    }

    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }
}
