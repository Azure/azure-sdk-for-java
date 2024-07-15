// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Isolated("Sets global TracingProvider.")
@Execution(ExecutionMode.SAME_THREAD)
@Disabled("Tracing tests need to be disabled until the discrepancy with the core is resolved.")
public class TracingIntegrationTests extends IntegrationTestBase {
    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private static final String PARTITION_ID = "0";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private TestSpanProcessor spanProcessor;
    private EventHubProducerAsyncClient producer;
    private EventHubConsumerAsyncClient consumer;
    private EventHubConsumerClient consumerSync;
    private EventProcessorClient processor;
    private Instant testStartTime;
    private EventData data;

    public TracingIntegrationTests() {
        super(new ClientLogger(TracingIntegrationTests.class));
    }

    @Override
    protected void beforeTest() {
        GlobalOpenTelemetry.resetForTest();

        spanProcessor = toClose(new TestSpanProcessor(getFullyQualifiedDomainName(), getEventHubName(), testName));
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(spanProcessor)
                    .build())
            .buildAndRegisterGlobal();

        createClients(null);

        testStartTime = Instant.now().minusSeconds(1);
        data = new EventData(CONTENTS_BYTES);
    }

    private void createClients(OpenTelemetrySdk otel) {
        dispose();
        ClientOptions options = new ClientOptions();
        if (otel != null) {
            options.setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(otel));
        }

        producer = toClose(new EventHubClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(options)
            .buildAsyncProducerClient());

        consumer = toClose(new EventHubClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(options)
            .consumerGroup("$Default")
            .buildAsyncConsumerClient());

        consumerSync = toClose(new EventHubClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(options)
            .consumerGroup("$Default")
            .buildConsumerClient());
    }

    @Override
    protected void afterTest() {
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    public void sendAndReceiveFromPartition() throws InterruptedException {
        AtomicReference<EventData> receivedMessage = new AtomicReference<>();
        AtomicReference<Span> receivedSpan = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || span.getName().equals("EventHubs.send"));
        toClose(consumer
            .receiveFromPartition(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime))
            .take(1)
            .subscribe(pe -> {
                if (receivedMessage.compareAndSet(null, pe.getData())) {
                    receivedSpan.compareAndSet(null, Span.current());
                }
            }));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, "EventHubs.message");
        assertMessageSpan(message.get(0), data);
        List<ReadableSpan> send = findSpans(spans, "EventHubs.send");
        assertSendSpan(send.get(0), Collections.singletonList(data), "EventHubs.send");

        List<ReadableSpan> received = findSpans(spans, "EventHubs.consume").stream()
            .filter(s -> s == receivedSpan.get()).collect(toList());
        assertConsumerSpan(received.get(0), receivedMessage.get(), "EventHubs.consume");
    }

    @Test
    public void sendAndReceive() throws InterruptedException {
        AtomicReference<EventData> receivedMessage = new AtomicReference<>();
        AtomicReference<Span> receivedSpan = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || span.getName().equals("EventHubs.send"));
        toClose(consumer
            .receive()
            .take(1)
            .subscribe(pe -> {
                if (receivedMessage.compareAndSet(null, pe.getData())) {
                    receivedSpan.compareAndSet(null, Span.current());
                }
            }));

        StepVerifier.create(producer.send(data, new SendOptions()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, "EventHubs.message");
        assertMessageSpan(message.get(0), data);
        List<ReadableSpan> send = findSpans(spans, "EventHubs.send");
        assertSendSpan(send.get(0), Collections.singletonList(data), "EventHubs.send");

        List<ReadableSpan> received = findSpans(spans, "EventHubs.consume").stream()
            .filter(s -> s == receivedSpan.get()).collect(toList());
        assertConsumerSpan(received.get(0), receivedMessage.get(), "EventHubs.consume");
    }

    @Test
    public void sendAndReceiveCustomProvider() throws InterruptedException {
        AtomicReference<EventData> receivedMessage = new AtomicReference<>();
        AtomicReference<Span> receivedSpan = new AtomicReference<>();

        TestSpanProcessor customSpanProcessor = toClose(new TestSpanProcessor(getFullyQualifiedDomainName(), getEventHubName(), "sendAndReceiveCustomProvider"));
        OpenTelemetrySdk otel = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder()
                    .addSpanProcessor(customSpanProcessor)
                    .build())
            .build();

        createClients(otel);

        CountDownLatch latch = new CountDownLatch(2);
        customSpanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || span.getName().equals("EventHubs.send"));

        toClose(consumer.receive()
            .take(1)
            .subscribe(pe -> {
                if (receivedMessage.compareAndSet(null, pe.getData())) {
                    receivedSpan.compareAndSet(null, Span.current());
                }
            }));

        StepVerifier.create(producer.send(data, new SendOptions()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        List<ReadableSpan> spans = customSpanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, "EventHubs.message");
        assertMessageSpan(message.get(0), data);
        List<ReadableSpan> send = findSpans(spans, "EventHubs.send");
        assertSendSpan(send.get(0), Collections.singletonList(data), "EventHubs.send");

        List<ReadableSpan> received = findSpans(spans, "EventHubs.consume").stream()
            .filter(s -> s == receivedSpan.get()).collect(toList());
        assertConsumerSpan(received.get(0), receivedMessage.get(), "EventHubs.consume");
    }

    @Test
    public void sendAndReceiveParallel() throws InterruptedException {
        int messageCount = 5;
        CountDownLatch latch = new CountDownLatch(messageCount);
        spanProcessor.notifyIfCondition(latch, span -> span.getName().equals("EventHubs.consume"));
        StepVerifier.create(consumer
                .receive()
                .take(messageCount)
                .doOnNext(pe -> {
                    String traceparent = (String) pe.getData().getProperties().get("traceparent");
                    if (traceparent != null) {
                        String traceId = Span.current().getSpanContext().getTraceId();

                        // context created for the message and current are the same
                        assertTrue(traceparent.startsWith("00-" + traceId));
                    }
                    assertFalse(((ReadableSpan) Span.current()).hasEnded());
                })
                .parallel(messageCount, 1)
                .runOn(Schedulers.boundedElastic(), 2))
            .expectNextCount(messageCount)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.send(data, new SendOptions()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(latch.await(20, TimeUnit.SECONDS));
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> received = findSpans(spans, "EventHubs.consume");
        assertTrue(messageCount <= received.size());
    }

    @Test
    public void sendBuffered() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        spanProcessor.notifyIfCondition(latch, span -> span.getName().equals("EventHubs.consume") || span.getName().equals("EventHubs.send"));

        EventHubBufferedProducerAsyncClient bufferedProducer = toClose(new EventHubBufferedProducerClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .onSendBatchFailed(failed -> fail("Exception occurred while sending messages." + failed.getThrowable()))
            .maxEventBufferLengthPerPartition(2)
            .maxWaitTime(Duration.ofSeconds(5))
            .onSendBatchSucceeded(b -> {
                logger.info("Batch published. partitionId[{}]", b.getPartitionId());
            })
            .buildAsyncClient());

        Instant start = Instant.now();
        EventData event1 = new EventData("1");
        EventData event2 = new EventData("2");

        // Using a specific partition in the case that an epoch receiver was created
        // (i.e. EventHubConsumerAsyncClientIntegrationTest), which this scenario will fail when trying to create a
        // receiver.
        SendOptions sendOptions = new SendOptions().setPartitionId("3");
        Boolean partitionIdExists = bufferedProducer.getPartitionIds()
            .any(id -> id.equals(sendOptions.getPartitionId()))
            .block(Duration.ofSeconds(30));

        assertNotNull(partitionIdExists, "Cannot be null. Partition id: " + sendOptions.getPartitionId());
        assertTrue(partitionIdExists, "Event Hubs does not contain partition id: " + sendOptions.getPartitionId());

        StepVerifier.create(Mono.when(
            bufferedProducer.enqueueEvent(event1, sendOptions), bufferedProducer.enqueueEvent(event2, sendOptions)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(consumer
                .receiveFromPartition(sendOptions.getPartitionId(), EventPosition.fromEnqueuedTime(start))
                .map(e -> {
                    logger.atInfo()
                        .addKeyValue("event", e.getData().getBodyAsString())
                        .addKeyValue("traceparent", e.getData().getProperties().get("traceparent"))
                        .log("received event");
                    return e;
                })
                .take(2)
                .then())
            .expectComplete()
            .verify(Duration.ofSeconds(60));

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, "EventHubs.message");
        assertMessageSpan(message.get(0), event1);
        assertMessageSpan(message.get(1), event2);

        List<ReadableSpan> send = findSpans(spans, "EventHubs.send");
        assertSendSpan(send.get(0), Arrays.asList(event1, event2), "EventHubs.send");

        List<ReadableSpan> received = findSpans(spans, "EventHubs.consume");
        assertEquals(2, received.size());
    }

    @Test
    public void syncReceive() {
        StepVerifier.create(producer.createBatch(new CreateBatchOptions().setPartitionId(PARTITION_ID))
                .map(b -> {
                    b.tryAdd(new EventData(CONTENTS_BYTES));
                    b.tryAdd(new EventData(CONTENTS_BYTES));
                    return b;
                })
                .flatMap(b -> producer.send(b)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        List<PartitionEvent> receivedMessages = consumerSync.receiveFromPartition(PARTITION_ID, 2, EventPosition.fromEnqueuedTime(testStartTime), Duration.ofSeconds(10))
            .stream().collect(toList());

        assertEquals(2, receivedMessages.size());
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertEquals(0, findSpans(spans, "EventHubs.process").size());

        List<ReadableSpan> received = findSpans(spans, "EventHubs.receiveFromPartition");
        assertSyncConsumerSpan(received.get(0), receivedMessages, "EventHubs.receiveFromPartition");
    }

    @Test
    public void syncReceiveWithOptions() {
        StepVerifier.create(producer.createBatch(new CreateBatchOptions().setPartitionId(PARTITION_ID))
                .map(b -> {
                    b.tryAdd(new EventData(CONTENTS_BYTES));
                    b.tryAdd(new EventData(CONTENTS_BYTES));
                    return b;
                })
                .flatMap(b -> producer.send(b)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        List<PartitionEvent> receivedMessages = consumerSync.receiveFromPartition(PARTITION_ID, 2,
                EventPosition.fromEnqueuedTime(testStartTime), Duration.ofSeconds(10), new ReceiveOptions())
            .stream().collect(toList());

        assertEquals(2, receivedMessages.size());
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertEquals(0, findSpans(spans, "EventHubs.process").size());

        List<ReadableSpan> received = findSpans(spans, "EventHubs.receiveFromPartition");
        assertSyncConsumerSpan(received.get(0), receivedMessages, "EventHubs.receiveFromPartition");
    }

    @Test
    public void syncReceiveTimeout() {
        List<PartitionEvent> receivedMessages = consumerSync.receiveFromPartition(PARTITION_ID, 2,
                EventPosition.fromEnqueuedTime(testStartTime), Duration.ofSeconds(1))
            .stream().collect(toList());

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertEquals(0, findSpans(spans, "EventHubs.process").size());

        List<ReadableSpan> received = findSpans(spans, "EventHubs.receiveFromPartition");
        assertSyncConsumerSpan(received.get(0), receivedMessages, "EventHubs.receiveFromPartition");
    }

    @Test
    public void sendAndProcess() throws InterruptedException {
        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<EventData> receivedMessage = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get() || span.getName().equals("EventHubs.send"));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
        processor = new EventProcessorClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .eventHubName(getEventHubName())
            .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEvent(ec -> {
                if (currentInProcess.compareAndSet(null, Span.current())) {
                    receivedMessage.compareAndSet(null, ec.getEventData());
                }
                ec.updateCheckpoint();
            })
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();

        toClose((Closeable) () -> processor.stop());
        processor.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        processor.stop();

        assertTrue(currentInProcess.get().getSpanContext().isValid());
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, "EventHubs.message");
        assertMessageSpan(message.get(0), data);

        List<ReadableSpan> send = findSpans(spans, "EventHubs.send");
        assertSendSpan(send.get(0), Collections.singletonList(data), "EventHubs.send");

        List<ReadableSpan> processed = findSpans(spans, "EventHubs.process")
            .stream().filter(p -> p == currentInProcess.get()).collect(toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), receivedMessage.get(), "EventHubs.process");
    }

    @Test
    @SuppressWarnings("try")
    public void sendNotInstrumentedAndProcess() throws InterruptedException {
        EventHubProducerAsyncClient notInstrumentedProducer = toClose(new EventHubClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(new ClientOptions().setTracingOptions(new TracingOptions().setEnabled(false)))
            .buildAsyncProducerClient());

        EventData message1 = new EventData(CONTENTS_BYTES);
        EventData message2 = new EventData(CONTENTS_BYTES);
        List<EventData> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span.getName().equals("EventHubs.process") && !span.getParentSpanContext().isValid());
        StepVerifier.create(notInstrumentedProducer.send(Arrays.asList(message1, message2), new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertNull(message1.getProperties().get("traceparent"));
        assertNull(message2.getProperties().get("traceparent"));

        Span test = GlobalOpenTelemetry.getTracer("test")
            .spanBuilder("test")
            .startSpan();

        try (Scope scope = test.makeCurrent()) {
            processor = new EventProcessorClientBuilder()
                .connectionString(TestUtils.getConnectionString())
                .eventHubName(getEventHubName())
                .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
                .consumerGroup("$Default")
                .checkpointStore(new SampleCheckpointStore())
                .processEvent(ec -> {
                    if (!ec.getEventData().getProperties().containsKey("traceparent")) {
                        received.add(ec.getEventData());
                    }
                    ec.updateCheckpoint();
                })
                .processError(e -> fail("unexpected error", e.getThrowable()))
                .buildEventProcessorClient();

            toClose((Closeable) () -> processor.stop());
            processor.start();

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            processor.stop();
        }

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> processed = findSpans(spans, "EventHubs.process").stream()
            .filter(s -> !s.getParentSpanContext().isValid())
            .collect(toList());
        assertTrue(processed.size() >= 2);
        assertConsumerSpan(processed.get(0), received.get(0), "EventHubs.process");

        for (int i = 1; i < processed.size(); i++) {
            assertConsumerSpan(processed.get(i), received.get(i), "EventHubs.process");
            assertNotEquals(processed.get(0).getSpanContext().getTraceId(), processed.get(i).getSpanContext().getTraceId());
        }
    }


    @Test
    public void sendAndProcessBatch() throws InterruptedException {
        EventData message1 = new EventData(CONTENTS_BYTES);
        EventData message2 = new EventData(CONTENTS_BYTES);
        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<List<EventData>> received = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get());
        StepVerifier.create(producer.send(Arrays.asList(message1, message2), new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        processor = new EventProcessorClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .eventHubName(getEventHubName())
            .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEventBatch(eb -> {
                if (currentInProcess.compareAndSet(null, Span.current())) {
                    received.compareAndSet(null, eb.getEvents());

                    logger.atInfo()
                        .addKeyValue("receivedCount", eb.getEvents().size())
                        .addKeyValue("currentInProcessSpan", currentInProcess.get())
                        .log("processing events");

                }
                eb.updateCheckpoint();
            }, 2)
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();
        toClose((Closeable) () -> processor.stop());
        processor.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> messages = findSpans(spans, "EventHubs.message");
        assertMessageSpan(messages.get(0), message1);
        assertMessageSpan(messages.get(1), message2);
        List<ReadableSpan> send = findSpans(spans, "EventHubs.send");

        assertSendSpan(send.get(0), Arrays.asList(message1, message2), "EventHubs.send");

        List<ReadableSpan> processed = findSpans(spans, "EventHubs.process")
            .stream().filter(p -> p == currentInProcess.get()).collect(toList());
        assertEquals(1, processed.size());

        assertConsumerSpan(processed.get(0), received.get(), "EventHubs.process", StatusCode.UNSET);
    }

    @Test
    public void sendProcessAndFail() throws InterruptedException {
        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<List<EventData>> received = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get() || span.getName().equals("EventHubs.send"));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        processor = new EventProcessorClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .eventHubName(getEventHubName())
            .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEventBatch(eb -> {
                if (currentInProcess.compareAndSet(null, Span.current())) {
                    received.compareAndSet(null, eb.getEvents());
                }
                eb.updateCheckpoint();
                throw new RuntimeException("foo");
            }, 1)
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();

        toClose((Closeable) () -> processor.stop());
        processor.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> processed = findSpans(spans, "EventHubs.process")
            .stream().filter(p -> p == currentInProcess.get())
            .collect(toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), received.get(), "EventHubs.process", StatusCode.ERROR);
    }

    private void assertMessageSpan(ReadableSpan actual, EventData message) {
        assertEquals("EventHubs.message", actual.getName());
        assertEquals(SpanKind.PRODUCER, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        assertNull(actual.getAttribute(AttributeKey.stringKey("messaging.operation")));
        String traceparent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getSpanContext().getSpanId() + "-01";
        assertEquals(message.getProperties().get("Diagnostic-Id"), traceparent);
        assertEquals(message.getProperties().get("traceparent"), traceparent);
    }

    private void assertSendSpan(ReadableSpan actual, List<EventData> messages, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        assertEquals("publish", actual.getAttribute(AttributeKey.stringKey("messaging.operation")));
        if (messages.size() > 1) {
            assertEquals(messages.size(), actual.getAttribute(AttributeKey.longKey("messaging.batch.message_count")));
        }
        List<LinkData> links = actual.toSpanData().getLinks();
        assertEquals(messages.size(), links.size());
        for (int i = 0; i < links.size(); i++) {
            String messageTraceparent = (String) messages.get(i).getProperties().get("traceparent");
            SpanContext linkContext = links.get(i).getSpanContext();
            String linkTraceparent = "00-" + linkContext.getTraceId() + "-" + linkContext.getSpanId() + "-01";
            assertEquals(messageTraceparent, linkTraceparent);
        }
    }

    private void assertSyncConsumerSpan(ReadableSpan actual, List<PartitionEvent> messages, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        List<LinkData> links = actual.toSpanData().getLinks();
        assertEquals("receive", actual.getAttribute(AttributeKey.stringKey("messaging.operation")));
        if (messages.size() > 1) {
            assertEquals(messages.size(), actual.getAttribute(AttributeKey.longKey("messaging.batch.message_count")));
        }
        assertEquals(messages.size(), links.size());
        for (int i = 0; i < links.size(); i++) {
            String messageTraceparent = (String) messages.get(i).getData().getProperties().get("traceparent");
            SpanContext linkContext = links.get(i).getSpanContext();
            String linkTraceparent = "00-" + linkContext.getTraceId() + "-" + linkContext.getSpanId() + "-01";
            assertEquals(messageTraceparent, linkTraceparent);
            assertNotNull(links.get(i).getAttributes().get(AttributeKey.longKey("messaging.eventhubs.message.enqueued_time")));
        }
    }

    private void assertConsumerSpan(ReadableSpan actual, EventData message, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        assertEquals(0, actual.toSpanData().getLinks().size());
        assertEquals("process", actual.getAttribute(AttributeKey.stringKey("messaging.operation")));

        String messageTraceparent = (String) message.getProperties().get("traceparent");
        if (messageTraceparent == null) {
            assertFalse(actual.getParentSpanContext().isValid());
        } else {
            String parent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getParentSpanContext().getSpanId() + "-01";
            assertEquals(messageTraceparent, parent);
        }
    }

    private void assertConsumerSpan(ReadableSpan actual, List<EventData> messages, String spanName, StatusCode status) {
        logger.atInfo()
            .addKeyValue("linkCount", actual.toSpanData().getLinks().size())
            .addKeyValue("receivedCount", messages.size())
            .addKeyValue("batchSize", actual.getAttribute(AttributeKey.longKey("messaging.batch.message_count")))
            .log("assertConsumerSpan");

        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(status, actual.toSpanData().getStatus().getStatusCode());
        assertEquals("process", actual.getAttribute(AttributeKey.stringKey("messaging.operation")));

        List<EventData> receivedMessagesWithTraceContext = messages.stream().filter(m -> m.getProperties().containsKey("traceparent")).collect(toList());
        assertEquals(receivedMessagesWithTraceContext.size(), actual.toSpanData().getLinks().size());
        if (messages.size() > 1) {
            assertEquals(messages.size(), actual.getAttribute(AttributeKey.longKey("messaging.batch.message_count")));
        }

        List<LinkData> links =  actual.toSpanData().getLinks();
        for (EventData data : receivedMessagesWithTraceContext) {
            String messageTraceparent = (String) data.getProperties().get("traceparent");
            List<LinkData> link = links.stream().filter(l -> {
                String linkedContext = "00-" + l.getSpanContext().getTraceId() + "-" + l.getSpanContext().getSpanId() + "-01";
                return linkedContext.equals(messageTraceparent);
            }).collect(toList());
            assertEquals(1, link.size());
            assertNotNull(link.get(0).getAttributes().get(AttributeKey.longKey("messaging.eventhubs.message.enqueued_time")));
        }
    }

    private List<ReadableSpan> findSpans(List<ReadableSpan> spans, String spanName) {
        return spans.stream()
            .filter(s -> s.getName().equals(spanName))
            .collect(toList());
    }

    static class TestSpanProcessor implements SpanProcessor {
        private static final ClientLogger LOGGER = new ClientLogger(TestSpanProcessor.class);
        private final ConcurrentLinkedDeque<ReadableSpan> spans = new ConcurrentLinkedDeque<>();
        private final String entityName;
        private final String namespace;
        private final String testName;

        private final AtomicReference<Consumer<ReadableSpan>> notifier = new AtomicReference<>();

        TestSpanProcessor(String namespace, String entityName, String testName) {
            this.namespace = namespace;
            this.entityName = entityName;
            this.testName = testName;
        }
        public List<ReadableSpan> getEndedSpans() {
            return new ArrayList<>(spans);
        }

        @Override
        public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        }

        @Override
        public boolean isStartRequired() {
            return false;
        }

        @Override
        public void onEnd(ReadableSpan readableSpan) {
            SpanData span = readableSpan.toSpanData();

            InstrumentationScopeInfo instrumentationScopeInfo = span.getInstrumentationScopeInfo();
            LoggingEventBuilder log = LOGGER.atInfo()
                .addKeyValue("testName", testName)
                .addKeyValue("name", span.getName())
                .addKeyValue("traceId", span.getTraceId())
                .addKeyValue("spanId", span.getSpanId())
                .addKeyValue("parentSpanId", span.getParentSpanId())
                .addKeyValue("kind", span.getKind())
                .addKeyValue("tracerName", instrumentationScopeInfo.getName())
                .addKeyValue("tracerVersion", instrumentationScopeInfo.getVersion())
                .addKeyValue("attributes", span.getAttributes());

            for (int i = 0; i < span.getLinks().size(); i++) {
                LinkData link = span.getLinks().get(i);
                log.addKeyValue("linkTraceId" + i, link.getSpanContext().getTraceId())
                    .addKeyValue("linkSpanId" + i, link.getSpanContext().getSpanId())
                    .addKeyValue("linkAttributes" + i, link.getAttributes());
            }
            log.log("got span");

            spans.add(readableSpan);
            Consumer<ReadableSpan> filter = notifier.get();
            if (filter != null) {
                filter.accept(readableSpan);
            }

            // Various attribute keys can be found in:
            // sdk/core/azure-core-metrics-opentelemetry/src/main/java/com/azure/core/metrics/opentelemetry/OpenTelemetryAttributes.java
            // sdk/core/azure-core-tracing-opentelemetry/src/main/java/com/azure/core/tracing/opentelemetry/OpenTelemetryUtils.java
            assertEquals("Microsoft.EventHub", readableSpan.getAttribute(AttributeKey.stringKey("az.namespace")));
            assertEquals("eventhubs", readableSpan.getAttribute(AttributeKey.stringKey("messaging.system")));
            assertEquals(entityName, readableSpan.getAttribute(AttributeKey.stringKey("messaging.destination.name")));
            assertEquals(namespace, readableSpan.getAttribute(AttributeKey.stringKey("net.peer.name")));
        }

        public void notifyIfCondition(CountDownLatch countDownLatch, Predicate<ReadableSpan> filter) {
            notifier.set((span) -> {
                if (filter.test(span)) {
                    LOGGER.atInfo()
                        .addKeyValue("traceId", span.getSpanContext().getTraceId())
                        .addKeyValue("spanId", span.getSpanContext().getSpanId())
                        .log("condition met");
                    countDownLatch.countDown();
                }
            });
        }

        @Override
        public boolean isEndRequired() {
            return true;
        }

        @Override
        public CompletableResultCode shutdown() {
            notifier.set(null);
            return CompletableResultCode.ofSuccess();
        }
    }
}
