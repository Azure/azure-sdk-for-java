// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;
import com.azure.spring.servicebus.core.ServiceBusMessageProcessor;
import com.azure.spring.servicebus.support.ServiceBusRuntimeException;
import com.azure.spring.servicebus.core.ServiceBusTopicClientFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicTemplate;
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
    private ServiceBusTopicClientFactory serviceBusTopicClientFactory;

    @Mock
    private ServiceBusProcessorClient processorClient;

    private ServiceBusTopicHealthIndicator serviceBusTopicHealthIndicator;

    private ServiceBusTopicTemplate serviceBusTopicTemplate;

    private Consumer<Message<?>> consumer = message -> {
    };

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        serviceBusTopicTemplate = new ServiceBusTopicTemplate(serviceBusTopicClientFactory);
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
        when(serviceBusTopicClientFactory.getOrCreateProcessor(anyString(), anyString(),
            any(ServiceBusClientConfig.class),
            any(ServiceBusMessageProcessor.class))).thenReturn(processorClient);
        serviceBusTopicTemplate.subscribe("topic-test-1", "topicSubTest", consumer, byte[].class);
        final Health health = serviceBusTopicHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceBusTopicIsDown() {
        when(serviceBusTopicClientFactory.getOrCreateProcessor(anyString(), anyString(),
            any(ServiceBusClientConfig.class),
            any(ServiceBusMessageProcessor.class))).thenReturn(processorClient);
        doThrow(NullPointerException.class).when(processorClient).start();
        assertThrows(ServiceBusRuntimeException.class, () -> {
            serviceBusTopicTemplate.subscribe("topic-test-1", "topicSubTest", consumer, byte[].class);
        });
        final Health health = serviceBusTopicHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
