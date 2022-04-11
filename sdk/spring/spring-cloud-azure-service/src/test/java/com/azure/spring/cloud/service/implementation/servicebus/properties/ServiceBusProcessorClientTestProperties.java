// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;

import java.time.Duration;

public class ServiceBusProcessorClientTestProperties extends ServiceBusClientCommonTestProperties
    implements ServiceBusProcessorClientProperties {

    private Integer maxConcurrentCalls;
    private Integer maxConcurrentSessions;
    private Boolean sessionEnabled;
    private Boolean autoComplete;
    private Integer prefetchCount;
    private SubQueue subQueue;
    private ServiceBusReceiveMode receiveMode;
    private String subscriptionName;
    private Duration maxAutoLockRenewDuration;

    public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public void setMaxConcurrentSessions(Integer maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    public void setSessionEnabled(Boolean sessionEnabled) {
        this.sessionEnabled = sessionEnabled;
    }

    public void setAutoComplete(Boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public void setSubQueue(SubQueue subQueue) {
        this.subQueue = subQueue;
    }

    public void setReceiveMode(ServiceBusReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public void setMaxAutoLockRenewDuration(Duration maxAutoLockRenewDuration) {
        this.maxAutoLockRenewDuration = maxAutoLockRenewDuration;
    }

    @Override
    public Integer getMaxConcurrentCalls() {
        return this.maxConcurrentCalls;
    }

    @Override
    public Integer getMaxConcurrentSessions() {
        return this.maxConcurrentSessions;
    }

    @Override
    public Boolean getSessionEnabled() {
        return this.sessionEnabled;
    }

    @Override
    public Boolean getAutoComplete() {
        return this.autoComplete;
    }

    @Override
    public Integer getPrefetchCount() {
        return this.prefetchCount;
    }

    @Override
    public SubQueue getSubQueue() {
        return this.subQueue;
    }

    @Override
    public ServiceBusReceiveMode getReceiveMode() {
        return this.receiveMode;
    }

    @Override
    public String getSubscriptionName() {
        return this.subscriptionName;
    }

    @Override
    public Duration getMaxAutoLockRenewDuration() {
        return this.maxAutoLockRenewDuration;
    }
}
