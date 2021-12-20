// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.servicebus.core.ServiceBusProcessorContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

public class ServiceBusInboundChannelAdapterTests {

    private TestServiceBusInboundChannelAdapter adapter;

    protected String subscription = "group";
    protected String destination = "dest";
    private String[] payloads = { "payload1", "payload2" };
    private List<Message<?>> messages = Arrays.stream(payloads)
                                              .map(p -> MessageBuilder.withPayload(p).build())
                                              .collect(Collectors.toList());

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        ServiceBusProcessorContainer processorsContainer = mock(ServiceBusProcessorContainer.class);
        this.closeable = MockitoAnnotations.openMocks(this);
        this.adapter = new TestServiceBusInboundChannelAdapter(processorsContainer, destination, subscription,
            new CheckpointConfig(CheckpointMode.RECORD));
    }
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

        this.messages.forEach(this.adapter::sendMessage);
        Assertions.assertTrue(latch.await(5L, TimeUnit.SECONDS), "Failed to receive message");

        for (int i = 0; i < receivedMessages.size(); i++) {
            Assertions.assertEquals(receivedMessages.get(i), payloads[i]);
        }
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    static class TestServiceBusInboundChannelAdapter extends ServiceBusInboundChannelAdapter {


        TestServiceBusInboundChannelAdapter(ServiceBusProcessorContainer messageProcessorsContainer,
                                          String destination, String subscription,
                                          CheckpointConfig checkpointConfig) {
            super(messageProcessorsContainer, destination, subscription, checkpointConfig);
        }

        @Override
        public void sendMessage(Message<?> messageArg) {
            super.sendMessage(messageArg);
        }
    }
}
