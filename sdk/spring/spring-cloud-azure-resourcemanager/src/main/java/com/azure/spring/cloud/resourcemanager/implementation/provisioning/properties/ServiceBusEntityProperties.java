// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties;

import java.time.Duration;

/**
 * Entity configuration properties of Service Bus.
 */
public class ServiceBusEntityProperties {
    private Long maxSizeInMegabytes;
    private Duration defaultMessageTimeToLive;

    /**
     * Get the maxSizeInMegabytes property.
     * @return the maxSizeInMegabytes value.
     */
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

    /**
     * Set the defaultMessageTimeToLive property.
     * @return the defaultMessageTimeToLive value.
     */
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
