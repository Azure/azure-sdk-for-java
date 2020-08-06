// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubTemplate;
import com.microsoft.azure.spring.integration.test.support.SubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubTemplateSubscribeTest extends SubscribeByGroupOperationTest<EventHubOperation> {

    @Mock
    private EventHubClientFactory mockClientFactory;

    @Mock
    private EventProcessorClient eventProcessorClient;

    @Before
    public void setUp() {
        this.subscribeByGroupOperation = new EventHubTemplate(mockClientFactory);
        when(this.mockClientFactory.createEventProcessorClient(anyString(), anyString(), isA(EventHubProcessor.class)))
            .thenReturn(this.eventProcessorClient);
        when(this.mockClientFactory.getEventProcessorClient(anyString(), anyString()))
            .thenReturn(Optional.of(this.eventProcessorClient));
        doNothing().when(this.eventProcessorClient).stop();
        doNothing().when(this.eventProcessorClient).start();
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).createEventProcessorClient(anyString(), anyString(),
            isA(EventHubProcessor.class));
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).createEventProcessorClient(anyString(), anyString(),
            isA(EventHubProcessor.class));
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
