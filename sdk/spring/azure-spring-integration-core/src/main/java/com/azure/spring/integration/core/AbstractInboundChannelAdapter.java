// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core;

import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.azure.spring.integration.core.api.SubscribeOperation;
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

    /**
     * The consumer group
     */
    protected String consumerGroup = null;

    /**
     * Subscribe by group operation/
     */
    protected SubscribeByGroupOperation subscribeByGroupOperation = null;

    /**
     * The subscribe operation.
     */
    protected SubscribeOperation subscribeOperation = null;

    private final String destination;

    /**
     *
     * @param destination The destination.
     */
    protected AbstractInboundChannelAdapter(String destination) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
    }

    /**
     *
     * @return The properties map.
     */
    protected Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("consumerGroup", consumerGroup);
        properties.put("destination", destination);

        return properties;
    }

    /**
     * Do start.
     */
    @Override
    public void doStart() {
        super.doStart();
        if (useGroupOperation()) {
            this.subscribeByGroupOperation.subscribe(this.destination, this.consumerGroup, this::receiveMessage);
        } else {
            this.subscribeOperation.subscribe(this.destination, this::receiveMessage);
        }
    }

    /**
     * Do stop.
     */
    @Override
    protected void doStop() {
        if (useGroupOperation()) {
            this.subscribeByGroupOperation.unsubscribe(destination, this.consumerGroup);
        } else {
            this.subscribeOperation.unsubscribe(destination);
        }

        super.doStop();
    }

    /**
     *
     * @param message The message.
     */
    public void receiveMessage(Message<?> message) {
        sendMessage(message);
    }

    private boolean useGroupOperation() {
        return this.subscribeByGroupOperation != null && StringUtils.hasText(consumerGroup);
    }

}
