// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.servicebus.core.processor.container.ServiceBusTopicProcessorContainer;
import com.azure.spring.servicebus.core.processor.ServiceBusTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicTemplate;
import com.azure.spring.servicebus.support.ServiceBusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class ServiceBusTopicBinderHealthIndicatorTest {
    @Mock
    private ServiceBusTopicProcessorClientFactory serviceBusTopicProcessorClientFactory;
    @Mock
    private ServiceBusProducerFactory serviceBusTopicSenderClientFactory;
    @Mock
    private ServiceBusProcessorClient processorClient;
    @Mock
    private MessageProcessingListener listener;

    private ServiceBusTopicHealthIndicator serviceBusTopicHealthIndicator;

    private ServiceBusTopicTemplate serviceBusTopicTemplate;

    private ServiceBusTopicProcessorContainer serviceBusTopicProcessorContainer;

    private Consumer<Message<?>> consumer = message -> {
    };

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        serviceBusTopicTemplate = new ServiceBusTopicTemplate(serviceBusTopicSenderClientFactory);
        serviceBusTopicProcessorContainer = new ServiceBusTopicProcessorContainer(serviceBusTopicProcessorClientFactory);
        serviceBusTopicHealthIndicator = new ServiceBusTopicHealthIndicator(serviceBusTopicTemplate);
    }

    @Test
    public void testNoTopicInstrumentationInUse() {
        final Health health = serviceBusTopicHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceBusTopicIsUp() {
        when(serviceBusTopicProcessorClientFactory.createProcessor(anyString(), anyString(),
            any(MessageProcessingListener.class))).thenReturn(processorClient);
        serviceBusTopicProcessorContainer.subscribe("topic-test-1", "topicSubTest", listener);
        final Health health = serviceBusTopicHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceBusTopicIsDown() {
        when(serviceBusTopicProcessorClientFactory.createProcessor(anyString(), anyString(),
            any(MessageProcessingListener.class))).thenReturn(processorClient);
        doThrow(NullPointerException.class).when(processorClient).start();
        assertThrows(ServiceBusRuntimeException.class, () -> {
            serviceBusTopicProcessorContainer.subscribe("topic-test-1", "topicSubTest", listener);
        });
        final Health health = serviceBusTopicHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
