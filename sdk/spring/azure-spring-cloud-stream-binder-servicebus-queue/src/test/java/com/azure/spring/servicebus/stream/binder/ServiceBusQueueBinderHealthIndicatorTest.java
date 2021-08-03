// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
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

public class ServiceBusQueueBinderHealthIndicatorTest {

    @Mock
    private ServiceBusQueueClientFactory serviceBusQueueClientFactory;

    @Mock
    private ServiceBusProcessorClient processorClient;

    private ServiceBusQueueHealthIndicator serviceBusQueueHealthIndicator;

    private ServiceBusQueueTemplate serviceBusQueueTemplate;

    private Consumer<Message<?>> consumer = message -> {
    };


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        serviceBusQueueTemplate = new ServiceBusQueueTemplate(serviceBusQueueClientFactory);
        serviceBusQueueHealthIndicator = new ServiceBusQueueHealthIndicator(serviceBusQueueTemplate);
    }

    @Test
    public void testNoInstrumentationInUse() {
        final Health health = serviceBusQueueHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceBusQueueIsUp() {
        when(serviceBusQueueClientFactory.getOrCreateProcessor(anyString(), any(ServiceBusClientConfig.class),
            any(ServiceBusMessageProcessor.class))).thenReturn(processorClient);
        serviceBusQueueTemplate.subscribe("queue-test-1", consumer, byte[].class);
        final Health health = serviceBusQueueHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceBusQueueIsDown() {
        when(serviceBusQueueClientFactory.getOrCreateProcessor(anyString(), any(ServiceBusClientConfig.class),
            any(ServiceBusMessageProcessor.class))).thenReturn(processorClient);
        doThrow(RuntimeException.class).when(processorClient).start();
        assertThrows(RuntimeException.class, () -> {
            serviceBusQueueTemplate.subscribe("queue-test-1", consumer, byte[].class);
        });
        final Health health = serviceBusQueueHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

}
