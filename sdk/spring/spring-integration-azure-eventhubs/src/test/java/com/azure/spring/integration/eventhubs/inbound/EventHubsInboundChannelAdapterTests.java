// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.integration.implementation.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventHubsInboundChannelAdapterTests {

    private TestEventHubsInboundChannelAdapter adapter;

    private final String consumerGroup = "group";
    private final String eventHub = "dest";
    private final String[] payloads = { "payload1", "payload2" };
    private final List<Message<?>> messages = Arrays.stream(payloads)
                                                    .map(p -> MessageBuilder.withPayload(p).build())
                                                    .collect(Collectors.toList());
    @BeforeEach
    void setUp() {
        EventHubsProcessorFactory processorFactory = mock(EventHubsProcessorFactory.class);
        when(processorFactory.createProcessor(eq(eventHub), eq(consumerGroup), isA(EventHubsContainerProperties.class))).thenReturn(mock(EventProcessorClient.class));

        EventHubsContainerProperties containerProperties = new EventHubsContainerProperties();
        containerProperties.setEventHubName(eventHub);
        containerProperties.setConsumerGroup(consumerGroup);

        this.adapter = new TestEventHubsInboundChannelAdapter(
            new EventHubsMessageListenerContainer(processorFactory, containerProperties),
            new CheckpointConfig());
    }

    @Test
    void setInstrumentationManager() {
        DefaultInstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
        this.adapter.setInstrumentationManager(instrumentationManager);
        assertThat(this.adapter).hasFieldOrPropertyWithValue("instrumentationManager", instrumentationManager);
    }

    @Test
    void setInstrumentationId() {
        String instrumentationId = "testId";
        this.adapter.setInstrumentationId(instrumentationId);
        assertThat(this.adapter).hasFieldOrPropertyWithValue("instrumentationId", instrumentationId);
    }

    @Test
    void setMessageConverter() {
        TestAzureMessageConverter converter = new TestAzureMessageConverter();
        this.adapter.setMessageConverter(converter);
        assertThat(this.adapter).extracting("recordListener").extracting("messageConverter").isEqualTo(converter);
        assertThat(this.adapter).extracting("batchListener").extracting("messageConverter").isNotEqualTo(converter);
    }

    @Test
    void setBatchMessageConverter() {
        TestBatchAzureMessageConverter converter = new TestBatchAzureMessageConverter();
        this.adapter.setBatchMessageConverter(converter);
        assertThat(this.adapter).extracting("batchListener").extracting("messageConverter").isEqualTo(converter);
        assertThat(this.adapter).extracting("recordListener").extracting("messageConverter").isNotEqualTo(converter);
    }

    @Test
    void setPayloadType() {
        this.adapter.afterPropertiesSet();
        assertThat(this.adapter).extracting("recordListener").extracting("payloadType").isEqualTo(byte[].class);
        this.adapter.setPayloadType(Long.class);
        this.adapter.afterPropertiesSet();
        assertThat(this.adapter).extracting("recordListener").extracting("payloadType").isEqualTo(Long.class);
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


        TestEventHubsInboundChannelAdapter(EventHubsMessageListenerContainer messageListenerContainer,
                                           CheckpointConfig checkpointConfig) {
            super(messageListenerContainer, checkpointConfig);
        }

        @Override
        public void sendMessage(Message<?> messageArg) {
            super.sendMessage(messageArg);
        }
    }


    static class TestAzureMessageConverter extends AbstractAzureMessageConverter<EventData, EventData> {


        @Override
        protected ObjectMapper getObjectMapper() {
            return null;
        }

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
        protected ObjectMapper getObjectMapper() {
            return null;
        }

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
