// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;

import java.time.Duration;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;

/**
 *  Service Bus producer properties.
 */
public class ServiceBusProducerProperties extends ProducerProperties implements ServiceBusEntityOptionsProvider {

    /**
     * Create an instance of {@link ServiceBusProducerProperties}.
     */
    public ServiceBusProducerProperties() {
    }

    private boolean sync = false;
    private Duration sendTimeout = Duration.ofMillis(10000);

    private Long maxSizeInMegabytes = 1024L;
    private Duration defaultMessageTimeToLive = MAX_DURATION;

    /**
     * Check whether is sync.
     *
     * @return true if is sync,false otherwise
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * Set sync.
     *
     * @param sync the sync
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    /**
     * Get send time out.
     *
     * @return sendTimeout the send time out
     */
    public Duration getSendTimeout() {
        return sendTimeout;
    }

    /**
     * Set send time out.
     *
     * @param sendTimeout the send time out
     */
    public void setSendTimeout(Duration sendTimeout) {
        this.sendTimeout = sendTimeout;
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
