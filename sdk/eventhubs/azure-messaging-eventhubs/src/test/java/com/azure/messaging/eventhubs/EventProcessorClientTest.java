// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracingLink;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.implementation.instrumentation.*;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Exceptions;
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
import java.util.stream.Stream;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static com.azure.messaging.eventhubs.TestUtils.assertAllAttributes;
import static com.azure.messaging.eventhubs.TestUtils.getSpanName;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.DIAGNOSTIC_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.PROCESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.CHECKPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EventProcessorClient}.
 */
public class EventProcessorClientTest {
    private static final String HOSTNAME = "test-ns";
    private static final String EVENT_HUB_NAME = "test-eh";
    private static final String CONSUMER_GROUP = "test-consumer";
    private static final String PARTITION_ID = "1";
    private AutoCloseable mocksDisposable;

    @Mock
    private EventHubClientBuilder eventHubClientBuilder;

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubConsumerAsyncClient consumer1, consumer2, consumer3;

    @Mock
    private EventData eventData1, eventData2, eventData3, eventData4;

    private final EventProcessorClientOptions processorOptions = new EventProcessorClientOptions();

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
        // Arrange
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(null);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), anyBoolean()))
            .thenReturn(consumer1);
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1), getEvent(eventData2)));

        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();

        final long beforeTest = System.currentTimeMillis();

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(false)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        // Act
        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        eventProcessorClient.start();
        TimeUnit.SECONDS.sleep(10);

        // Assert
        assertNotNull(eventProcessorClient.getIdentifier());

        StepVerifier.create(checkpointStore.listOwnership(HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP))
            .expectNextCount(1).verifyComplete();

        StepVerifier.create(checkpointStore.listOwnership(HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP))
            .assertNext(partitionOwnership -> {
                assertEquals(PARTITION_ID, partitionOwnership.getPartitionId(), "Partition");
                assertEquals(CONSUMER_GROUP, partitionOwnership.getConsumerGroup(), "Consumer");
                assertEquals(EVENT_HUB_NAME, partitionOwnership.getEventHubName(), "EventHub name");
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

        StepVerifier.create(checkpointStore.listOwnership(HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP))
            .assertNext(partitionOwnership -> {
                assertEquals(PARTITION_ID, partitionOwnership.getPartitionId(), "Partition");
                assertEquals(CONSUMER_GROUP, partitionOwnership.getConsumerGroup(), "Consumer");
                assertEquals(EVENT_HUB_NAME, partitionOwnership.getEventHubName(), "EventHub name");
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
    public void testProcessSpansAndMetrics() throws Exception {
        //Arrange
        final Tracer tracer = mock(Tracer.class);
        final TestMeter meter = new TestMeter();
        when(tracer.isEnabled()).thenReturn(true);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer);
        when(eventHubClientBuilder.createMeter()).thenReturn(meter);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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
        when(tracer.extractContext(any())).thenAnswer(invocation -> {
            Function<String, String> consumer = invocation.getArgument(0, Function.class);
            assertEquals(diagnosticId, consumer.apply("traceparent"));
            assertNull(consumer.apply("tracestate"));
            return new Context(SPAN_CONTEXT_KEY, "value");
        });
        final String expectedProcessSpanName = getSpanName(PROCESS, EVENT_HUB_NAME);
        when(tracer.start(eq(expectedProcessSpanName), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(PROCESS, invocation.getArgument(1, StartSpanOptions.class), 1);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
            }
        );

        final String expectedSettleSpanName = getSpanName(CHECKPOINT, EVENT_HUB_NAME);
        when(tracer.start(eq(expectedSettleSpanName), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
                invocation -> {
                    assertCheckpointStartOptions(invocation.getArgument(1, StartSpanOptions.class));
                    return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
                }
        );

        when(tracer.makeSpanCurrent(any())).thenReturn(() -> { });
        // processor span ends after TestPartitionProcessor latch counts down
        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(tracer).end(any(), any(), any());

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(false)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            TestPartitionProcessor::new, checkpointStore, EventProcessorClientTest::noopConsumer, tracer, meter,
            processorOptions);

        eventProcessorClient.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        eventProcessorClient.stop();

        //Assert
        verify(tracer, times(1)).extractContext(any());
        verify(tracer, times(1)).start(eq(expectedProcessSpanName), any(), any(Context.class));
        verify(tracer, times(1)).start(eq(expectedSettleSpanName), any(), any(Context.class));

        assertProcessMetrics(meter, 1, null);
    }

    /**
     * Tests process start spans invoked for {@link EventProcessorClient}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testProcessBatchTracesAndMetrics() throws Exception {
        //Arrange
        final Tracer tracer = mock(Tracer.class);
        final TestMeter meter = new TestMeter();
        when(tracer.isEnabled()).thenReturn(true);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer);
        when(eventHubClientBuilder.createMeter()).thenReturn(meter);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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
        when(tracer.extractContext(any())).thenAnswer(invocation -> {
            Function<String, String> consumer = invocation.getArgument(0, Function.class);
            String traceparent = consumer.apply("traceparent");
            if (counter.getAndIncrement() == 0) {
                assertEquals(diagnosticId1, traceparent);
            } else {
                assertEquals(diagnosticId2, traceparent);
            }
            return new Context(SPAN_CONTEXT_KEY, traceparent);
        });

        final String expectedProcessSpanName = getSpanName(PROCESS, EVENT_HUB_NAME);
        when(tracer.start(eq(expectedProcessSpanName), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(PROCESS, invocation.getArgument(1, StartSpanOptions.class), 2);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
            }
        );

        final String expectedCheckpointSpanName = getSpanName(CHECKPOINT, EVENT_HUB_NAME);
        when(tracer.start(eq(expectedCheckpointSpanName), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
                invocation -> {
                    assertCheckpointStartOptions(invocation.getArgument(1, StartSpanOptions.class));
                    return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value3");
                }
        );

        when(tracer.makeSpanCurrent(any())).thenReturn(() -> { });

        // processor span ends after TestPartitionProcessor latch counts down
        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(tracer).end(any(), any(), any());

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(2)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(true)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            TestPartitionProcessor::new, checkpointStore, EventProcessorClientTest::noopConsumer, tracer, meter,
            processorOptions);

        eventProcessorClient.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        eventProcessorClient.stop();

        //Assert
        verify(tracer, times(2)).extractContext(any());
        verify(tracer, times(1)).start(eq(expectedProcessSpanName), any(), any(Context.class));
        verify(tracer, times(1)).start(eq(expectedCheckpointSpanName), any(), any(Context.class));

        assertProcessMetrics(meter, 2, null);
    }

    public static Stream<Arguments> errorSource() {
        Throwable inner = new RuntimeException("test");
        final ArrayList<Arguments> arguments = new ArrayList<>();
        arguments.add(Arguments.of(inner, inner.getClass().getName()));
        arguments.add(Arguments.of(Exceptions.propagate(inner), inner.getClass().getName()));
        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource("errorSource")
    @SuppressWarnings("unchecked")
    public void testProcessWithErrorTracesAndMetrics(RuntimeException error, String expectedErrorType) throws Exception {
        //Arrange
        final Tracer tracer = mock(Tracer.class);
        final TestMeter meter = new TestMeter();
        when(tracer.isEnabled()).thenReturn(true);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer);
        when(eventHubClientBuilder.createMeter()).thenReturn(meter);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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

        when(tracer.extractContext(any())).thenReturn(Context.NONE);

        final String expectedProcessSpanName = getSpanName(PROCESS, EVENT_HUB_NAME);
        when(tracer.start(eq(expectedProcessSpanName), any(StartSpanOptions.class), any(Context.class)))
            .thenReturn(new Context(PARENT_TRACE_CONTEXT_KEY, "span"));
        when(tracer.makeSpanCurrent(any())).thenReturn(() -> { });

        // processor span ends after TestPartitionProcessor latch counts down
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(tracer).end(any(), any(), any());

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor processor = new TestPartitionProcessor(new CountDownLatch(1), error);
        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(2)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(true)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> processor, checkpointStore, EventProcessorClientTest::noopConsumer, tracer, meter,
            processorOptions);

        eventProcessorClient.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        eventProcessorClient.stop();

        //Assert
        verify(tracer, times(2)).extractContext(any());
        verify(tracer, times(1)).start(eq(expectedProcessSpanName), any(), any(Context.class));
        verify(tracer, times(1)).end(eq(expectedErrorType), same(error), any(Context.class));

        assertProcessMetrics(meter, 2, expectedErrorType);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTracingMetricsEmptyBatch() throws Exception {
        //Arrange
        final Tracer tracer = mock(Tracer.class);
        final TestMeter meter = new TestMeter();
        when(tracer.isEnabled()).thenReturn(true);
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(tracer);
        when(eventHubClientBuilder.createMeter()).thenReturn(meter);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyInt(), eq(true)))
            .thenReturn(consumer1);
        when(eventHubAsyncClient.getIdentifier()).thenReturn("my-client-identifier");
        when(consumer1.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.generate(sync -> { }));
        when(tracer.makeSpanCurrent(any())).thenReturn(() -> { });
        // processor span ends after TestPartitionProcessor latch counts down
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(tracer).end(any(), any(), any());

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        processorOptions.setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(2)
            .setMaxWaitTime(Duration.ofMillis(1))
            .setBatchReceiveMode(true)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            TestPartitionProcessor::new, checkpointStore, EventProcessorClientTest::noopConsumer, tracer, meter,
            processorOptions);

        eventProcessorClient.start();
        assertFalse(latch.await(2, TimeUnit.SECONDS));
        eventProcessorClient.stop();

        //Assert
        verify(tracer, never()).start(anyString(), any(), any(Context.class));
        assertEquals(0, meter.getCounters().get("messaging.client.consumed.messages").getMeasurements().size());
        assertEquals(0, meter.getHistograms().get("messaging.process.duration").getMeasurements().size());
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
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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

        final String expectedProcessSpanName = getSpanName(PROCESS, EVENT_HUB_NAME);
        when(tracer.start(eq(expectedProcessSpanName), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(PROCESS, invocation.getArgument(1, StartSpanOptions.class), 1);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
            }
        );

        final String expectedSettleSpanName = getSpanName(CHECKPOINT, EVENT_HUB_NAME);
        when(tracer.start(eq(expectedSettleSpanName), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
                invocation -> {
                    assertCheckpointStartOptions(invocation.getArgument(1, StartSpanOptions.class));
                    return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "value2");
                }
        );

        AtomicBoolean closed = new AtomicBoolean(false);
        when(tracer.makeSpanCurrent(any())).thenReturn(() -> closed.set(true));

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();

        TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor(numberOfEvents);

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(false)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        //Act
        EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, tracer, null,
            processorOptions);

        eventProcessorClient.start();
        boolean success = testPartitionProcessor.countDownLatch.await(10, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        assertTrue(success);
        assertTrue(closed.get());

        verify(tracer, times(numberOfEvents)).start(eq(expectedProcessSpanName), any(), any(Context.class));

        // This is one less because the latch happens at the start of process callback
        // and checkpoint/process spans are reported after

        verify(tracer, atLeast(numberOfEvents - 1)).start(eq(expectedSettleSpanName), any(), any(Context.class));
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
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.createConsumer(anyString(), eq(EventHubClientBuilder.DEFAULT_PREFETCH_COUNT), eq(true)))
            .thenReturn(consumer1, consumer2, consumer3);

        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.fromIterable(identifiers));
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
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

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(false)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        // Act
        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            TestPartitionProcessor::new, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        eventProcessorClient.start();
        final boolean completed = count.await(10, TimeUnit.SECONDS);
        eventProcessorClient.stop();

        // Assert
        Assertions.assertTrue(completed);
        StepVerifier.create(checkpointStore.listOwnership(HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP))
            .expectNextCount(1).verifyComplete();

        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, times(1))
            .createConsumer(anyString(), anyInt(), eq(true));

        // We expected one to be removed.
        Assertions.assertEquals(2, identifiers.size());

        StepVerifier.create(checkpointStore.listOwnership(HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP))
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
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor(3);

        processorOptions.setConsumerGroup(consumerGroup)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(2)
            .setMaxWaitTime(Duration.ofSeconds(1))
            .setBatchReceiveMode(true)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        // Act
        eventProcessorClient.start();
        boolean completed = testPartitionProcessor.countDownLatch.await(10, TimeUnit.SECONDS);
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
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor(3);

        processorOptions.setConsumerGroup(consumerGroup)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(2)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(true)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        // Act
        eventProcessorClient.start();
        boolean completed = testPartitionProcessor.countDownLatch.await(10, TimeUnit.SECONDS);
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
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor(3);

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(2)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(true)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        // Act
        eventProcessorClient.start();
        boolean completed = testPartitionProcessor.countDownLatch.await(10, TimeUnit.SECONDS);
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
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor(1);

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(2)
            .setMaxWaitTime(Duration.ofSeconds(1))
            .setBatchReceiveMode(true)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        // Act
        eventProcessorClient.start();
        boolean completed = testPartitionProcessor.countDownLatch.await(20, TimeUnit.SECONDS);
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
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(null);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor(1);

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(Duration.ofSeconds(1))
            .setBatchReceiveMode(false)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        eventProcessorClient.start();
        boolean completed = testPartitionProcessor.countDownLatch.await(20, TimeUnit.SECONDS);
        eventProcessorClient.stop();
        assertTrue(completed);
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(0));
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(1));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void passesInstrumentedCheckpointStore() throws InterruptedException {
        // Arrange
        when(eventHubClientBuilder.getPrefetchCount()).thenReturn(DEFAULT_PREFETCH_COUNT);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubClientBuilder.createTracer()).thenReturn(null);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn(HOSTNAME);
        when(eventHubAsyncClient.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just(PARTITION_ID));
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

        final SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor(1);

        processorOptions.setConsumerGroup(CONSUMER_GROUP)
            .setTrackLastEnqueuedEventProperties(false)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(1)
            .setMaxWaitTime(Duration.ofSeconds(1))
            .setBatchReceiveMode(false)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventProcessorClient eventProcessorClient = new EventProcessorClient(eventHubClientBuilder,
            () -> testPartitionProcessor, checkpointStore, EventProcessorClientTest::noopConsumer, null, null,
            processorOptions);

        eventProcessorClient.start();
        boolean completed = testPartitionProcessor.countDownLatch.await(20, TimeUnit.SECONDS);
        eventProcessorClient.stop();
        assertTrue(completed);
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(0));
        assertTrue(testPartitionProcessor.receivedEventsCount.contains(1));
    }

    static void noopConsumer(ErrorContext unused) {
    }

    private PartitionEvent getEvent(EventData event) {
        PartitionContext context = new PartitionContext(HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);
        return new PartitionEvent(context, event, null);
    }

    private void assertStartOptions(OperationName operationName, StartSpanOptions startOpts, int linkCount) {
        assertEquals(SpanKind.CONSUMER, startOpts.getSpanKind());
        assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, PARTITION_ID, CONSUMER_GROUP, null,
            operationName, startOpts.getAttributes());

        if (linkCount == 0) {
            assertTrue(startOpts.getAttributes().containsKey(MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME));
            assertNull(startOpts.getLinks());
        } else {
            assertEquals(linkCount, startOpts.getLinks().size());
            if (linkCount == 1) {
                assertTrue(startOpts.getAttributes().containsKey(MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME));
            } else {
                for (TracingLink link : startOpts.getLinks()) {
                    assertTrue(link.getAttributes().containsKey(MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME));
                }
            }
        }
    }

    private void assertCheckpointStartOptions(StartSpanOptions startOpts) {
        assertEquals(SpanKind.INTERNAL, startOpts.getSpanKind());
        assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, PARTITION_ID, CONSUMER_GROUP, null,
            CHECKPOINT, startOpts.getAttributes());
        assertNull(startOpts.getLinks());
    }

    private static void assertProcessMetrics(TestMeter meter, int batchSize, String expectedErrorType) {
        TestCounter eventCounter = meter.getCounters().get("messaging.client.consumed.messages");
        assertNotNull(eventCounter);
        assertEquals(1, eventCounter.getMeasurements().size());
        assertEquals(batchSize, eventCounter.getMeasurements().get(0).getValue());
        assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, PARTITION_ID, CONSUMER_GROUP, expectedErrorType,
            PROCESS, eventCounter.getMeasurements().get(0).getAttributes());

        TestHistogram processDuration = meter.getHistograms().get("messaging.process.duration");
        assertNotNull(processDuration);
        assertEquals(1, processDuration.getMeasurements().size());
        assertNotNull(processDuration.getMeasurements().get(0).getValue());
        assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, PARTITION_ID, CONSUMER_GROUP, expectedErrorType,
            PROCESS, processDuration.getMeasurements().get(0).getAttributes());

        if (expectedErrorType == null) {
            TestHistogram checkpointDuration = meter.getHistograms().get("messaging.client.operation.duration");
            assertNotNull(checkpointDuration);
            assertEquals(1, checkpointDuration.getMeasurements().size());
            assertNotNull(checkpointDuration.getMeasurements().get(0).getValue());
            assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, PARTITION_ID, CONSUMER_GROUP, null,
                CHECKPOINT, checkpointDuration.getMeasurements().get(0).getAttributes());
        }
    }

    private static final class TestPartitionProcessor extends PartitionProcessor {
        List<Integer> receivedEventsCount = new ArrayList<>();
        final CountDownLatch countDownLatch;
        private final RuntimeException error;

        TestPartitionProcessor() {
            this(null, null);
        }

        TestPartitionProcessor(int count) {
            this(new CountDownLatch(count), null);
        }

        TestPartitionProcessor(CountDownLatch countDownLatch, RuntimeException error) {
            this.countDownLatch = countDownLatch;
            this.error = error;
        }

        @Override
        public void processEvent(EventContext eventContext) {
            if (eventContext.getEventData() != null) {
                receivedEventsCount.add(1);
                if (countDownLatch != null) {
                    countDownLatch.countDown();

                    if (error != null) {
                        throw error;
                    }
                }

                eventContext.updateCheckpoint();
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

            if (error != null) {
                throw error;
            }

            eventBatchContext.updateCheckpoint();
        }

        @Override
        public void processError(ErrorContext errorContext) {
            // do nothing
            return;
        }
    }

}
