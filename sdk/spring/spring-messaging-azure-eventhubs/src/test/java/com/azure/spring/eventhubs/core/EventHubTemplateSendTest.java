// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.eventhubs.core.producer.BatchableProducerAsyncClient;
import com.azure.spring.eventhubs.core.producer.EventHubProducerFactory;
import com.azure.spring.messaging.core.SendOperationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class EventHubTemplateSendTest extends SendOperationTest<EventHubsTemplate> {

    private EventHubProducerFactory producerFactory;
    private EventHubProducerAsyncClient mockProducerClient;
    private BatchableProducerAsyncClient batchableProducerAsyncClient;

    @BeforeEach
    public void setUp() {
        this.mockProducerClient = mock(EventHubProducerAsyncClient.class);
        this.batchableProducerAsyncClient = new BatchableProducerAsyncClient(this.mockProducerClient, 0, Duration.ZERO);
        this.producerFactory = mock(EventHubProducerFactory.class);
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.producerFactory.createProducer(eq(this.destination))).thenReturn(this.batchableProducerAsyncClient);
        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
            .thenReturn(Mono.just(eventDataBatch));
        when(this.mockProducerClient.send(any(EventDataBatch.class))).thenReturn(this.mono);
        when(eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true);

        this.sendOperation = new EventHubsTemplate(producerFactory);
    }
    @Test
    public void testSendBatch() {
        final Mono<Void> mono = this.sendOperation.sendAsync(destination, Collections.nCopies(5, message), null);

        assertNull(mono.block());
        verifySendCalled(1);
    }
    @Test
    public void testSendBatchWithTimeout() {
        this.batchableProducerAsyncClient = new BatchableProducerAsyncClient(this.mockProducerClient, 0, Duration.ofMinutes(3));
        when(this.producerFactory.createProducer(eq(this.destination))).thenReturn(this.batchableProducerAsyncClient);

        final Mono<Void> mono = this.sendOperation.sendAsync(destination, Collections.nCopies(5, message), null);

        assertNull(mono.block());
        verifySendCalled(0);
    }
    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockProducerClient, times(times)).send(any(EventDataBatch.class));
    }

    @Override
    protected void whenSendWithException() {
        when(this.producerFactory.createProducer(this.destination))
            .thenThrow(EventHubRuntimeException.class);
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
