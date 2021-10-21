// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;

import java.time.Duration;

/**
 *
 */
public interface ServiceBusConsumerProperties extends ServiceBusCommonProperties {

    Boolean getSessionAware();

    Boolean getAutoComplete();

    Integer getPrefetchCount();

    String getQueueName();

    SubQueue getSubQueue();

    ServiceBusReceiveMode getReceiveMode();

    String getSubscriptionName();

    String getTopicName();

    Duration getMaxAutoLockRenewDuration();

}
