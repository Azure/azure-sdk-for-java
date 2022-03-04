// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.properties;

import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientProperties;

/**
 * A service bus processor related properties.
 */
public class ProcessorProperties extends ConsumerProperties implements ServiceBusProcessorClientProperties {

    private Integer maxConcurrentCalls = 1;
    private Integer maxConcurrentSessions = null;

    @Override
    public Integer getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     * Set the max concurrent call number.
     * @param maxConcurrentCalls the max concurrent call number.
     */
    public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    @Override
    public Integer getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    /**
     * Set the max concurrent session number.
     * @param maxConcurrentSessions the max concurrent session number.
     */
    public void setMaxConcurrentSessions(Integer maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }
}
