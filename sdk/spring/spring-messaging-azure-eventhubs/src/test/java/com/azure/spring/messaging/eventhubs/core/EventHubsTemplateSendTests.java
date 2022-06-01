// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.messaging.core.SendOperationTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

import static com.azure.spring.messaging.AzureHeaders.PARTITION_ID;
import static com.azure.spring.messaging.AzureHeaders.PARTITION_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class EventHubsTemplateSendTests extends SendOperationTests<EventHubsTemplate> {

    @Mock
    private EventHubsProducerFactory producerFactory;

    @Mock
    private EventHubProducerAsyncClient mockProducerClient;

    private AutoCloseable mockCloseable;

    @BeforeEach
    public void setUp() {
        mockCloseable = MockitoAnnotations.openMocks(this);

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

    @AfterEach
    public void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }

    @Override
    public void verifySendCalled(int times) {
        verify(this.mockProducerClient, times(times)).send(any(EventDataBatch.class));
    }

    @Override
    public void verifyGetClientCreator(int times) {
        verify(this.producerFactory, times(times)).createProducer(this.destination);
    }

    @Override
    public void setupError(String errorMessage) {
        when(this.mockProducerClient.send(any(EventDataBatch.class)))
            .thenReturn(Mono.error(new IllegalArgumentException("Send failed.")));
    }

    @Test
    public void testGetPartitionIdFromMessageHeader() {
        Message<String> message = MessageBuilder.withPayload("test")
                                                .setHeader(PARTITION_ID, "partition-id")
                                                .build();
        PartitionSupplier partitionSupplier = sendOperation.buildPartitionSupplier(message);
        assertEquals(partitionSupplier.getPartitionId(), "partition-id");
        assertNull(partitionSupplier.getPartitionKey());
    }

    @Test
    public void testGetPartitionKeyFromMessageHeader() {
        Message<String> message = MessageBuilder.withPayload("test")
                                                .setHeader(PARTITION_KEY, "partition-key")
                                                .build();
        PartitionSupplier partitionSupplier = sendOperation.buildPartitionSupplier(message);
        assertNull(partitionSupplier.getPartitionId());
        assertEquals(partitionSupplier.getPartitionKey(), "partition-key");
    }

    @Test
    public void testGetPartitionIdAndKeyFromMessageHeader() {
        Message<String> message = MessageBuilder.withPayload("test")
                                                .setHeader(PARTITION_ID, "partition-id")
                                                .setHeader(PARTITION_KEY, "partition-key")
                                                .build();
        PartitionSupplier partitionSupplier = sendOperation.buildPartitionSupplier(message);
        assertEquals(partitionSupplier.getPartitionId(), "partition-id");
        assertEquals(partitionSupplier.getPartitionKey(), "partition-key");
    }

    @Test
    public void testFailToGetPartitionIFromMessageHeader() {
        Message<String> message = MessageBuilder.withPayload("test").build();
        PartitionSupplier partitionSupplier = sendOperation.buildPartitionSupplier(message);
        assertNull(partitionSupplier.getPartitionId());
        assertNull(partitionSupplier.getPartitionKey());
    }
}
