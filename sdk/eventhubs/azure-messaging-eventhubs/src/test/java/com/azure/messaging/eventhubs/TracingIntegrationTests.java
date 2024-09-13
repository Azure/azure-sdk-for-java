// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.instrumentation.OperationName;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventContext;
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
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.TestUtils.getEventHubName;
import static com.azure.messaging.eventhubs.TestUtils.getFullyQualifiedDomainName;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.EVENT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.CHECKPOINT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.PROCESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.RECEIVE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.SEND;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class TracingIntegrationTests extends IntegrationTestBase {
    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private static final String PARTITION_ID = "0";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final AtomicReference<TokenCredential> cachedCredential = new AtomicReference<>();
    private static final AttributeKey<String> OPERATION_NAME_ATTRIBUTE = AttributeKey.stringKey("messaging.operation.name");
    private static final AttributeKey<String> OPERATION_TYPE_ATTRIBUTE = AttributeKey.stringKey("messaging.operation.type");
    private TestSpanProcessor spanProcessor;
    private Instant testStartTime;
    private EventData data;
    private OpenTelemetrySdk otel;

    public TracingIntegrationTests() {
        super(new ClientLogger(TracingIntegrationTests.class));
    }

    @Override
    protected void beforeTest() {
        GlobalOpenTelemetry.resetForTest();
        testStartTime = Instant.now().minusSeconds(1);
        data = new EventData(CONTENTS_BYTES);
        spanProcessor = toClose(new TestSpanProcessor(getFullyQualifiedDomainName(), getEventHubName(), testName));
        otel = toClose(OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(spanProcessor)
                    .build())
            .build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    @NullSource
    public void sendAndReceiveFromPartition(String v2) throws InterruptedException {
        EventHubProducerAsyncClient producer = createProducer(v2);
        EventHubConsumerAsyncClient consumer = createConsumer(v2);

        AtomicReference<PartitionEvent> receivedMessage = new AtomicReference<>();
        AtomicReference<Span> receivedSpan = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || hasOperationName(span, SEND));
        toClose(consumer
            .receiveFromPartition(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime))
            .take(1)
            .subscribe(pe -> {
                if (receivedMessage.compareAndSet(null, pe)) {
                    receivedSpan.compareAndSet(null, Span.current());
                }
            }));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(latch.await(30, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, EVENT);
        assertMessageSpan(message.get(0), data);
        List<ReadableSpan> send = findSpans(spans, SEND);
        assertSendSpan(send.get(0), Collections.singletonList(data));

        List<ReadableSpan> received = findSpans(spans, PROCESS).stream()
            .filter(s -> s == receivedSpan.get()).collect(toList());
        assertConsumerSpan(received.get(0), receivedMessage.get().getData(), receivedMessage.get().getPartitionContext().getPartitionId());
        assertNull(received.get(0).getAttribute(AttributeKey.stringKey("messaging.consumer.group.name")));
    }

    @Test
    public void sendAndReceive() throws InterruptedException {
        EventHubProducerAsyncClient producer = createProducer(null);
        EventHubConsumerAsyncClient consumer = createConsumer(null);

        AtomicReference<PartitionEvent> receivedMessage = new AtomicReference<>();
        AtomicReference<Span> receivedSpan = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || hasOperationName(span, SEND));
        toClose(consumer
            .receive()
            .take(DEFAULT_TIMEOUT)
            .subscribe(pe -> {
                if (receivedMessage.compareAndSet(null, pe)) {
                    receivedSpan.compareAndSet(null, Span.current());
                }
            }));

        StepVerifier.create(producer.send(data, new SendOptions()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(latch.await(30, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, EVENT);
        assertMessageSpan(message.get(0), data);
        List<ReadableSpan> send = findSpans(spans, SEND);
        assertSendSpan(send.get(0), Collections.singletonList(data));

        List<ReadableSpan> received = findSpans(spans, PROCESS).stream()
            .filter(s -> s == receivedSpan.get()).collect(toList());
        assertConsumerSpan(received.get(0), receivedMessage.get().getData(), receivedMessage.get().getPartitionContext().getPartitionId());
    }

    @Test
    public void sendAndReceiveCustomProvider() throws InterruptedException {

        AtomicReference<PartitionEvent> receivedMessage = new AtomicReference<>();
        AtomicReference<Span> receivedSpan = new AtomicReference<>();

        TestSpanProcessor customSpanProcessor = toClose(new TestSpanProcessor(getFullyQualifiedDomainName(), getEventHubName(), "sendAndReceiveCustomProvider"));
        otel = toClose(OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder()
                    .addSpanProcessor(customSpanProcessor)
                    .build())
            .build());
        EventHubProducerAsyncClient producer = createProducer(null);
        EventHubConsumerAsyncClient consumer = createConsumer(null);

        CountDownLatch latch = new CountDownLatch(2);
        customSpanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || hasOperationName(span, SEND));

        toClose(consumer.receive()
            .take(1)
            .subscribe(pe -> {
                if (receivedMessage.compareAndSet(null, pe)) {
                    receivedSpan.compareAndSet(null, Span.current());
                }
            }));

        StepVerifier.create(producer.send(data, new SendOptions()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(latch.await(30, TimeUnit.SECONDS));

        List<ReadableSpan> spans = customSpanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, EVENT);
        assertMessageSpan(message.get(0), data);
        List<ReadableSpan> send = findSpans(spans, SEND);
        assertSendSpan(send.get(0), Collections.singletonList(data));

        List<ReadableSpan> received = findSpans(spans, PROCESS).stream()
            .filter(s -> s == receivedSpan.get()).collect(toList());
        assertConsumerSpan(received.get(0), receivedMessage.get().getData(), receivedMessage.get().getPartitionContext().getPartitionId());
    }

    @Test
    public void sendAndReceiveParallel() throws InterruptedException {
        EventHubProducerAsyncClient producer = createProducer(null);
        EventHubConsumerAsyncClient consumer = createConsumer(null);

        int messageCount = 5;
        CountDownLatch latch = new CountDownLatch(messageCount);

        spanProcessor.notifyIfCondition(latch, span -> hasOperationName(span, PROCESS));

        StepVerifier.create(producer.send(data, new SendOptions()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

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

        assertTrue(latch.await(20, TimeUnit.SECONDS));
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> received = findSpans(spans, PROCESS);
        assertTrue(messageCount <= received.size());
    }

    @Test
    public void sendBuffered() throws InterruptedException {
        EventHubConsumerAsyncClient consumer = createConsumer(null);

        CountDownLatch latch = new CountDownLatch(3);
        spanProcessor.notifyIfCondition(latch, span -> hasOperationName(span, PROCESS) || hasOperationName(span, SEND));

        EventHubBufferedProducerAsyncClient bufferedProducer = toClose(new EventHubBufferedProducerClientBuilder()
            .clientOptions(getClientOptions())
            .credential(TestUtils.getPipelineCredential(cachedCredential))
            .eventHubName(getEventHubName())
            .fullyQualifiedNamespace(getFullyQualifiedDomainName())
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
        SendOptions sendOptions = new SendOptions().setPartitionId("0");
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

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, EVENT);
        assertMessageSpan(message.get(0), event1);
        assertMessageSpan(message.get(1), event2);

        List<ReadableSpan> send = findSpans(spans, SEND);
        assertSendSpan(send.get(0), Arrays.asList(event1, event2));
        assertEquals(sendOptions.getPartitionId(), send.get(0).getAttribute(AttributeKey.stringKey("messaging.destination.partition.id")));

        List<ReadableSpan> received = findSpans(spans, PROCESS);
        assertEquals(2, received.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void syncReceive(String v2) {
        EventHubProducerAsyncClient producer = createProducer(v2);
        EventHubConsumerClient consumerSync = createSyncConsumer(v2);

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
        assertEquals(0, findSpans(spans, PROCESS).size());

        List<ReadableSpan> received = findSpans(spans, RECEIVE);
        assertSyncReceiveSpan(received.get(0), receivedMessages);
    }

    @Test
    public void syncReceiveWithOptions() {
        EventHubProducerAsyncClient producer = createProducer(null);
        EventHubConsumerClient consumerSync = createSyncConsumer(null);

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
        assertEquals(0, findSpans(spans, PROCESS).size());

        List<ReadableSpan> received = findSpans(spans, RECEIVE);
        assertSyncReceiveSpan(received.get(0), receivedMessages);
    }

    @Test
    public void syncReceiveTimeout() {
        EventHubProducerAsyncClient producer = createProducer(null);
        EventHubConsumerClient consumerSync = createSyncConsumer(null);
        List<PartitionEvent> receivedMessages = consumerSync.receiveFromPartition(PARTITION_ID, 2,
                EventPosition.fromEnqueuedTime(testStartTime), Duration.ofSeconds(1))
            .stream().collect(toList());

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertEquals(0, findSpans(spans, PROCESS).size());

        List<ReadableSpan> received = findSpans(spans, RECEIVE);
        assertSyncReceiveSpan(received.get(0), receivedMessages);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void sendAndProcess(String v2) throws InterruptedException {
        EventHubProducerAsyncClient producer = createProducer(v2);

        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<EventContext> receivedMessage = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get() || hasOperationName(span, SEND));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        EventHubClientBuilder builder = createBuilder();
        EventProcessorClient processor = new EventProcessorClientBuilder()
            .credential(builder.getFullyQualifiedNamespace(), builder.getEventHubName(), builder.getCredentials())
            .clientOptions(getClientOptions())
            .configuration(getConfiguration(v2))
            .initialPartitionEventPosition(p -> EventPosition.fromEnqueuedTime(testStartTime))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEvent(ec -> {
                if (currentInProcess.compareAndSet(null, Span.current())) {
                    receivedMessage.compareAndSet(null, ec);
                }
                ec.updateCheckpoint();
            })
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();

        toClose((Closeable) () -> processor.stop());
        processor.start();
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        processor.stop();

        assertTrue(currentInProcess.get().getSpanContext().isValid());
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> message = findSpans(spans, EVENT);
        assertMessageSpan(message.get(0), data);

        List<ReadableSpan> send = findSpans(spans, SEND);
        assertSendSpan(send.get(0), Collections.singletonList(data));

        List<ReadableSpan> processed = findSpans(spans, PROCESS)
            .stream().filter(p -> p == currentInProcess.get()).collect(toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), receivedMessage.get().getEventData(), receivedMessage.get().getPartitionContext().getPartitionId());
        assertNull(processed.get(0).getAttribute(AttributeKey.stringKey("messaging.consumer.group.name")));

        SpanContext parentSpanContext = currentInProcess.get().getSpanContext();
        List<ReadableSpan> checkpointed = findSpans(spans, CHECKPOINT)
                .stream().filter(c -> c.getParentSpanContext().getSpanId()
                        .equals(parentSpanContext.getSpanId())).collect(toList());
        assertEquals(1, checkpointed.size());
        assertCheckpointSpan(checkpointed.get(0), parentSpanContext);
    }

    @Test
    @SuppressWarnings("try")
    public void sendNotInstrumentedAndProcess() throws InterruptedException {
        EventHubProducerAsyncClient notInstrumentedProducer = toClose(createBuilder()
            .clientOptions(new ClientOptions().setTracingOptions(new TracingOptions().setEnabled(false)))
            .buildAsyncProducerClient());

        EventData message1 = new EventData(CONTENTS_BYTES);
        EventData message2 = new EventData(CONTENTS_BYTES);
        List<EventContext> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> hasOperationName(span, PROCESS) && !span.getParentSpanContext().isValid());
        StepVerifier.create(notInstrumentedProducer.send(Arrays.asList(message1, message2), new SendOptions().setPartitionId(PARTITION_ID)))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

        assertNull(message1.getProperties().get("traceparent"));
        assertNull(message2.getProperties().get("traceparent"));

        Span test = GlobalOpenTelemetry.getTracer("test")
                .spanBuilder("test")
                .startSpan();

        EventHubClientBuilder builder = createBuilder();

        try (Scope scope = test.makeCurrent()) {
            EventProcessorClient processor = new EventProcessorClientBuilder()
                .credential(builder.getFullyQualifiedNamespace(), builder.getEventHubName(), builder.getCredentials())
                .clientOptions(getClientOptions())
                .initialPartitionEventPosition(p -> EventPosition.fromEnqueuedTime(testStartTime))
                .consumerGroup("$Default")
                .checkpointStore(new SampleCheckpointStore())
                .processEvent(ec -> {
                    if (!ec.getEventData().getProperties().containsKey("traceparent")) {
                        received.add(ec);
                    }
                    ec.updateCheckpoint();
                })
                .processError(e -> fail("unexpected error", e.getThrowable()))
                .buildEventProcessorClient();

            toClose((Closeable) () -> processor.stop());
            processor.start();

            assertTrue(latch.await(30, TimeUnit.SECONDS));
            processor.stop();
        }

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> processed = findSpans(spans, PROCESS).stream()
                .filter(s -> !s.getParentSpanContext().isValid())
                .collect(toList());
        assertTrue(processed.size() >= 2);
        assertConsumerSpan(processed.get(0), received.get(0).getEventData(), received.get(0).getPartitionContext().getPartitionId());

        List<ReadableSpan> checkpointed = findSpans(spans, CHECKPOINT).stream().collect(toList());
        for (int i = 1; i < processed.size(); i++) {
            assertConsumerSpan(processed.get(i), received.get(i).getEventData(), received.get(i).getPartitionContext().getPartitionId());
            SpanContext parentSpanContext = processed.get(i).getSpanContext();
            assertNotEquals(processed.get(0).getSpanContext().getTraceId(), parentSpanContext.getTraceId());
            List<ReadableSpan> checkpointedChildren = checkpointed.stream()
                    .filter(c -> c.getParentSpanContext().getSpanId().equals(parentSpanContext.getSpanId()))
                    .collect(toList());
            assertEquals(1, checkpointedChildren.size());
            assertCheckpointSpan(checkpointedChildren.get(0), parentSpanContext);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void sendAndProcessBatch(String v2) throws InterruptedException {
        EventHubProducerAsyncClient producer = createProducer(v2);

        EventData message1 = new EventData(CONTENTS_BYTES);
        EventData message2 = new EventData(CONTENTS_BYTES);
        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<List<EventData>> received = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get());
        StepVerifier.create(producer.send(Arrays.asList(message1, message2), new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        EventHubClientBuilder builder = createBuilder();

        EventProcessorClient processor = new EventProcessorClientBuilder()
            .credential(builder.getFullyQualifiedNamespace(), builder.getEventHubName(), builder.getCredentials())
            .clientOptions(getClientOptions())
            .configuration(getConfiguration(v2))
            .initialPartitionEventPosition(p -> EventPosition.fromEnqueuedTime(testStartTime))
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
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> messages = findSpans(spans, EVENT);
        assertMessageSpan(messages.get(0), message1);
        assertMessageSpan(messages.get(1), message2);
        List<ReadableSpan> send = findSpans(spans, SEND);

        assertSendSpan(send.get(0), Arrays.asList(message1, message2));

        List<ReadableSpan> processed = findSpans(spans, PROCESS)
            .stream().filter(p -> p == currentInProcess.get()).collect(toList());
        assertEquals(1, processed.size());

        assertConsumerSpan(processed.get(0), received.get(), StatusCode.UNSET);

        SpanContext parentSpanContext = currentInProcess.get().getSpanContext();
        List<ReadableSpan> checkpointed = findSpans(spans, CHECKPOINT)
                .stream().filter(c -> c.getParentSpanContext().getSpanId()
                        .equals(parentSpanContext.getSpanId())).collect(toList());
        assertEquals(1, checkpointed.size());
        assertCheckpointSpan(checkpointed.get(0), parentSpanContext);
    }

    @Test
    public void sendProcessAndFail() throws InterruptedException {
        EventHubProducerAsyncClient producer = createProducer(null);

        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<List<EventData>> received = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get() || hasOperationName(span, SEND));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        EventHubClientBuilder builder = createBuilder();

        EventProcessorClient processor = new EventProcessorClientBuilder()
            .credential(builder.getFullyQualifiedNamespace(), builder.getEventHubName(), builder.getCredentials())
            .initialPartitionEventPosition(p -> EventPosition.fromEnqueuedTime(testStartTime))
            .clientOptions(getClientOptions())
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
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> processed = findSpans(spans, PROCESS)
            .stream().filter(p -> p == currentInProcess.get())
            .collect(toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), received.get(), StatusCode.ERROR);
        assertEquals(RuntimeException.class.getName(), processed.get(0).getAttribute(AttributeKey.stringKey("error.type")));

        SpanContext parentSpanContext = currentInProcess.get().getSpanContext();
        List<ReadableSpan> checkpointed = findSpans(spans, CHECKPOINT)
                .stream().filter(c -> c.getParentSpanContext().getSpanId()
                        .equals(parentSpanContext.getSpanId())).collect(toList());
        assertEquals(1, checkpointed.size());
        assertCheckpointSpan(checkpointed.get(0), parentSpanContext);
    }

    private void assertMessageSpan(ReadableSpan actual, EventData message) {
        assertEquals(SpanKind.PRODUCER, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        assertEquals("event", actual.getAttribute(OPERATION_NAME_ATTRIBUTE));
        assertNull(actual.getAttribute(OPERATION_TYPE_ATTRIBUTE));
        String traceparent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getSpanContext().getSpanId() + "-01";
        assertEquals(message.getProperties().get("Diagnostic-Id"), traceparent);
        assertEquals(message.getProperties().get("traceparent"), traceparent);
    }

    private void assertSendSpan(ReadableSpan actual, List<EventData> messages) {
        assertEquals(SpanKind.CLIENT, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        assertEquals("send", actual.getAttribute(OPERATION_NAME_ATTRIBUTE));
        assertEquals("publish", actual.getAttribute(OPERATION_TYPE_ATTRIBUTE));
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

    private void assertSyncReceiveSpan(ReadableSpan actual, List<PartitionEvent> messages) {
        assertEquals(SpanKind.CLIENT, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        List<LinkData> links = actual.toSpanData().getLinks();
        assertEquals("receive", actual.getAttribute(OPERATION_NAME_ATTRIBUTE));
        assertEquals("receive", actual.getAttribute(OPERATION_TYPE_ATTRIBUTE));
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

    private void assertConsumerSpan(ReadableSpan actual, EventData message, String partitionId) {
        SpanData spanData = actual.toSpanData();
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(StatusCode.UNSET, spanData.getStatus().getStatusCode());
        assertNotNull(actual.getAttribute(OPERATION_NAME_ATTRIBUTE));
        assertNotNull(actual.getAttribute(OPERATION_TYPE_ATTRIBUTE));
        assertEquals(partitionId, actual.getAttribute(AttributeKey.stringKey("messaging.destination.partition.id")));

        String messageTraceparent = (String) message.getProperties().get("traceparent");
        if (messageTraceparent == null) {
            assertEquals(0, spanData.getLinks().size());
            assertFalse(actual.getParentSpanContext().isValid());
        } else {
            assertEquals(1, spanData.getLinks().size());
            LinkData link = spanData.getLinks().get(0);
            assertEquals(actual.getSpanContext().getTraceId(), link.getSpanContext().getTraceId());
            assertEquals(actual.getParentSpanContext().getSpanId(), link.getSpanContext().getSpanId());
            String parent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getParentSpanContext().getSpanId() + "-01";
            assertEquals(messageTraceparent, parent);
        }
    }

    private void assertConsumerSpan(ReadableSpan actual, List<EventData> messages, StatusCode status) {
        logger.atInfo()
            .addKeyValue("linkCount", actual.toSpanData().getLinks().size())
            .addKeyValue("receivedCount", messages.size())
            .addKeyValue("batchSize", actual.getAttribute(AttributeKey.longKey("messaging.batch.message_count")))
            .log("assertConsumerSpan");

        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(status, actual.toSpanData().getStatus().getStatusCode());
        assertEquals("process", actual.getAttribute(OPERATION_NAME_ATTRIBUTE));
        assertEquals("process", actual.getAttribute(OPERATION_TYPE_ATTRIBUTE));
        assertNotNull(actual.getAttribute(AttributeKey.stringKey("messaging.destination.partition.id")));

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

    private void assertCheckpointSpan(ReadableSpan actual, SpanContext parent) {
        assertEquals(SpanKind.INTERNAL, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        assertEquals("checkpoint", actual.getAttribute(OPERATION_NAME_ATTRIBUTE));
        assertEquals("settle", actual.getAttribute(OPERATION_TYPE_ATTRIBUTE));
        assertEquals(parent.getTraceId(), actual.getSpanContext().getTraceId());
        assertEquals(parent.getSpanId(), actual.getParentSpanContext().getSpanId());

        assertNull(actual.getAttribute(AttributeKey.stringKey("messaging.consumer.group.name")));
        assertNotNull(actual.getAttribute(AttributeKey.stringKey("messaging.destination.partition.id")));
    }

    private List<ReadableSpan> findSpans(List<ReadableSpan> spans, OperationName operationName) {
        String spanName = TestUtils.getSpanName(operationName, getEventHubName());
        return spans.stream()
            .filter(s -> s.getName().equals(spanName))
            .collect(toList());
    }

    private boolean hasOperationName(ReadableSpan span, OperationName operationName) {
        return operationName.toString().equals(span.getAttribute(OPERATION_NAME_ATTRIBUTE));
    }


    private Configuration getConfiguration(String v2Stack) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();

        if (v2Stack == null) {
            return configBuilder.build();
        }

        return configBuilder
            .putProperty("com.azure.messaging.eventhubs.v2", v2Stack)
            .putProperty("com.azure.core.amqp.cache", v2Stack)
            .build();
    }

    private ClientOptions getClientOptions() {
        return new ClientOptions().setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(otel));
    }

    private EventHubProducerAsyncClient createProducer(String v2) {
        return toClose(createBuilder()
            .clientOptions(getClientOptions())
            .configuration(getConfiguration(v2))
            .buildAsyncProducerClient());
    }

    private EventHubConsumerAsyncClient createConsumer(String v2) {
        return toClose(createBuilder()
            .clientOptions(getClientOptions())
            .configuration(getConfiguration(v2))
            .consumerGroup("$Default")
            .buildAsyncConsumerClient());
    }

    private EventHubConsumerClient createSyncConsumer(String v2) {
        return toClose(createBuilder()
            .clientOptions(getClientOptions())
            .configuration(getConfiguration(v2))
            .consumerGroup("$Default")
            .buildConsumerClient());
    }
}
