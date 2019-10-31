// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link EventProcessor}.
 */
public class EventProcessorTest {

    @Mock
    private EventHubClientBuilder eventHubClientBuilder;

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubAsyncConsumer consumer1, consumer2, consumer3;

    @Mock
    private EventData eventData1, eventData2, eventData3, eventData4;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void teardown() {
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
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Tests all the happy cases for {@link EventProcessor}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testWithSimplePartitionProcessor() throws Exception {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);

        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(consumer1.receive()).thenReturn(Flux.just(eventData1, eventData2));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);

        final InMemoryEventProcessorStore eventProcessorStore = new InMemoryEventProcessorStore();
        final TestPartitionProcessor testPartitionProcessor = new TestPartitionProcessor();

        final long beforeTest = System.currentTimeMillis();
        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        when(tracer1.extractContext(eq(diagnosticId), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value");
            }
        );
        when(tracer1.start(eq("Azure.eventhubs.process"), any(), eq(ProcessKind.PROCESS))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value1")
                    .addData("scope", (Closeable) () -> {
                    })
                    .addData(PARENT_SPAN_KEY, "value2");
            }
        );

        // Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubClientBuilder, "test-consumer",
            () -> testPartitionProcessor, EventPosition.earliest(), eventProcessorStore, tracerProvider);
        eventProcessor.start();
        TimeUnit.SECONDS.sleep(10);
        eventProcessor.stop();

        // Assert
        assertNotNull(eventProcessor.getIdentifier());

        StepVerifier.create(eventProcessorStore.listOwnership("ns", "test-eh", "test-consumer"))
            .expectNextCount(1).verifyComplete();

        StepVerifier.create(eventProcessorStore.listOwnership("ns", "test-eh", "test-consumer"))
            .assertNext(partitionOwnership -> {
                assertEquals("Partition", "1", partitionOwnership.getPartitionId());
                assertEquals("Consumer", "test-consumer", partitionOwnership.getConsumerGroupName());
                assertEquals("EventHub name", "test-eh", partitionOwnership.getEventHubName());
                assertEquals("Sequence number", 2, (long) partitionOwnership.getSequenceNumber());
                assertEquals("Offset", Long.valueOf(100), partitionOwnership.getOffset());
                assertEquals("OwnerId", eventProcessor.getIdentifier(), partitionOwnership.getOwnerId());
                assertTrue("LastModifiedTime", partitionOwnership.getLastModifiedTime() >= beforeTest);
                assertTrue("LastModifiedTime", partitionOwnership.getLastModifiedTime() <= System.currentTimeMillis());
                assertNotNull(partitionOwnership.getETag());
            }).verifyComplete();

        verify(eventHubAsyncClient, atLeastOnce()).getPartitionIds();
        verify(eventHubAsyncClient, atLeastOnce())
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class));
        verify(consumer1, atLeastOnce()).receive();
        verify(consumer1, atLeastOnce()).close();
    }

    /**
     * Tests {@link EventProcessor} with a partition processor that throws an exception when processing an event.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testWithFaultyPartitionProcessor() throws Exception {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(consumer1.receive()).thenReturn(Flux.just(eventData1));
        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";

        final InMemoryEventProcessorStore eventProcessorStore = new InMemoryEventProcessorStore();
        final FaultyPartitionProcessor faultyPartitionProcessor = new FaultyPartitionProcessor();

        when(tracer1.extractContext(eq(diagnosticId), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value");
            }
        );
        when(tracer1.start(eq("Azure.eventhubs.process"), any(), eq(ProcessKind.PROCESS))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value1")
                    .addData("scope", (Closeable) () -> {
                    })
                    .addData(PARENT_SPAN_KEY, "value2");
            }
        );
        // Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubClientBuilder, "test-consumer",
            () -> faultyPartitionProcessor, EventPosition.earliest(), eventProcessorStore, tracerProvider);

        eventProcessor.start();
        TimeUnit.SECONDS.sleep(10);
        eventProcessor.stop();

        // Assert
        assertTrue(faultyPartitionProcessor.error);
    }

    /**
     * Tests process start spans error messages invoked for {@link EventProcessor}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testErrorProcessSpans() throws Exception {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);

        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        Map<String, Object> properties = new HashMap<>();
        properties.put(DIAGNOSTIC_ID_KEY, diagnosticId);

        when(eventData1.getProperties()).thenReturn(properties);
        when(consumer1.receive()).thenReturn(Flux.just(eventData1));
        when(tracer1.extractContext(eq(diagnosticId), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value");
            }
        );
        when(tracer1.start(eq("Azure.eventhubs.process"), any(), eq(ProcessKind.PROCESS))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value1")
                    .addData("scope", (Closeable) () -> {
                    })
                    .addData(PARENT_SPAN_KEY, "value2");
            }
        );

        final InMemoryEventProcessorStore eventProcessorStore = new InMemoryEventProcessorStore();

        //Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubClientBuilder, "test-consumer",
            FaultyPartitionProcessor::new, EventPosition.earliest(), eventProcessorStore, tracerProvider);
        eventProcessor.start();
        TimeUnit.SECONDS.sleep(10);
        eventProcessor.stop();

        //Assert
        verify(tracer1, times(1)).extractContext(eq(diagnosticId), any());
        verify(tracer1, times(1)).start(eq("Azure.eventhubs.process"), any(), eq(ProcessKind.PROCESS));
        verify(tracer1, times(1)).end(eq(""), any(IllegalStateException.class), any());
    }

    /**
     * Tests process start spans invoked for {@link EventProcessor}.
     *
     * @throws Exception if an error occurs while running the test.
     */
    @Test
    public void testProcessSpans() throws Exception {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1"));
        when(eventHubAsyncClient
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);

        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";
        Map<String, Object> properties = new HashMap<>();
        properties.put(DIAGNOSTIC_ID_KEY, diagnosticId);

        when(eventData1.getProperties()).thenReturn(properties);
        when(consumer1.receive()).thenReturn(Flux.just(eventData1));
        when(tracer1.extractContext(eq(diagnosticId), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value");
            }
        );
        when(tracer1.start(eq("Azure.eventhubs.process"), any(), eq(ProcessKind.PROCESS))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value1").addData("scope", (Closeable) () -> {
                    return;
                }).addData(PARENT_SPAN_KEY, "value2");
            }
        );

        final InMemoryEventProcessorStore eventProcessorStore = new InMemoryEventProcessorStore();

        //Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubClientBuilder, "test-consumer",
            TestPartitionProcessor::new, EventPosition.earliest(), eventProcessorStore, tracerProvider);

        eventProcessor.start();
        TimeUnit.SECONDS.sleep(10);
        eventProcessor.stop();

        //Assert
        verify(tracer1, times(1)).extractContext(eq(diagnosticId), any());
        verify(tracer1, times(1)).start(eq("Azure.eventhubs.process"), any(), eq(ProcessKind.PROCESS));
        verify(tracer1, times(1)).end(eq("success"), isNull(), any());
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
        final CountDownLatch count = new CountDownLatch(1);

        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1", "2", "3"));
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
        when(eventHubAsyncClient
            .createConsumer(anyString(), eq("1"), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer1);
        when(consumer1.receive()).thenReturn(
            Mono.fromRunnable(() -> count.countDown()).thenMany(Flux.just(eventData1, eventData2)));
        when(eventData1.getSequenceNumber()).thenReturn(1L);
        when(eventData2.getSequenceNumber()).thenReturn(2L);
        when(eventData1.getOffset()).thenReturn(1L);
        when(eventData2.getOffset()).thenReturn(100L);

        when(eventHubAsyncClient
            .createConsumer(anyString(), eq("2"), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer2);
        when(consumer2.receive()).thenReturn(Mono.fromRunnable(() -> count.countDown()).thenMany(Flux.just(eventData3)));
        when(eventData3.getSequenceNumber()).thenReturn(1L);
        when(eventData3.getOffset()).thenReturn(1L);

        when(eventHubAsyncClient
            .createConsumer(anyString(), eq("3"), any(EventPosition.class), any(EventHubConsumerOptions.class)))
            .thenReturn(consumer3);
        when(consumer3.receive()).thenReturn(Mono.fromRunnable(() -> count.countDown()).thenMany(Flux.just(eventData4)));
        when(eventData4.getSequenceNumber()).thenReturn(1L);
        when(eventData4.getOffset()).thenReturn(1L);

        final InMemoryEventProcessorStore eventProcessorStore = new InMemoryEventProcessorStore();
        final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

        // Act
        final EventProcessor eventProcessor = new EventProcessor(eventHubClientBuilder,
            "test-consumer",
            TestPartitionProcessor::new, EventPosition.latest(), eventProcessorStore, tracerProvider);
        eventProcessor.start();
        final boolean completed = count.await(10, TimeUnit.SECONDS);
        eventProcessor.stop();

        // Assert
        Assert.assertTrue(completed);
        StepVerifier.create(eventProcessorStore.listOwnership("ns", "test-eh", "test-consumer"))
            .expectNextCount(1).verifyComplete();

        verify(eventHubAsyncClient, atLeast(1)).getPartitionIds();
        verify(eventHubAsyncClient, times(1))
            .createConsumer(anyString(), anyString(), any(EventPosition.class), any(EventHubConsumerOptions.class));

        StepVerifier.create(eventProcessorStore.listOwnership("ns", "test-eh", "test-consumer"))
            .assertNext(po -> {
                try {
                    if (po.getPartitionId().equals("1")) {
                        verify(consumer1, atLeastOnce()).receive();
                        verify(consumer1, atLeastOnce()).close();
                    } else if (po.getPartitionId().equals("2")) {
                        verify(consumer2, atLeastOnce()).receive();
                        verify(consumer2, atLeastOnce()).close();
                    } else {
                        verify(consumer3, atLeastOnce()).receive();
                        verify(consumer3, atLeastOnce()).close();
                    }
                } catch (IOException ex) {
                    fail("Failed to assert consumer close method invocation");
                }
            }).verifyComplete();
    }

    private static final class FaultyPartitionProcessor extends PartitionProcessor {

        boolean error;

        @Override
        public void processError(ErrorContext errorContext) {
            error = true;
        }

        @Override
        public Mono<Void> processEvent(PartitionEvent partitionEvent) {
            return Mono.error(new IllegalStateException());
        }
    }

    private static final class TestPartitionProcessor extends PartitionProcessor {

        @Override
        public Mono<Void> processEvent(PartitionEvent partitionEvent) {
            return partitionEvent.getPartitionContext().updateCheckpoint(partitionEvent.getEventData());
        }
    }

}
