// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound;

import com.azure.spring.eventhubs.core.EventHubProcessorContainer;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import org.junit.jupiter.api.Assertions;
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

import static org.mockito.Mockito.mock;

class EventHubInboundAdapterTest {

    private TestEventHubInboundChannelAdapter adapter;

    private final String consumerGroup = "group";
    private final String eventHub = "dest";
    private final String[] payloads = { "payload1", "payload2" };
    private final List<Message<?>> messages = Arrays.stream(payloads)
                                                    .map(p -> MessageBuilder.withPayload(p).build())
                                                    .collect(Collectors.toList());


    @BeforeEach
    void setUp() {
        EventHubProcessorContainer processorsContainer = mock(EventHubProcessorContainer.class);
        //        EventProcessorClient processorClient = mock(EventProcessorClient.class);
        //        when(processorsContainer.subscribe(eventHub, consumerGroup, processorProperties)).thenReturn
        //        (processorClient);
        //

        this.adapter = new TestEventHubInboundChannelAdapter(processorsContainer, this.eventHub, this.consumerGroup,
            new CheckpointConfig());
    }

    @Test
    void sendAndReceive() throws InterruptedException {
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

    static class TestEventHubInboundChannelAdapter extends EventHubInboundChannelAdapter {


        TestEventHubInboundChannelAdapter(EventHubProcessorContainer eventProcessorsContainer,
                                          String eventHubName, String consumerGroup,
                                          CheckpointConfig checkpointConfig) {
            super(eventProcessorsContainer, eventHubName, consumerGroup, checkpointConfig);
        }

        @Override
        public void sendMessage(Message<?> messageArg) {
            super.sendMessage(messageArg);
        }
    }


}
