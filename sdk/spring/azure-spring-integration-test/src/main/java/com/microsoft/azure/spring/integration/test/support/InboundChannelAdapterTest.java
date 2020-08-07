// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.test.support;

import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class InboundChannelAdapterTest<A extends AbstractInboundChannelAdapter> {

    protected String destination = "dest";
    protected String consumerGroup = "group";
    protected A adapter;
    private String[] payloads = {"payload1", "payload2"};
    private List<Message<?>> messages =
        Arrays.stream(payloads).map(p -> MessageBuilder.withPayload(p).build()).collect(Collectors.toList());

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public A getAdapter() {
        return adapter;
    }

    public void setAdapter(A adapter) {
        this.adapter = adapter;
    }

    @Before
    public abstract void setUp();

    @Test
    public void sendAndReceive() throws InterruptedException {
        DirectChannel channel = new DirectChannel();
        channel.setBeanName("output");
        this.adapter.doStart();
        this.adapter.setOutputChannel(channel);

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> receivedMessages = new CopyOnWriteArrayList<>();
        channel.subscribe(message -> {
            try {
                receivedMessages.add((String) message.getPayload());
            } finally {
                latch.countDown();
            }

        });

        this.messages.forEach(this.adapter::receiveMessage);
        assertTrue("Failed to receive message", latch.await(5L, TimeUnit.SECONDS));

        for (int i = 0; i < receivedMessages.size(); i++) {
            assertEquals(receivedMessages.get(i), payloads[i]);
        }
    }
}
