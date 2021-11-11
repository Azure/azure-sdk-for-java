// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.properties;

import com.azure.spring.eventhubs.core.properties.ProducerProperties;

import java.time.Duration;

public class BatchableProducerProperties extends ProducerProperties implements EventHubBatchableProducerDescriptor {

    private Integer maxBatchInBytes;

    private Duration maxWaitTime;

    private boolean batchMode;

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

    public boolean isBatchMode() {
        return batchMode;
    }

    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }
}
