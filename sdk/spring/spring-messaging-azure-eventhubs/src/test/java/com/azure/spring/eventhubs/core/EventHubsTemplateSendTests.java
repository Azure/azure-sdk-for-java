// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.messaging.core.SendOperationTests;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class EventHubsTemplateSendTests extends SendOperationTests<EventHubsTemplate> {

    private EventHubsProducerFactory producerFactory;
    private EventHubProducerAsyncClient mockProducerClient;

    @BeforeEach
    public void setUp() {
        this.mockProducerClient = mock(EventHubProducerAsyncClient.class);
        this.producerFactory = mock(EventHubsProducerFactory.class);
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.producerFactory.createProducer(eq(this.destination))).thenReturn(this.mockProducerClient);
        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
            .thenReturn(Mono.just(eventDataBatch));
        when(this.mockProducerClient.send(any(EventDataBatch.class))).thenReturn(this.mono);
        when(eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true);

        this.sendOperation = new EventHubsTemplate(producerFactory);
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockProducerClient, times(times)).send(any(EventDataBatch.class));
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.producerFactory, times(times)).createProducer(this.destination);
    }

    @Override
    protected void setupError(String errorMessage) {
        when(this.mockProducerClient.send(any(EventDataBatch.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Send failed.")));
    }
}
