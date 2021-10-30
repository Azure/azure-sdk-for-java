// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.service.servicebus.properties.ServiceBusProcessorDescriptor;

public class ProcessorProperties extends ConsumerProperties implements ServiceBusProcessorDescriptor {

    private Integer maxConcurrentCalls;
    private Integer maxConcurrentSessions;

    public Integer getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public Integer getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(Integer maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }
}
