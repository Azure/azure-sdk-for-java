// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

import com.azure.spring.service.implementation.eventhubs.properties.EventProcessorClientProperties;

import java.time.Duration;

/**
 * Event processor batch properties.
 */
public class EventBatchProperties implements EventProcessorClientProperties.EventBatch {

    /**
     * The max time duration to wait to receive an event before processing events.
     */
    private Duration maxWaitTime;
    /**
     * The maximum number of events that will be in the batch.
     */
    private Integer maxSize;

    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }
}
