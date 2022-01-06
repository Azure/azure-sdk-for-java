// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.eventhubs.core.EventHubsProcessorContainer;
import com.azure.spring.integration.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EventHubsInboundChannelAdapterTests {

    private TestEventHubsInboundChannelAdapter adapter;

    private final String consumerGroup = "group";
    private final String eventHub = "dest";
    private final String[] payloads = { "payload1", "payload2" };
    private final List<Message<?>> messages = Arrays.stream(payloads)
                                                    .map(p -> MessageBuilder.withPayload(p).build())
                                                    .collect(Collectors.toList());
    private EventHubsProcessorContainer processorsContainer;

    @BeforeEach
    void setUp() {
        processorsContainer = mock(EventHubsProcessorContainer.class);
        this.adapter = new TestEventHubsInboundChannelAdapter(processorsContainer, this.eventHub, this.consumerGroup,
            new CheckpointConfig());
    }

    @Test
    void setInstrumentationManager() {
        DefaultInstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
        this.adapter.setInstrumentationManager(instrumentationManager);
        assertThat(this.adapter).extracting("recordEventProcessor").hasFieldOrProperty("instrumentationManager");
        assertThat(this.adapter).extracting("batchEventProcessor").hasFieldOrPropertyWithValue("instrumentationManager", null);
    }

    @Test
    void setInstrumentationId() {
        String instrumentationId = "testId";
        this.adapter.setInstrumentationId(instrumentationId);
        assertThat(this.adapter).extracting("recordEventProcessor").hasFieldOrProperty("instrumentationId");
        assertThat(this.adapter).extracting("batchEventProcessor").hasFieldOrPropertyWithValue("instrumentationId", null);
    }

    @Test
    void setMessageConverter() {
        TestAzureMessageConverter converter = new TestAzureMessageConverter();
        this.adapter.setMessageConverter(converter);
        assertThat(this.adapter).extracting("recordEventProcessor").extracting("messageConverter").isEqualTo(converter);
        assertThat(this.adapter).extracting("batchEventProcessor").extracting("messageConverter").isNotEqualTo(converter);
    }

    @Test
    void setBatchMessageConverter() {
        TestBatchAzureMessageConverter converter = new TestBatchAzureMessageConverter();
        this.adapter.setBatchMessageConverter(converter);
        assertThat(this.adapter).extracting("batchEventProcessor").extracting("messageConverter").isEqualTo(converter);
        assertThat(this.adapter).extracting("recordEventProcessor").extracting("messageConverter").isNotEqualTo(converter);
    }

    @Test
    void setPayloadType() {
        this.adapter.afterPropertiesSet();
        assertThat(this.adapter).extracting("listener").extracting("payloadType").isEqualTo(byte[].class);
        this.adapter.setPayloadType(Long.class);
        this.adapter.afterPropertiesSet();
        assertThat(this.adapter).extracting("listener").extracting("payloadType").isEqualTo(Long.class);
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

    static class TestEventHubsInboundChannelAdapter extends EventHubsInboundChannelAdapter {


        TestEventHubsInboundChannelAdapter(EventHubsProcessorContainer eventProcessorsContainer,
                                           String eventHubName, String consumerGroup,
                                           CheckpointConfig checkpointConfig) {
            super(eventProcessorsContainer, eventHubName, consumerGroup, checkpointConfig);
        }

        @Override
        public void sendMessage(Message<?> messageArg) {
            super.sendMessage(messageArg);
        }
    }


    static class TestAzureMessageConverter extends AbstractAzureMessageConverter<EventData, EventData> {


        @Override
        protected Object getPayload(EventData azureMessage) {
            return azureMessage.getBody();
        }

        @Override
        protected EventData fromString(String payload) {
            return new EventData(payload.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        protected EventData fromByte(byte[] payload) {
            return new EventData(payload);
        }
    }

    static class TestBatchAzureMessageConverter extends AbstractAzureMessageConverter<EventBatchContext, EventData> {


        @Override
        protected Object getPayload(EventBatchContext azureMessage) {
            return azureMessage.getEvents().stream().map(EventData::getBody).collect(Collectors.toList());
        }

        @Override
        protected EventData fromString(String payload) {
            return new EventData(payload.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        protected EventData fromByte(byte[] payload) {
            return new EventData(payload);
        }
    }
}
