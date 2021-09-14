// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration;

import com.azure.spring.messaging.core.SubscribeByGroupOperation;
import com.azure.spring.messaging.core.SubscribeOperation;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The abstract inbound channel adapter. The subscriber will start to subscribe on start and stop subscribing on stop.
 */
public abstract class AbstractInboundChannelAdapter extends MessageProducerSupport {

    private final String destination;
    protected String consumerGroup = null;
    protected SubscribeByGroupOperation subscribeByGroupOperation = null;
    protected SubscribeOperation subscribeOperation = null;

    protected AbstractInboundChannelAdapter(String destination) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
    }

    protected Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("consumerGroup", consumerGroup);
        properties.put("destination", destination);

        return properties;
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

    @Override
    protected void doStop() {
        if (useGroupOperation()) {
            this.subscribeByGroupOperation.unsubscribe(destination, this.consumerGroup);
        } else {
            this.subscribeOperation.unsubscribe(destination);
        }

        super.doStop();
    }

    public void receiveMessage(Message<?> message) {
        sendMessage(message);
    }

    private boolean useGroupOperation() {
        return this.subscribeByGroupOperation != null && StringUtils.hasText(consumerGroup);
    }

}
