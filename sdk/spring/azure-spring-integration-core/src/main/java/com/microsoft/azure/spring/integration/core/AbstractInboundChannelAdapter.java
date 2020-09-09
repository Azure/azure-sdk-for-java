// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core;

import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.microsoft.azure.spring.integration.core.api.SubscribeOperation;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractInboundChannelAdapter extends MessageProducerSupport {
    private final String destination;
    protected String consumerGroup = null;
    protected SubscribeOperation subscribeOperation = null;
    protected SubscribeByGroupOperation subscribeByGroupOperation = null;

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public SubscribeOperation getSubscribeOperation() {
        return subscribeOperation;
    }

    public void setSubscribeOperation(SubscribeOperation subscribeOperation) {
        this.subscribeOperation = subscribeOperation;
    }

    public SubscribeByGroupOperation getSubscribeByGroupOperation() {
        return subscribeByGroupOperation;
    }

    public void setSubscribeByGroupOperation(SubscribeByGroupOperation subscribeByGroupOperation) {
        this.subscribeByGroupOperation = subscribeByGroupOperation;
    }

    protected AbstractInboundChannelAdapter(String destination) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
    }

    @Override
    public void doStart() {
        super.doStart();
        if (useGroupOperation()) {
            this.subscribeByGroupOperation.subscribe(this.destination, this.consumerGroup, this::receiveMessage);
        } else {
            this.subscribeOperation.subscribe(this.destination, this::receiveMessage);
        }
    }

    public void receiveMessage(Message<?> message) {
        sendMessage(message);
    }

    @Override
    protected void doStop() {
        if (useGroupOperation()) {
            this.subscribeByGroupOperation.unsubscribe(destination, this.consumerGroup);
        } else {
            this.subscribeOperation.unsubscribe(destination);
        }

        super.doStop();
    }

    private boolean useGroupOperation() {
        return this.subscribeByGroupOperation != null && StringUtils.hasText(consumerGroup);
    }

    protected Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("consumerGroup", consumerGroup);
        properties.put("destination", destination);

        return properties;
    }

}
