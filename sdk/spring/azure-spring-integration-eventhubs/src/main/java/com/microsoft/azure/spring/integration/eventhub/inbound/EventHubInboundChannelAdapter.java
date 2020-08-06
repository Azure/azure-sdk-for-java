// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub.inbound;

import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class EventHubInboundChannelAdapter extends AbstractInboundChannelAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(EventHubInboundChannelAdapter.class);

    public EventHubInboundChannelAdapter(String destination, SubscribeByGroupOperation subscribeByGroupOperation,
                                         String consumerGroup) {
        super(destination);
        Assert.hasText(consumerGroup, "consumerGroup can't be null or empty");
        this.subscribeByGroupOperation = subscribeByGroupOperation;
        this.consumerGroup = consumerGroup;
        LOG.info("Started EventHubInboundChannelAdapter with properties: {}", buildPropertiesMap());
    }
}
