// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.implementation.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.servicebus.inbound.implementation.health.ServiceBusProcessorInstrumentation;
import com.azure.spring.messaging.ListenerMode;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.azure.spring.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.service.servicebus.consumer.ServiceBusMessageListener;
import com.azure.spring.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.servicebus.core.properties.ServiceBusContainerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.azure.spring.integration.instrumentation.Instrumentation.Type.CONSUMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusInboundChannelAdapterTests {

    private ServiceBusInboundChannelAdapter adapter;
    private ServiceBusProcessorFactory processorFactory;
    private ServiceBusContainerProperties containerProperties;
    protected String subscription = "group";
    protected String destination = "dest";
    private String[] payloads = { "payload1", "payload2" };
    private List<Message<?>> messages = Arrays.stream(payloads)
                                              .map(p -> MessageBuilder.withPayload(p).build())
                                              .collect(Collectors.toList());

    @BeforeEach
    public void setUp() {
        this.processorFactory = mock(ServiceBusProcessorFactory.class);
        when(processorFactory.createProcessor(eq(destination), eq(subscription), isA(ServiceBusContainerProperties.class))).thenReturn(mock(ServiceBusProcessorClient.class));

        this.containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(destination);
        containerProperties.setSubscriptionName(subscription);

        this.adapter = new ServiceBusInboundChannelAdapter(
            new ServiceBusMessageListenerContainer(processorFactory, containerProperties));
    }

    @Test
    void defaultRecordListenerMode() {
        this.containerProperties.setCheckpointConfig(new CheckpointConfig(CheckpointMode.RECORD));
        ServiceBusInboundChannelAdapter channelAdapter = new ServiceBusInboundChannelAdapter(
            new ServiceBusMessageListenerContainer(this.processorFactory, this.containerProperties));
        assertThat(channelAdapter).hasFieldOrPropertyWithValue("listenerMode", ListenerMode.RECORD);
    }

    @Test
    void batchListenerModeDoesNotSupport() {
        this.containerProperties.setCheckpointConfig(new CheckpointConfig(CheckpointMode.BATCH));
        ServiceBusInboundChannelAdapter channelAdapter = new ServiceBusInboundChannelAdapter(
            new ServiceBusMessageListenerContainer(this.processorFactory, this.containerProperties),
            ListenerMode.BATCH);

        assertThrows(IllegalStateException.class, channelAdapter::onInit, "Only record mode is supported!");
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
        TestServiceBusMessageConverter converter = new TestServiceBusMessageConverter();
        this.adapter.setMessageConverter(converter);
        assertThat(this.adapter).extracting("recordListener").extracting("messageConverter").isEqualTo(converter);
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
        this.containerProperties.setCheckpointConfig(new CheckpointConfig(CheckpointMode.RECORD));
        ServiceBusMessageListenerContainer listenerContainer =
            new ServiceBusMessageListenerContainer(this.processorFactory, this.containerProperties);
        ServiceBusInboundChannelAdapter channelAdapter = new ServiceBusInboundChannelAdapter(listenerContainer);

        DirectChannel channel = new DirectChannel();
        channel.setBeanName("output");

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> receivedMessages = new CopyOnWriteArrayList<>();
        channel.subscribe(message -> {
            try {
                receivedMessages.add(new String((byte[]) message.getPayload()));
            } finally {
                latch.countDown();
            }

        });

        channelAdapter.setOutputChannel(channel);
        channelAdapter.onInit();
        channelAdapter.doStart();

        ServiceBusMessageListener messageListener = listenerContainer.getContainerProperties().getMessageListener();
        assertTrue(messageListener instanceof ServiceBusRecordMessageListener);
        List<String> payloads = Arrays.asList("a", "b", "c");
        payloads.stream()
                .map(payload -> {
                    ServiceBusReceivedMessageContext mock = mock(ServiceBusReceivedMessageContext.class);
                    ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
                    when(message.getBody()).thenReturn(BinaryData.fromString(payload));
                    when(mock.getMessage()).thenReturn(message);
                    return mock;
                })
                .forEach(context -> ((ServiceBusRecordMessageListener) messageListener).onMessage(context));


        assertTrue(latch.await(5L, TimeUnit.SECONDS), "Failed to receive message");

        for (int i = 0; i < receivedMessages.size(); i++) {
            Assertions.assertEquals(receivedMessages.get(i), payloads.get(i));
        }
    }

    @Test
    void instrumentationErrorHandler() {
        this.containerProperties.setCheckpointConfig(new CheckpointConfig(CheckpointMode.RECORD));
        DefaultInstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
        ServiceBusMessageListenerContainer listenerContainer =
            new ServiceBusMessageListenerContainer(this.processorFactory, this.containerProperties);
        ServiceBusInboundChannelAdapter channelAdapter = new ServiceBusInboundChannelAdapter(listenerContainer);

        String instrumentationId = CONSUMER + ":" + destination;

        ServiceBusProcessorInstrumentation processorInstrumentation = new ServiceBusProcessorInstrumentation(
            destination, CONSUMER, Duration.ofMinutes(1));
        instrumentationManager.addHealthInstrumentation(processorInstrumentation);

        processorInstrumentation.setStatus(Instrumentation.Status.UP);
        assertEquals(Instrumentation.Status.UP, processorInstrumentation.getStatus());

        channelAdapter.setInstrumentationId(instrumentationId);
        channelAdapter.setInstrumentationManager(instrumentationManager);
        channelAdapter.onInit();
        channelAdapter.doStart();

        ServiceBusErrorHandler errorHandler = listenerContainer.getContainerProperties().getErrorHandler();

        ServiceBusErrorContext errorContext = mock(ServiceBusErrorContext.class);
        when(errorContext.getException()).thenReturn(new IllegalArgumentException("test"));
        when(errorContext.getEntityPath()).thenReturn("entity-path");
        errorHandler.accept(errorContext);

        Instrumentation healthInstrumentation = instrumentationManager.getHealthInstrumentation(instrumentationId);
        assertEquals(Instrumentation.Status.DOWN, healthInstrumentation.getStatus());
        assertEquals(healthInstrumentation.getException().getClass(), IllegalArgumentException.class);
        assertEquals(healthInstrumentation.getException().getMessage(), "test");

    }

    static class TestServiceBusMessageConverter extends AbstractAzureMessageConverter<ServiceBusReceivedMessage,
        ServiceBusMessage> {

        @Override
        protected ObjectMapper getObjectMapper() {
            return null;
        }

        @Override
        protected Object getPayload(ServiceBusReceivedMessage azureMessage) {
            final BinaryData body = azureMessage.getBody();
            return body.toBytes();
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
