// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.endpoint;

import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.core.SubscribeByGroupOperation;
import com.azure.spring.messaging.core.SubscribeOperation;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The abstract inbound channel adapter. The subscriber will start to subscribe on start and stop subscribing on stop.
 */
public abstract class AbstractInboundChannelAdapter<T extends Lifecycle & DisposableBean> extends MessageProducerSupport {

    protected final String destination;
    protected String consumerGroup = null;
    protected final T processorContainer;
    protected final ListenerMode listenerMode;
    protected final CheckpointConfig checkpointConfig;

    protected AbstractInboundChannelAdapter(T processorContainer, String destination, CheckpointConfig checkpointConfig) {
        this(processorContainer, destination, ListenerMode.RECORD, checkpointConfig);
    }

    protected AbstractInboundChannelAdapter(T processorContainer, String destination, ListenerMode listenerMode,
                                            CheckpointConfig checkpointConfig) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.processorContainer = processorContainer;
        this.destination = destination;
        this.listenerMode = listenerMode;
        this.checkpointConfig = checkpointConfig;
    }

    @Override
    public void doStart() {
        this.processorContainer.start();
    }

    @Override
    protected void doStop() {
        this.processorContainer.stop();
    }

    public void receiveMessage(Message<?> message) {
        sendMessage(message);
    }

}
