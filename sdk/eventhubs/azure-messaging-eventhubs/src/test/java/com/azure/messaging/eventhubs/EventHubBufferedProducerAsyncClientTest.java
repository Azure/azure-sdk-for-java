// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventHubBufferedProducerAsyncClientTest {
    private static final String PARTITION_ID = "10";
    private static final String NAMESPACE = "test-eventhubs-namespace";
    private static final String EVENT_HUB_NAME = "test-hub";
    private static final String[] PARTITION_IDS = new String[] { "one", "two", PARTITION_ID, "four" };

    private AutoCloseable mockCloseable;

    private final Queue<EventDataBatch> returnedBatches = new LinkedList<>();

    @Mock
    private Tracer tracer;

    @Mock
    private EventHubClientBuilder clientBuilder;

    @Mock
    private EventHubProducerAsyncClient asyncClient;

    @Mock
    private EventDataBatch batch;

    @Mock
    private EventDataBatch batch2;

    @Mock
    private EventDataBatch batch3;

    @Mock
    private EventDataBatch batch4;

    @Mock
    private EventDataBatch batch5;

    @BeforeEach
    public void beforeEach() {
        mockCloseable = MockitoAnnotations.openMocks(this);
        returnedBatches.add(batch);
        returnedBatches.add(batch2);
        returnedBatches.add(batch3);
        returnedBatches.add(batch4);
        returnedBatches.add(batch5);

        when(clientBuilder.buildAsyncProducerClient()).thenReturn(asyncClient);

        when(asyncClient.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        when(asyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(asyncClient.getPartitionIds()).thenReturn(Flux.fromArray(PARTITION_IDS));

        when(asyncClient.createBatch(any(CreateBatchOptions.class))).thenAnswer(invocation -> {
            final EventDataBatch returned = returnedBatches.poll();
            assertNotNull(returned, "there should be more batches to be returned.");
            return Mono.just(returned);
        });
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    @Test
    public void enqueuesEvent() {
        // Arrange
        final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();

        clientOptions.setMaxWaitTime(Duration.ofSeconds(5));
        clientOptions.setSendSucceededContext(succeedContext -> {

        });
        clientOptions.setSendFailedContext(failedContext -> {

        });

        final PartitionResolver partitionResolver = new PartitionResolver();
        final AmqpRetryOptions retryOptions
            = new AmqpRetryOptions().setMaxRetries(0).setTryTimeout(clientOptions.getMaxWaitTime());

        // Range some assumptions about the producer client.
        final List<String> partitionIds
            = IntStream.range(0, 1).mapToObj(index -> Integer.toString(index)).collect(Collectors.toList());
        final EventHubProperties eventHubProperties
            = new EventHubProperties("test", Instant.ofEpochSecond(1740431270L), partitionIds.toArray(new String[0]));
        final EventData eventData = new EventData("foo-bar");

        when(asyncClient.getEventHubProperties()).thenReturn(Mono.fromSupplier(() -> eventHubProperties));
        when(batch.tryAdd(eventData)).thenReturn(true);

        try (EventHubBufferedProducerAsyncClient bufferedClient = new EventHubBufferedProducerAsyncClient(clientBuilder,
            clientOptions, partitionResolver, retryOptions, tracer)) {

            // Don't assert the count of events count because EventDataBatch is mocked.
            StepVerifier.create(bufferedClient.enqueueEvent(eventData)).expectNextCount(1).expectComplete().verify();
        }

        verify(batch).tryAdd(eventData);
    }

    @Test
    public void enqueuesEventWithPartitionKey() {
        // Arrange
        final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();

        clientOptions.setMaxWaitTime(Duration.ofSeconds(5));
        clientOptions.setSendSucceededContext(succeedContext -> {
        });
        clientOptions.setSendFailedContext(failedContext -> {
        });

        final PartitionResolver partitionResolver = new PartitionResolver();
        final AmqpRetryOptions retryOptions
            = new AmqpRetryOptions().setMaxRetries(0).setTryTimeout(clientOptions.getMaxWaitTime());
        final EventHubProperties eventHubProperties
            = new EventHubProperties("test", Instant.ofEpochSecond(1740431270L), PARTITION_IDS);
        final SendOptions sendOptions = new SendOptions().setPartitionKey("my-partition-key");
        final EventData eventData = new EventData("foo-bar");

        when(asyncClient.getEventHubProperties()).thenReturn(Mono.fromSupplier(() -> eventHubProperties));
        when(batch.tryAdd(eventData)).thenReturn(true);

        try (EventHubBufferedProducerAsyncClient bufferedClient = new EventHubBufferedProducerAsyncClient(clientBuilder,
            clientOptions, partitionResolver, retryOptions, tracer)) {

            // Don't assert the count of events count because EventDataBatch is mocked.
            StepVerifier.create(bufferedClient.enqueueEvent(eventData, sendOptions))
                .expectNextCount(1)
                .expectComplete()
                .verify();
        }
    }

    @Test
    public void enqueuesEventWithPartitionId() {
        // Arrange
        final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();

        clientOptions.setMaxWaitTime(Duration.ofSeconds(5));
        clientOptions.setSendSucceededContext(succeedContext -> {

        });
        clientOptions.setSendFailedContext(failedContext -> {

        });

        final PartitionResolver partitionResolver = new PartitionResolver();
        final AmqpRetryOptions retryOptions
            = new AmqpRetryOptions().setMaxRetries(0).setTryTimeout(clientOptions.getMaxWaitTime());

        final EventHubProperties eventHubProperties
            = new EventHubProperties("test", Instant.ofEpochSecond(1740431270L), PARTITION_IDS);
        final SendOptions sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
        final EventData eventData = new EventData("foo-bar");

        when(asyncClient.getEventHubProperties()).thenReturn(Mono.fromSupplier(() -> eventHubProperties));
        when(batch.tryAdd(eventData)).thenReturn(true);

        try (EventHubBufferedProducerAsyncClient bufferedClient = new EventHubBufferedProducerAsyncClient(clientBuilder,
            clientOptions, partitionResolver, retryOptions, tracer)) {

            // Don't assert the count of events count because EventDataBatch is mocked.
            StepVerifier.create(bufferedClient.enqueueEvent(eventData, sendOptions))
                .expectNextCount(1)
                .expectComplete()
                .verify();
        }
    }

    @Test
    public void receivesEventHubProperties() {
        // Arrange
        final BufferedProducerClientOptions clientOptions = new BufferedProducerClientOptions();
        final PartitionResolver partitionResolver = new PartitionResolver();
        final String[] partitionIds = new String[5];
        for (int i = 0; i < partitionIds.length; i++) {
            partitionIds[i] = Integer.toString(i);
        }

        final String firstPartitionId = partitionIds[0];
        final PartitionProperties expectedPartitionProperties = new PartitionProperties(EVENT_HUB_NAME,
            firstPartitionId, 0L, 120L, "1256S", Instant.ofEpochSecond(1740431210L), true);
        final EventHubProperties eventHubProperties
            = new EventHubProperties(EVENT_HUB_NAME, Instant.ofEpochSecond(1740431270L), partitionIds);

        when(asyncClient.getPartitionProperties(firstPartitionId))
            .thenReturn(Mono.defer(() -> Mono.just(expectedPartitionProperties)));
        when(asyncClient.getEventHubProperties()).thenReturn(Mono.fromSupplier(() -> eventHubProperties));

        try (EventHubBufferedProducerAsyncClient bufferedClient = new EventHubBufferedProducerAsyncClient(clientBuilder,
            clientOptions, partitionResolver, new AmqpRetryOptions(), tracer)) {

            Assertions.assertEquals(EVENT_HUB_NAME, bufferedClient.getEventHubName());
            Assertions.assertEquals(NAMESPACE, bufferedClient.getFullyQualifiedNamespace());

            StepVerifier.create(bufferedClient.getPartitionIds()).expectNext(partitionIds).expectComplete().verify();

            StepVerifier.create(bufferedClient.getPartitionProperties(firstPartitionId)).assertNext(actual -> {
                Assertions.assertEquals(expectedPartitionProperties, actual);
            }).expectComplete().verify();
        }
    }
}
