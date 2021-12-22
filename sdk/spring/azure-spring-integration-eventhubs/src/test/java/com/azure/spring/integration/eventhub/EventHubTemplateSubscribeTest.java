// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.integration.core.api.BatchConsumerConfig;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.azure.spring.integration.eventhub.impl.EventHubTemplate;
import com.azure.spring.integration.test.support.SubscribeByGroupOperationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventHubTemplateSubscribeTest extends SubscribeByGroupOperationTest<EventHubOperation> {

    @Mock
    private EventHubClientFactory mockClientFactory;

    @Mock
    private EventProcessorClient eventProcessorClient;

    private AutoCloseable closeable;

    private final BatchConsumerConfig batchConsumerConfig = BatchConsumerConfig.builder().batchSize(10).build();

    @BeforeEach
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.subscribeByGroupOperation = new EventHubTemplate(mockClientFactory);
        when(this.mockClientFactory.createEventProcessorClient(anyString(), anyString(), isA(EventHubProcessor.class),
            any())).thenReturn(this.eventProcessorClient);
        when(this.mockClientFactory.getEventProcessorClient(anyString(), anyString()))
            .thenReturn(Optional.of(this.eventProcessorClient));
        doNothing().when(this.eventProcessorClient).stop();
        doNothing().when(this.eventProcessorClient).start();
    }

    @Test
    public void testSubscribeAndUnsubscribeWithBatch() {
        this.subscribeByGroupOperation.setBatchConsumerConfig(batchConsumerConfig);
        super.testSubscribeAndUnsubscribe();
    }

    @Test
    public void testSubscribeTwice() {
        this.subscribeByGroupOperation.setBatchConsumerConfig(batchConsumerConfig);
        super.testSubscribeTwice();
    }

    @Test
    public void testSubscribeWithAnotherGroup() {
        this.subscribeByGroupOperation.setBatchConsumerConfig(batchConsumerConfig);
        super.testSubscribeWithAnotherGroup();
    }

    @Test
    public void testUnsubscribeNotSubscribed() {
        this.subscribeByGroupOperation.setBatchConsumerConfig(batchConsumerConfig);
        super.testUnsubscribeNotSubscribed();
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).createEventProcessorClient(anyString(), anyString(),
            isA(EventHubProcessor.class), any());
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).createEventProcessorClient(anyString(), anyString(),
            isA(EventHubProcessor.class), any());
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        verify(this.eventProcessorClient, times(times)).start();
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
        verify(this.mockClientFactory, times(times)).removeEventProcessorClient(anyString(), anyString());
        verify(this.eventProcessorClient, times(times)).stop();
    }
}
