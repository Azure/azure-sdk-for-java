// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusReceiverClientProperties;

import java.time.Duration;

/**
 * A service bus consumer related properties.
 */
public class ConsumerProperties extends CommonProperties implements ServiceBusReceiverClientProperties {

    private Boolean sessionAware;
    private Boolean autoComplete;
    private Integer prefetchCount;
    private SubQueue subQueue = SubQueue.NONE;
    private ServiceBusReceiveMode receiveMode = ServiceBusReceiveMode.PEEK_LOCK;
    private String subscriptionName;
    private Duration maxAutoLockRenewDuration = Duration.ofMinutes(5);

    @Override
    public Boolean getSessionEnabled() {
        return sessionAware;
    }

    /**
     * Set if is session aware.
     * @param sessionAware if is session awared.
     */
    public void setSessionAware(Boolean sessionAware) {
        this.sessionAware = sessionAware;
    }

    @Override
    public Boolean getAutoComplete() {
        return autoComplete;
    }

    /**
     * Set if enable auto-complete.
     * @param autoComplete if enable auto-complete.
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
     * Set the receive mode.
     * @param receiveMode the receive mode.
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
