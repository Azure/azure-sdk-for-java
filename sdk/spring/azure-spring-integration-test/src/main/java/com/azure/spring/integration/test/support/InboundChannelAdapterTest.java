// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.AbstractInboundChannelAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In bound channel adapter test.
 * @param <A> The Type that extends AbstractInboundChannelAdapter.
 */
public abstract class InboundChannelAdapterTest<A extends AbstractInboundChannelAdapter> {

    /**
     * The adapter.
     */
    protected A adapter;

    /**
     * The consumer group.
     */
    protected String consumerGroup = "group";

    /**
     * The destination.
     */
    protected String destination = "dest";

    private String[] payloads = { "payload1", "payload2" };
    private List<Message<?>> messages = Arrays.stream(payloads)
                                              .map(p -> MessageBuilder.withPayload(p).build())
                                              .collect(Collectors.toList());

    /**
     *
     * @throws InterruptedException The interruptedException
     */
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
        assertTrue(latch.await(5L, TimeUnit.SECONDS), "Failed to receive message");

        for (int i = 0; i < receivedMessages.size(); i++) {
            assertEquals(receivedMessages.get(i), payloads[i]);
        }
    }

    /**
     * Set up.
     */
    @BeforeEach
    public abstract void setUp();

    /**
     *
     * @return The adapter.
     */
    public A getAdapter() {
        return adapter;
    }

    /**
     *
     * @param adapter The adapter.
     */
    public void setAdapter(A adapter) {
        this.adapter = adapter;
    }

    /**
     *
     * @return The consumerGroup.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     *
     * @param consumerGroup The consumerGroup.
     */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /**
     *
     * @return The destination.
     */
    public String getDestination() {
        return destination;
    }

    /**
     *
     * @param destination The destination.
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }
}
