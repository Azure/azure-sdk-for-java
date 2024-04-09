// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import java.time.Duration;

/**
 * Properties to define a service bus entity
 */
public interface ServiceBusEntityOptionsProvider {

    /**
     * Gets the maximum size of the queue/topic in megabytes, which is the size of memory allocated for the queue/topic.
     *
     * @return the maxSizeInMegabytes value.
     */
    Long getMaxSizeInMegabytes();

    /**
     * Gets the default message time to live value. This is the duration after which the message expires, starting from
     * when the message is sent to Service Bus. This is the default value used when TimeToLive is not set on a message
     * itself.Messages older than their TimeToLive value will expire and no longer be retained in the message store.
     * Subscribers will be unable to receive expired messages. A message can have a lower TimeToLive value than that
     * specified here, but by default TimeToLive is set to MaxValue. Therefore, this property becomes the default time
     * to live value applied to messages.
     *
     * @return the defaultMessageTimeToLive value.
     */
    Duration getDefaultMessageTimeToLive();

}
