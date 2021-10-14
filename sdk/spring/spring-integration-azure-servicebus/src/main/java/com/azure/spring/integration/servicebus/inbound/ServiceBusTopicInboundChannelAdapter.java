// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.spring.integration.endpoint.AbstractInboundChannelAdapter;
import com.azure.spring.messaging.core.SubscribeByGroupOperation;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Inbound channel adapter for Service Bus Topic.
 */
public class ServiceBusTopicInboundChannelAdapter extends AbstractInboundChannelAdapter {

    public ServiceBusTopicInboundChannelAdapter(String destination,
                                                @NonNull SubscribeByGroupOperation subscribeByGroupOperation,
                                                String consumerGroup) {
        super(destination);
        Assert.hasText(consumerGroup, "consumerGroup cannot be null or empty");
        this.subscribeByGroupOperation = subscribeByGroupOperation;
        this.consumerGroup = consumerGroup;
    }
}
