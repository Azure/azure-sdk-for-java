// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracingLink;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer.DIAGNOSTIC_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer.MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EventProcessorClient}.
 */
public class EventProcessorClientTest {
    private AutoCloseable mocksDisposable;

    @Mock
    private EventHubClientBuilder eventHubClientBuilder;

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubConsumerAsyncClient consumer1, consumer2, consumer3;

    @Mock
    private EventData eventData1, eventData2, eventData3, eventData4;

    @BeforeEach
    public void setup() {
        mocksDisposable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        if (mocksDisposable != null) {
            mocksDisposable.close();
        }

        consumer1 = null;
        consumer2 = null;
        consumer3 = null;
        eventData1 = null;
        eventData2 = null;
        eventData3 = null;
        eventData4 = null;
        eventHubAsyncClient = null;

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Tests all the happy cases for {@link EventProcessorClient}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testWithSimplePartitionProcessor() throws Exception {
        Tracer tracer = mock(Tracer.class);
        // Arrange
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), anyBoolean()))
            .thenReturn(consumer1);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class))).thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2)));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();

        final long beforeTest = System.currentTimeMillis();
        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        when(tracer.extractContext(any())).thenAnswer(invocation -> new Context(SPAN_CONTEXT_KEY, "value"));
        when(tracer.start(eq("EventHubs.process"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), 0);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
            }
        );

        // Act
        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, "test-consumer",
            () -> testPartitionProcessor, checkpointStore, false, ec -> { }, new HashMap<>(),
            1, null, false, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);
        eventProcessorClient.start();
        TimeUnit.SECONDS.sleep(10);

        // Assert
        assertNotNull(eventProcessorClient.getIdentifier());

        StepVerifier.create(checkpointStore.listOwnership("test-ns", "test-eh", "test-consumer"))
            .expectNextCount(1).verifyComplete();

        StepVerifier.create(checkpointStore.listOwnership("test-ns", "test-eh", "test-consumer"))
            .assertNext(partitionOwnership -> {
                assertEquals("1", partitionOwnership.getPartitionId(), "Partition");
                assertEquals("test-consumer", partitionOwnership.getConsumerGroup(), "Consumer");
                assertEquals("test-eh", partitionOwnership.getEventHubName(), "EventHub name");
                assertEquals(eventProcessorClient.getIdentifier(), partitionOwnership.getOwnerId(), "OwnerId");
                assertTrue(partitionOwnership.getLastModifiedTime() >= beforeTest, "LastModifiedTime");
                assertTrue(partitionOwnership.getLastModifiedTime() <= System.currentTimeMillis(), "LastModifiedTime");
                assertNotNull(partitionOwnership.getETag());
            }).verifyComplete();

        verify(eventHubAsyncClient, atLeastOnce()).getPartitionIds();
        verify(eventHubAsyncClient, atLeastOnce())
            .createConsumer(anyString(), anyInt(), eq(true));
        verify(consumer1, atLeastOnce()).receiveFromPartition(anyString(), any(EventPosition.class),
            any(ReceiveOptions.class));
        verify(consumer1, atLeastOnce()).close();
        eventProcessorClient.stop();
        StepVerifier.create(checkpointStore.listOwnership("test-ns", "test-eh", "test-consumer"))
            .assertNext(partitionOwnership -> {
                assertEquals("1", partitionOwnership.getPartitionId(), "Partition");
                assertEquals("test-consumer", partitionOwnership.getConsumerGroup(), "Consumer");
                assertEquals("test-eh", partitionOwnership.getEventHubName(), "EventHub name");
                assertEquals("", partitionOwnership.getOwnerId(), "Owner Id");
                assertTrue(partitionOwnership.getLastModifiedTime() >= beforeTest, "LastModifiedTime");
                assertTrue(partitionOwnership.getLastModifiedTime() <= System.currentTimeMillis(), "LastModifiedTime");
                assertNotNull(partitionOwnership.getETag());
            }).verifyComplete();
    }

    /**
     * Tests process start spans invoked for {@link EventProcessorClient}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testProcessSpans() throws Exception {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer1);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), eq(true)))
            .thenReturn(consumer1);
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData1.getOffset()).thenReturn(100L);
        when(eventData1.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639208));

        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        Map<String, Object> properties = new HashMap<>();
        properties.put(DIAGNOSTIC_ID_KEY, diagnosticId);

        when(eventData1.getProperties()).thenReturn(properties);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1)));
        when(tracer1.extractContext(any())).thenAnswer(invocation -> {
            Function<String, String> consumer = invocation.getArgument(0, Function.class);
            assertEquals(diagnosticId, consumer.apply("traceparent"));
            assertNull(consumer.apply("tracestate"));
            return new Context(SPAN_CONTEXT_KEY, "value");
        });
        when(tracer1.start(eq("EventHubs.process"), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), 0);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
            }
        );

        CountDownLatch latch = new CountDownLatch(1);
        when(tracer1.makeSpanCurrent(any())).thenReturn(() -> { });

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(tracer1).end(isNull(), isNull(), any());

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, "test-consumer",
            TestPartitionProcessor::new, checkpointStore, false, ec -> { }, new HashMap<>(),
            1, null, false, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);

        eventProcessorClient.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        eventProcessorClient.stop();

        //Assert
        verify(tracer1, times(1)).extractContext(any());
        verify(tracer1, times(1)).start(eq("EventHubs.process"), any(), any(Context.class));
        verify(tracer1, times(1)).end(isNull(), isNull(), any());
    }


    /**
     * Tests process start spans invoked for {@link EventProcessorClient}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testProcessBatchSpans() throws Exception {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer1);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), eq(true)))
            .thenReturn(consumer1);
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData1.getOffset()).thenReturn(100L);
        when(eventData1.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639208));
        when(eventData2.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639209));

        String diagnosticId1 = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        Map<String, Object> properties1 = new HashMap<>();
        properties1.put(DIAGNOSTIC_ID_KEY, diagnosticId1);

        String diagnosticId2 = "00-18ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        Map<String, Object> properties2 = new HashMap<>();
        properties2.put(DIAGNOSTIC_ID_KEY, diagnosticId2);

        when(eventData1.getProperties()).thenReturn(properties1);
        when(eventData2.getProperties()).thenReturn(properties2);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2)));

        AtomicInteger counter = new AtomicInteger(0);
        when(tracer1.extractContext(any())).thenAnswer(invocation -> {
            Function<String, String> consumer = invocation.getArgument(0, Function.class);
            String traceparent = consumer.apply("traceparent");
            if (counter.getAndIncrement() == 0) {
                assertEquals(diagnosticId1, traceparent);
            } else {
                assertEquals(diagnosticId2, traceparent);
            }
            return new Context(SPAN_CONTEXT_KEY, traceparent);
        });
        when(tracer1.start(eq("EventHubs.process"), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), 2);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
            }
        );

        CountDownLatch latch = new CountDownLatch(1);
        when(tracer1.makeSpanCurrent(any())).thenReturn(() -> { });

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(tracer1).end(isNull(), isNull(), any());

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, "test-consumer",
            TestPartitionProcessor::new, checkpointStore, false, ec -> { }, new HashMap<>(),
            2, null, true, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);

        eventProcessorClient.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        eventProcessorClient.stop();

        //Assert
        verify(tracer1, times(2)).extractContext(any());
        verify(tracer1, times(1)).start(eq("EventHubs.process"), any(), any(Context.class));
        verify(tracer1, times(1)).end(isNull(), isNull(), any());
    }

    /**
     * Tests process start spans invoked without diagnostic id from event data of upstream for {@link EventProcessorClient}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testProcessSpansWithoutDiagnosticId() throws Exception {
        //Arrange
        final Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), eq(true)))
            .thenReturn(consumer1);
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData1.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639208));
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData2.getOffset()).thenReturn(100L);
        when(eventData2.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639208));
        when(eventData3.getSequenceNumber()).thenReturn(3L);
        when(eventData3.getOffset()).thenReturn(150L);
        when(eventData3.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639208));
        final int numberOfEvents = 3;

        Map<String, Object> properties = new HashMap<>();

        when(eventData1.getProperties()).thenReturn(properties);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2), getEvent(eventData3)));

        when(tracer.start(eq("EventHubs.process"), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), 0);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
            }
        );

        AtomicBoolean closed = new AtomicBoolean(false);
        when(tracer.makeSpanCurrent(any())).thenReturn(() -> closed.set(true));

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);
        TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();
        testPartitionProcessor.countDownLatch = countDownLatch;
        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, "test-consumer",
            () -> testPartitionProcessor, checkpointStore, false,  ec -> { }, new HashMap<>(),
            1, null, false, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);

        eventProcessorClient.start();
        boolean success = countDownLatch.await(10, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        assertTrue(success);
        assertTrue(closed.get());

        // This is one less because the processEvent is called before the end span call, so it is possible for
        // to reach this line without calling it the 5th time yet. (Timing issue.)
        verify(tracer, times(numberOfEvents)).start(eq("EventHubs.process"), any(), any(Context.class));
        verify(tracer, atLeast(numberOfEvents - 1)).end(isNull(), isNull(), any());
    }

    /**
     * Tests {@link EventProcessorClient} that processes events from an Event Hub configured with multiple partitions.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testWithMultiplePartitions() throws Exception {
        // Arrange
        final CountDownLatch count = new CountDownLatch(1);
        final Set<String> identifiers = new HashSet<>();
        identifiers.add("1");
        identifiers.add("2");
        identifiers.add("3");
        final EventPosition position = EventPosition.latest();

        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(EventHubClientBuilder.DEFAULT_PREFETCH_COUNT);

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1", "2", "3"));
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.createConsumer(anyString(), eq(EventHubClientBuilder.DEFAULT_PREFETCH_COUNT), eq(true)))
            .thenReturn(consumer1, consumer2, consumer3);

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(identifiers));
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");

        when(consumer1.receiveFromPartition(argThat(arg -> identifiers.remove(arg)), eq(position), any()))
            .thenReturn(Mono.fromRunnable(() -> count.countDown())
                .thenMany(Flux.just(getEvent(eventData1), getEvent(eventData2))));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);

        when(consumer2.receiveFromPartition(argThat(arg -> identifiers.remove(arg)), eq(position), any()))
            .thenReturn(Mono.fromRunnable(() -> count.countDown()).thenMany(Flux.just(getEvent(eventData3))));
        when(eventData3.getSequenceNumber()).thenReturn(1L);
        when(eventData3.getOffset()).thenReturn(1L);

        when(consumer3.receiveFromPartition(argThat(arg -> identifiers.remove(arg)), eq(position), any()))
            .thenReturn(Mono.fromRunnable(() -> count.countDown()).thenMany(Flux.just(getEvent(eventData4))));
        when(eventData4.getSequenceNumber()).thenReturn(1L);
        when(eventData4.getOffset()).thenReturn(1L);

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        // Act
        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            "test-consumer",
            TestPartitionProcessor::new, checkpointStore, false, ec -> { }, new HashMap<>(),
            1, null, false, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);
        eventProcessorClient.start();
        final boolean completed = count.await(10, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        // Assert
        Assertions.assertTrue(completed);
        StepVerifier.create(checkpointStore.listOwnership("test-ns", "test-eh", "test-consumer"))
            .expectNextCount(1).verifyComplete();

        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, times(1))
            .createConsumer(anyString(), anyInt(), eq(true));

        // We expected one to be removed.
        Assertions.assertEquals(2, identifiers.size());

        StepVerifier.create(checkpointStore.listOwnership("test-ns", "test-eh", "test-consumer"))
            .assertNext(po -> {
                String partitionId = po.getPartitionId();
                verify(consumer1, atLeastOnce()).receiveFromPartition(eq(partitionId), any(EventPosition.class), any());
            }).verifyComplete();
    }

    @Test
    public void testPrefetchCountSet() throws Exception {
        // Arrange
        final String consumerGroup = "my-consumer-group";
        final int prefetch = 15;

        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(prefetch);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(eq(consumerGroup), eq(prefetch), eq(true)))
            .thenReturn(consumer1);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2), getEvent(eventData3)));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData3.getSequenceNumber()).thenReturn(3L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);
        when(eventData3.getOffset()).thenReturn(150L);

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        testPartitionProcessor.countDownLatch = countDownLatch;

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, consumerGroup,
            () -> testPartitionProcessor, checkpointStore, false, ec -> { }, new HashMap<>(),
            2, Duration.ofSeconds(1), true, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);

        // Act
        eventProcessorClient.start();
        boolean completed = countDownLatch.await(10, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        // Assert
        assertTrue(completed);
        assertIterableEquals(testPartitionProcessor.receivedEventsCount, Arrays.asList(2, 1));

        verify(eventHubAsyncClient).createConsumer(eq(consumerGroup), eq(prefetch), eq(true));
    }

    @Test
    public void testDefaultPrefetch() throws Exception {
        // Arrange
        final String consumerGroup = "my-consumer-group";

        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(null);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(eq(consumerGroup), eq(EventHubClientBuilder.DEFAULT_PREFETCH_COUNT), eq(true)))
            .thenReturn(consumer1);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2), getEvent(eventData3)));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData3.getSequenceNumber()).thenReturn(3L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);
        when(eventData3.getOffset()).thenReturn(150L);

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        testPartitionProcessor.countDownLatch = countDownLatch;

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, consumerGroup,
            () -> testPartitionProcessor, checkpointStore, false, ec -> { }, new HashMap<>(),
            2, Duration.ofSeconds(1), true, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);

        // Act
        eventProcessorClient.start();
        boolean completed = countDownLatch.await(10, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        // Assert
        assertTrue(completed);
        assertIterableEquals(testPartitionProcessor.receivedEventsCount, Arrays.asList(2, 1));

        verify(eventHubAsyncClient).createConsumer(eq(consumerGroup), eq(EventHubClientBuilder.DEFAULT_PREFETCH_COUNT), eq(true));
    }

    @Test
    public void testBatchReceive() throws Exception {
        // Arrange
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), eq(true)))
            .thenReturn(consumer1);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2), getEvent(eventData3)));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData3.getSequenceNumber()).thenReturn(3L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);
        when(eventData3.getOffset()).thenReturn(150L);

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        testPartitionProcessor.countDownLatch = countDownLatch;

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, "test-consumer",
            () -> testPartitionProcessor, checkpointStore, false, ec -> { }, new HashMap<>(),
            2, Duration.ofSeconds(1), true, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);

        // Act
        eventProcessorClient.start();
        boolean completed = countDownLatch.await(10, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        // Assert
        assertTrue(completed);
        assertIterableEquals(testPartitionProcessor.receivedEventsCount, Arrays.asList(2, 1));
    }

    @Test
    public void testBatchReceiveHeartBeat() throws InterruptedException {
        // Arrange
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), eq(true)))
            .thenReturn(consumer1);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2)).delayElements(Duration.ofSeconds(3)));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData3.getSequenceNumber()).thenReturn(3L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);
        when(eventData3.getOffset()).thenReturn(150L);

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        testPartitionProcessor.countDownLatch = countDownLatch;

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, "test-consumer",
            () -> testPartitionProcessor, checkpointStore, false, ec -> { }, new HashMap<>(),
            2, Duration.ofSeconds(1), true, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);

        // Act
        eventProcessorClient.start();
        boolean completed = countDownLatch.await(20, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        // Assert
        assertTrue(completed);
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(0));
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleEventReceiveHeartBeat() throws InterruptedException {
        // Arrange
        Tracer tracer = mock(Tracer.class);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(null);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), eq(true)))
            .thenReturn(consumer1);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2)).delayElements(Duration.ofSeconds(3)));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData1.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639208));
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData2.getOffset()).thenReturn(100L);
        when(eventData2.getEnqueuedTime()).thenReturn(Instant.ofEpochSecond(1560639208));

        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        Map<String, Object> properties = new HashMap<>();
        properties.put(DIAGNOSTIC_ID_KEY, diagnosticId);

        when(eventData1.getProperties()).thenReturn(properties);
        when(eventData2.getProperties()).thenReturn(properties);
        when(tracer.extractContext(any())).thenAnswer(
            invocation -> {
                Function<String, String> getter = invocation.getArgument(0, Function.class);
                assertEquals(diagnosticId, getter.apply(DIAGNOSTIC_ID_KEY));
                return invocation.getArgument(1, Context.class).addData(SPAN_CONTEXT_KEY, "value");
            }
        );
        when(tracer.start(eq("EventHubs.process"), any(), any(Context.class))).thenAnswer(
            invocation -> invocation.getArgument(1, Context.class)
                    .addData(PARENT_TRACE_CONTEXT_KEY, "value2"));

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        testPartitionProcessor.countDownLatch = countDownLatch;

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder, "test-consumer",
            () -> testPartitionProcessor, checkpointStore, false, ec -> { }, new HashMap<>(),
            1, Duration.ofSeconds(1), false, Duration.ofSeconds(10), Duration.ofMinutes(1), LoadBalancingStrategy.BALANCED);
        eventProcessorClient.start();
        boolean completed = countDownLatch.await(20, TimeUnit.SECONDS);
        eventProcessorClient.stop();
        assertTrue(completed);
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(0));
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(1));
    }

    private PartitionEvent getEvent(EventData event) {
        PartitionContext context = new PartitionContext("test-ns", "foo", "bar", "baz");
        return new PartitionEvent(context, event, null);
    }

    private void assertStartOptions(StartSpanOptions startOpts, int linkCount) {
        assertEquals(SpanKind.CONSUMER, startOpts.getSpanKind());
        assertEquals("test-eh", startOpts.getAttributes().get(ENTITY_PATH_KEY));
        assertEquals("test-ns", startOpts.getAttributes().get(HOST_NAME_KEY));

        if (linkCount == 0) {
            assertTrue(startOpts.getAttributes().containsKey(MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME));
            assertNull(startOpts.getLinks());
        } else {
            assertEquals(linkCount, startOpts.getLinks().size());
            for (TracingLink link : startOpts.getLinks()) {
                assertTrue(link.getAttributes().containsKey(MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME));
            }
        }
    }

    private static final class TestPartitionProcessor extends PartitionProcessor {
        List<Integer> receivedEventsCount = new ArrayList<>();
        CountDownLatch countDownLatch;

        @Override
        public void processEvent(EventContext eventContext) {
            if (eventContext.getEventData() != null) {
                receivedEventsCount.add(1);
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                    eventContext.updateCheckpoint();
                }
            } else {
                receivedEventsCount.add(0);
            }
        }

        @Override
        public void processEventBatch(EventBatchContext eventBatchContext) {
            receivedEventsCount.add(eventBatchContext.getEvents().size());
            eventBatchContext.getEvents().forEach(eventContext -> {
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }
            });
            eventBatchContext.updateCheckpoint();
        }

        @Override
        public void processError(ErrorContext errorContext) {
            // do nothing
            return;
        }
    }

}
