// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusReceiverClientProperties;

import java.time.Duration;

/**
 * A service bus consumer related properties.
 */
public class ConsumerProperties extends CommonProperties implements ServiceBusReceiverClientProperties {

    /**
     * Create an instance of {@link ConsumerProperties}.
     */
    public ConsumerProperties() {
    }

    private Boolean sessionEnabled;
    private Boolean autoComplete;
    private Integer prefetchCount;
    private SubQueue subQueue;
    private ServiceBusReceiveMode receiveMode;
    private String subscriptionName;
    private Duration maxAutoLockRenewDuration;

    @Override
    public Boolean getSessionEnabled() {
        return sessionEnabled;
    }

    /**
     * Set whether to enable session for the consumer.
     * @param sessionEnabled whether session is enabled for consumer.
     */
    public void setSessionEnabled(Boolean sessionEnabled) {
        this.sessionEnabled = sessionEnabled;
    }

    @Override
    public Boolean getAutoComplete() {
        return autoComplete;
    }

    /**
     * Set whether to enable auto-complete.
     * @param autoComplete whether auto-complete is enabled.
     */
    public void setAutoComplete(Boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    @Override
    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Set the prefetch count.
     * @param prefetchCount the prefetch count.
     */
    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    @Override
    public SubQueue getSubQueue() {
        return subQueue;
    }

    /**
     * Set the sub queue.
     * @param subQueue the sub queue.
     */
    public void setSubQueue(SubQueue subQueue) {
        this.subQueue = subQueue;
    }

    @Override
    public ServiceBusReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     * Set the receiving mode.
     * @param receiveMode the receiving mode.
     */
    public void setReceiveMode(ServiceBusReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
    }

    @Override
    public String getSubscriptionName() {
        return subscriptionName;
    }

    /**
     * Set the subscription name.
     * @param subscriptionName the subscription name.
     */
    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    @Override
    public Duration getMaxAutoLockRenewDuration() {
        return maxAutoLockRenewDuration;
    }

    /**
     * Set the mau auto lock renew duration.
     * @param maxAutoLockRenewDuration the mau auto lock renew duration.
     */
    public void setMaxAutoLockRenewDuration(Duration maxAutoLockRenewDuration) {
        this.maxAutoLockRenewDuration = maxAutoLockRenewDuration;
    }
}
