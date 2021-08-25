// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.api.ProcessorConsumerFactory;
import com.azure.spring.integration.eventhub.api.ProducerFactory;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.azure.spring.integration.eventhub.impl.EventHubTemplate;
import com.azure.spring.integration.test.support.SubscribeByGroupOperationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventHubTemplateSubscribeTest extends SubscribeByGroupOperationTest<EventHubOperation> {

    @Mock
    ProducerFactory producerFactory;
    @Mock
    ProcessorConsumerFactory processorConsumerFactory;

    @Mock
    private EventProcessorClient eventProcessorClient;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.subscribeByGroupOperation = new EventHubTemplate(producerFactory,processorConsumerFactory);
        when(this.processorConsumerFactory.createEventProcessorClient(anyString(), anyString(), isA(EventHubProcessor.class)))
            .thenReturn(this.eventProcessorClient);
        when(this.processorConsumerFactory.getEventProcessorClient(anyString(), anyString()))
            .thenReturn(Optional.of(this.eventProcessorClient));
        doNothing().when(this.eventProcessorClient).stop();
        doNothing().when(this.eventProcessorClient).start();
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.processorConsumerFactory, atLeastOnce()).createEventProcessorClient(anyString(), anyString(),
            isA(EventHubProcessor.class));
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.processorConsumerFactory, never()).createEventProcessorClient(anyString(), anyString(),
            isA(EventHubProcessor.class));
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        verify(this.eventProcessorClient, times(times)).start();
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
        verify(this.processorConsumerFactory, times(times)).removeEventProcessorClient(anyString(), anyString());
        verify(this.eventProcessorClient, times(times)).stop();
    }
}
