// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link EventProcessor}.
 */
public class EventProcessorTest {

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubConsumer consumer1, consumer2, consumer3;

    @Mock
    private EventData eventData1, eventData2, eventData3, eventData4;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests all the happy cases for {@link EventProcessor}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testWithSimplePartitionProcessor() throws Exception {
        // Arrange
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(consumer1.receive()).thenReturn(Flux.just(eventData1, eventData2));
        when(eventData1.sequenceNumber()).thenReturn(1L);
        when(eventData2.sequenceNumber()).thenReturn(2L);
        when(eventData1.offset()).thenReturn("1");
        when(eventData2.offset()).thenReturn("100");

        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();
        final InMemoryPartitionManager partitionManager = new InMemoryPartitionManager();

        final long beforeTest = System.currentTimeMillis();

        // Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubAsyncClient,
            "test-consumer",
            (partitionContext, checkpointManager) -> {
                testPartitionProcessor.checkpointManager = checkpointManager;
                testPartitionProcessor.partitionContext = partitionContext;
                return testPartitionProcessor;
            }, EventPosition.latest(), partitionManager, "test-eh");
        eventProcessor.start();
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        eventProcessor.stop();

        // Assert
        assertNotNull(eventProcessor.identifier());

        assertNotNull(testPartitionProcessor.partitionContext);
        assertNotNull(testPartitionProcessor.checkpointManager);

        assertEquals("1", testPartitionProcessor.partitionContext.partitionId());
        assertEquals("test-eh", testPartitionProcessor.partitionContext.eventHubName());
        assertEquals("test-consumer", testPartitionProcessor.partitionContext.consumerGroupName());

        StepVerifier.create(partitionManager.listOwnership("test-eh", "test-consumer"))
            .expectNextCount(1).verifyComplete();

        StepVerifier.create(partitionManager.listOwnership("test-eh", "test-consumer"))
            .assertNext(partitionOwnership -> {
                assertEquals("Partition", "1", partitionOwnership.partitionId());
                assertEquals("Consumer", "test-consumer", partitionOwnership.consumerGroupName());
                assertEquals("EventHub name", "test-eh", partitionOwnership.eventHubName());
                assertEquals("Sequence number", 2, (long) partitionOwnership.sequenceNumber());
                assertEquals("Offset", "100", partitionOwnership.offset());
                assertEquals("OwnerId", eventProcessor.identifier(), partitionOwnership.ownerId());
                assertTrue("LastModifiedTime", partitionOwnership.lastModifiedTime() >= beforeTest);
                assertTrue("LastModifiedTime", partitionOwnership.lastModifiedTime() <= System.currentTimeMillis());
                assertNotNull(partitionOwnership.eTag());
            }).verifyComplete();

        verify(eventHubAsyncClient, atLeastOnce()).getPartitionIds();
        verify(eventHubAsyncClient, atLeastOnce())
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class));
        verify(consumer1, atLeastOnce()).receive();
        verify(consumer1, atLeastOnce()).close();
    }

    /**
     * Tests {@link EventProcessor} with a partition processor that throws an exception when processing an
     * event.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testWithFaultyPartitionProcessor() throws Exception {
        // Arrange
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(consumer1.receive()).thenReturn(Flux.just(eventData1));

        final FaultyPartitionProcessor faultyPartitionProcessor = new FaultyPartitionProcessor();
        final InMemoryPartitionManager partitionManager = new InMemoryPartitionManager();

        // Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubAsyncClient,
            "test-consumer",
            (partitionContext, checkpointManager) -> faultyPartitionProcessor,
            EventPosition.latest(), partitionManager, "test-eh");
        eventProcessor.start();
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        eventProcessor.stop();

        // Assert
        assertTrue(faultyPartitionProcessor.error);
    }

    /**
     * Tests {@link EventProcessor} that processes events from an Event Hub configured with multiple
     * partitions.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testWithMultiplePartitions() throws Exception {
        // Arrange
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1", "2", "3"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), eq("1"), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(consumer1.receive()).thenReturn(Flux.just(eventData1, eventData2));
        when(eventData1.sequenceNumber()).thenReturn(1L);
        when(eventData2.sequenceNumber()).thenReturn(2L);
        when(eventData1.offset()).thenReturn("1");
        when(eventData2.offset()).thenReturn("100");

        when(eventHubAsyncClient
            .createConsumer(anyString(), eq("2"), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer2);
        when(consumer2.receive()).thenReturn(Flux.just(eventData3));
        when(eventData3.sequenceNumber()).thenReturn(1L);
        when(eventData3.offset()).thenReturn("1");

        when(eventHubAsyncClient
            .createConsumer(anyString(), eq("3"), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer3);
        when(consumer3.receive()).thenReturn(Flux.just(eventData4));
        when(eventData4.sequenceNumber()).thenReturn(1L);
        when(eventData4.offset()).thenReturn("1");

        final InMemoryPartitionManager partitionManager = new InMemoryPartitionManager();
        // Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubAsyncClient,
            "test-consumer",
            TestPartitionProcessor::new, EventPosition.latest(), partitionManager, "test-eh");
        eventProcessor.start();
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        eventProcessor.stop();

        // Assert
        StepVerifier.create(partitionManager.listOwnership("test-eh", "test-consumer"))
            .expectNextCount(3).verifyComplete();

        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, times(1))
            .createConsumer(anyString(), eq("1"), any(EventPosition.class), any(EventHubConsumerOptions.class));
        verify(eventHubAsyncClient, times(1))
            .createConsumer(anyString(), eq("2"), any(EventPosition.class), any(EventHubConsumerOptions.class));
        verify(eventHubAsyncClient, times(1))
            .createConsumer(anyString(), eq("3"), any(EventPosition.class), any(EventHubConsumerOptions.class));

        verify(consumer1, atLeastOnce()).receive();
        verify(consumer1, atLeastOnce()).close();

        verify(consumer2, atLeastOnce()).receive();
        verify(consumer2, atLeastOnce()).close();

        verify(consumer3, atLeastOnce()).receive();
        verify(consumer3, atLeastOnce()).close();
    }

    private static final class FaultyPartitionProcessor implements PartitionProcessor {

        boolean error;

        @Override
        public Mono<Void> initialize() {
            return Mono.empty();
        }

        @Override
        public Mono<Void> processEvent(EventData eventData) {
            return Mono.error(new IllegalStateException());
        }

        @Override
        public void processError(Throwable throwable) {
            error = true;
        }

        @Override
        public Mono<Void> close(CloseReason closeReason) {
            return Mono.empty();
        }
    }

    private static final class TestPartitionProcessor implements PartitionProcessor {

        PartitionContext partitionContext;
        CheckpointManager checkpointManager;

        private TestPartitionProcessor() {
            // default ctr
        }

        private TestPartitionProcessor(PartitionContext partitionContext, CheckpointManager checkpointManager) {
            this.partitionContext = partitionContext;
            this.checkpointManager = checkpointManager;
        }

        @Override
        public Mono<Void> initialize() {
            return Mono.empty();
        }

        @Override
        public Mono<Void> processEvent(EventData eventData) {
            return this.checkpointManager.updateCheckpoint(eventData);
        }

        @Override
        public void processError(Throwable throwable) {
        }

        @Override
        public Mono<Void> close(CloseReason closeReason) {
            return Mono.empty();
        }
    }

}
