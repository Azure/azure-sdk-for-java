// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.properties;

import java.time.Duration;

public class ServiceBusProcessorClientTestProperties extends ServiceBusReceiverClientTestProperties
    implements ServiceBusProcessorClientProperties {

    private Integer maxConcurrentCalls;
    private Integer maxConcurrentSessions;
    private Duration sessionIdleTimeout;

    @Override
    public Integer getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    @Override
    public Integer getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(Integer maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    @Override
    public Duration getSessionIdleTimeout() {
        return sessionIdleTimeout;
    }

    public void setSessionIdleTimeout(Duration sessionIdleTimeout) {
        this.sessionIdleTimeout = sessionIdleTimeout;
    }
}
