// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.inbound;

import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class ServiceBusTopicInboundChannelAdapter extends AbstractInboundChannelAdapter {

    public ServiceBusTopicInboundChannelAdapter(String destination,
                                                @NonNull SubscribeByGroupOperation subscribeByGroupOperation, String consumerGroup) {
        super(destination);
        Assert.hasText(consumerGroup, "consumerGroup cannot be null or empty");
        this.subscribeByGroupOperation = subscribeByGroupOperation;
        this.consumerGroup = consumerGroup;
    }
}
