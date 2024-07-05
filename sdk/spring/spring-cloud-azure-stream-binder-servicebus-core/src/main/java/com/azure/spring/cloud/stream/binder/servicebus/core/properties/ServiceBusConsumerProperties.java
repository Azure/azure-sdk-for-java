// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;

import java.time.Duration;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;

/**
 * Service Bus consumer properties.
 */
public class ServiceBusConsumerProperties extends ProcessorProperties implements ServiceBusEntityOptionsProvider {

    /**
     * Create an instance of {@link ServiceBusConsumerProperties}.
     */
    public ServiceBusConsumerProperties() {
    }

    private boolean requeueRejected;

    private Long maxSizeInMegabytes = 1024L;
    private Duration defaultMessageTimeToLive = MAX_DURATION;

    /**
     * Controls if the failed messages are routed to the DLQ
     *
     * @return boolean, default : false
     */
    public boolean isRequeueRejected() {
        return requeueRejected;
    }

    /**
     * Set checkpoint mode.
     *
     * @param requeueRejected the requeue Rejected
     */
    public void setRequeueRejected(boolean requeueRejected) {
        this.requeueRejected = requeueRejected;
    }

    @Override
    public Long getMaxSizeInMegabytes() {
        return maxSizeInMegabytes;
    }

    /**
     * Set the maxSizeInMegabytes property: The maximum size of the queue in megabytes, which is the size of memory allocated for the queue.
     * @param maxSizeInMegabytes the maxSizeInMegabytes value to set.
     */
    public void setMaxSizeInMegabytes(Long maxSizeInMegabytes) {
        this.maxSizeInMegabytes = maxSizeInMegabytes;
    }

    @Override
    public Duration getDefaultMessageTimeToLive() {
        return defaultMessageTimeToLive;
    }

    /**
     * Set the defaultMessageTimeToLive property: ISO 8601 default message timespan to live value.
     * This is the duration after which the message expires, starting from when the message is sent to Service Bus.
     * This is the default value used when TimeToLive is not set on a message itself.
     * @param defaultMessageTimeToLive the defaultMessageTimeToLive value to set.
     */
    public void setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
    }
}
