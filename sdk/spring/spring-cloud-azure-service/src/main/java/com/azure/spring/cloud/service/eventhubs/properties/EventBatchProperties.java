// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.eventhubs.properties;

import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventProcessorClientProperties;

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

    @Override
    public Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * Set the max wait time.
     * @param maxWaitTime The max wait time.
     */
    public void setMaxWaitTime(Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    @Override
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     * Set the max size.
     * @param maxSize The max size.
     */
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }
}
