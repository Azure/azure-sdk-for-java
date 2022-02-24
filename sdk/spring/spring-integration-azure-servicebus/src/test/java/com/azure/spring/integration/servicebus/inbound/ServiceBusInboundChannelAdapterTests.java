// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.integration.implementation.DefaultInstrumentationManager;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter.IntegrationRecordMessageProcessingListener;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.azure.spring.servicebus.core.ServiceBusProcessorContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusInboundChannelAdapterTests {

    private TestServiceBusInboundChannelAdapter adapter;
    protected String subscription = "group";
    protected String destination = "dest";
    private String[] payloads = { "payload1", "payload2" };
    private List<Message<?>> messages = Arrays.stream(payloads)
                                              .map(p -> MessageBuilder.withPayload(p).build())
                                              .collect(Collectors.toList());

    @BeforeEach
    public void setUp() {
        this.adapter = new TestServiceBusInboundChannelAdapter(mock(ServiceBusProcessorContainer.class),
            destination, subscription, new CheckpointConfig(CheckpointMode.RECORD));
    }

    @Test
    void destinationCannotEmptyWhenEntityTypeIsTopic() {
        assertThrows(IllegalArgumentException.class,
            () -> new TestServiceBusInboundChannelAdapter(mock(ServiceBusProcessorContainer.class),
                null, null, new CheckpointConfig(CheckpointMode.RECORD)));
    }

    @Test
    void setInstrumentationManager() {
        DefaultInstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
        this.adapter.setInstrumentationManager(instrumentationManager);
        assertThat(this.adapter).extracting("recordEventProcessor").hasFieldOrProperty("instrumentationManager");
    }

    @Test
    void setInstrumentationId() {
        String instrumentationId = "testId";
        this.adapter.setInstrumentationId(instrumentationId);
        assertThat(this.adapter).extracting("recordEventProcessor").hasFieldOrProperty("instrumentationId");
    }

    @Test
    void setMessageConverter() {
        TestServiceBusMessageConverter converter = new TestServiceBusMessageConverter();
        this.adapter.setMessageConverter(converter);
        assertThat(this.adapter).extracting("recordEventProcessor").extracting("messageConverter").isEqualTo(converter);
    }

    @Test
    void setPayloadType() {
        this.adapter.afterPropertiesSet();
        assertThat(this.adapter).extracting("recordEventProcessor").extracting("payloadType").isEqualTo(byte[].class);
        this.adapter.setPayloadType(Long.class);
        this.adapter.afterPropertiesSet();
        assertThat(this.adapter).extracting("recordEventProcessor").extracting("payloadType").isEqualTo(Long.class);
    }

    @Test
    void consumeErrorContextWhenNoInstrumentationManager() {
        ServiceBusErrorContext serviceBusErrorContext = mock(ServiceBusErrorContext.class);
        when(serviceBusErrorContext.getEntityPath()).thenReturn("test-path");
        when(serviceBusErrorContext.getException()).thenReturn(new IllegalArgumentException());
        assertDoesNotThrow(() -> {
            IntegrationRecordMessageProcessingListener listener = this.adapter.new IntegrationRecordMessageProcessingListener();
            listener.getErrorContextConsumer().accept(serviceBusErrorContext);
        });
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

    static class TestServiceBusMessageConverter extends AbstractAzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> {

        @Override
        protected ObjectMapper getObjectMapper() {
            return null;
        }

        @Override
        protected Object getPayload(ServiceBusReceivedMessage azureMessage) {
            final BinaryData body = azureMessage.getBody();
            return body == null ? null : body.toBytes();
        }

        @Override
        protected ServiceBusMessage fromString(String payload) {
            return new ServiceBusMessage(payload);
        }

        @Override
        protected ServiceBusMessage fromByte(byte[] payload) {
            return new ServiceBusMessage(payload);
        }
    }
}
