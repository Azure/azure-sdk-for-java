// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubRuntimeException;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubTemplate;
import com.microsoft.azure.spring.integration.test.support.reactor.SendOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventHubTemplateSendTest extends SendOperationTest<EventHubOperation> {

    @Mock
    EventDataBatch eventDataBatch;
    @Mock
    private EventHubClientFactory mockClientFactory;
    @Mock
    private EventHubProducerAsyncClient mockProducerClient;

    @Before
    public void setUp() {
        when(this.mockClientFactory.getOrCreateProducerClient(eq(this.destination)))
            .thenReturn(this.mockProducerClient);
        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
            .thenReturn(Mono.just(this.eventDataBatch));
        when(this.mockProducerClient.send(any(EventDataBatch.class))).thenReturn(this.mono);
        when(this.eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true);

        this.sendOperation = new EventHubTemplate(mockClientFactory);
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockProducerClient, times(times)).send(any(EventDataBatch.class));
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getOrCreateProducerClient(this.destination))
            .thenThrow(EventHubRuntimeException.class);
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getOrCreateProducerClient(this.destination);
    }

    @Override
    protected void setupError(String errorMessage) {
        when(this.mockProducerClient.send(any(EventDataBatch.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Send failed.")));
    }
}
