// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
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
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.LinkData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

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

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class TracingIntegrationTests extends IntegrationTestBase {
    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private static final String PARTITION_ID = "0";
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
        spanProcessor = new TestSpanProcessor(getFullyQualifiedDomainName(), getEventHubName());
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
        ClientOptions options = new ClientOptions();
        if (otel != null) {
            options.setTracingOptions(new OpenTelemetryTracingOptions().setProvider(otel.getTracerProvider()));
        }

        producer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(options)
            .buildAsyncProducerClient();

        consumer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(options)
            .consumerGroup("$Default")
            .buildAsyncConsumerClient();

        consumerSync = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(options)
            .consumerGroup("$Default")
            .buildConsumerClient();
    }

    @Override
    protected void afterTest() {
        GlobalOpenTelemetry.resetForTest();
        if (processor != null) {
            processor.stop();
        }
        try {
            dispose(consumer, producer, consumerSync);
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        }
    }

    @Test
    public void sendAndReceiveFromPartition() throws InterruptedException {
        AtomicReference<EventData> receivedMessage = new AtomicReference<>();
        AtomicReference<Span> receivedSpan = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || span.getName().equals("EventHubs.send"));
        consumer
            .receiveFromPartition(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime))
            .take(1)
            .subscribe(pe -> {
                receivedMessage.set(pe.getData());
                receivedSpan.set(Span.current());
            });

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID))).verifyComplete();

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
        consumer
            .receive()
            .take(1)
            .subscribe(pe -> {
                receivedMessage.set(pe.getData());
                receivedSpan.set(Span.current());
            });


        StepVerifier.create(producer.send(data, new SendOptions())).verifyComplete();

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

        TestSpanProcessor customSpanProcessor = new TestSpanProcessor(getFullyQualifiedDomainName(), getEventHubName());
        OpenTelemetrySdk otel = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder()
                    .addSpanProcessor(customSpanProcessor)
                    .build())
            .build();

        createClients(otel);

        CountDownLatch latch = new CountDownLatch(2);
        customSpanProcessor.notifyIfCondition(latch, span -> span == receivedSpan.get() || span.getName().equals("EventHubs.send"));

        consumer.receive()
            .take(1)
            .subscribe(pe -> {
                receivedMessage.set(pe.getData());
                receivedSpan.set(Span.current());
            });

        StepVerifier.create(producer.send(data, new SendOptions())).verifyComplete();

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
                    String traceId = Span.current().getSpanContext().getTraceId();

                    // context created for the message and current are the same
                    assertTrue(traceparent.startsWith("00-" + traceId));
                    assertFalse(((ReadableSpan) Span.current()).hasEnded());
                })
                .parallel(messageCount, 1)
                .runOn(Schedulers.boundedElastic(), 2))
            .expectNextCount(messageCount)
            .verifyComplete();

        StepVerifier.create(producer.send(data, new SendOptions())).verifyComplete();

        assertTrue(latch.await(20, TimeUnit.SECONDS));
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> received = findSpans(spans, "EventHubs.consume");
        assertTrue(messageCount <= received.size());
    }

    @Test
    public void sendBuffered() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        EventHubBufferedProducerAsyncClient bufferedProducer =  new EventHubBufferedProducerClientBuilder()
            .connectionString(getConnectionString())
            .onSendBatchFailed(failed -> fail("Exception occurred while sending messages." + failed.getThrowable()))
            .onSendBatchSucceeded(succeeded -> latch.countDown())
            .maxEventBufferLengthPerPartition(5)
            .maxWaitTime(Duration.ofSeconds(5))
            .buildAsyncClient();

        EventData event1 = new EventData("1");
        EventData event2 = new EventData("2");

        StepVerifier.create(
                bufferedProducer
                    .getPartitionIds().take(1)
                    .map(partitionId -> new SendOptions().setPartitionId(partitionId))
                        .flatMap(sendOpts ->
                            bufferedProducer.enqueueEvent(event1, sendOpts)
                            .then(bufferedProducer.enqueueEvent(event2, sendOpts))))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(consumer
            .receive()
            .take(2))
            .expectNextCount(2)
            .verifyComplete();

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
            .verifyComplete();

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
            .verifyComplete();

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
        AtomicReference<Span> currentInProcess = new AtomicReference<>(Span.getInvalid());
        AtomicReference<EventData> receivedMessage = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get() || span.getName().equals("EventHubs.send"));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID))).verifyComplete();
        processor = new EventProcessorClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEvent(ec -> {
                currentInProcess.set(Span.current());
                receivedMessage.set(ec.getEventData());
                ec.updateCheckpoint();
            })
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();

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
    public void sendNotInstrumentedAndProcess() throws InterruptedException {
        EventHubProducerAsyncClient notInstrumentedProducer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .clientOptions(new ClientOptions().setTracingOptions(new TracingOptions().setEnabled(false)))
            .buildAsyncProducerClient();

        EventData message1 = new EventData(CONTENTS_BYTES);
        EventData message2 = new EventData(CONTENTS_BYTES);
        List<EventData> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span.getName().equals("EventHubs.process"));
        StepVerifier.create(notInstrumentedProducer.send(Arrays.asList(message1, message2), new SendOptions().setPartitionId(PARTITION_ID))).verifyComplete();

        assertNull(message1.getProperties().get("traceparent"));
        assertNull(message2.getProperties().get("traceparent"));
        processor = new EventProcessorClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEvent(ec -> {
                received.add(ec.getEventData());
                ec.updateCheckpoint();
            })
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();

        processor.start();

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> processed = findSpans(spans, "EventHubs.process");
        assertTrue(processed.size() >= 2);
        assertConsumerSpan(processed.get(0), received.get(0), "EventHubs.process");

        for (int i = 1; i < processed.size(); i ++) {
            assertConsumerSpan(processed.get(i), received.get(i), "EventHubs.process");
            assertNotEquals(processed.get(0).getSpanContext().getTraceId(), processed.get(i).getSpanContext().getTraceId());
        }
    }


    @Test
    public void sendAndProcessBatch() throws InterruptedException {
        EventData message1 = new EventData(CONTENTS_BYTES);
        EventData message2 = new EventData(CONTENTS_BYTES);
        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        List<EventData> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get());
        StepVerifier.create(producer.send(Arrays.asList(message1, message2), new SendOptions().setPartitionId(PARTITION_ID))).verifyComplete();

        processor = new EventProcessorClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEventBatch(eb -> {
                received.clear();
                eb.getEvents().forEach(e -> {
                    currentInProcess.set(Span.current());
                    received.add(e);
                    eb.updateCheckpoint();
                });
            }, 2)
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();

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
        assertConsumerSpan(processed.get(0), received, "EventHubs.process", StatusCode.UNSET);
    }

    @Test
    public void sendProcessAndFail() throws InterruptedException {
        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        List<EventData> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get() || span.getName().equals("EventHubs.send"));

        StepVerifier.create(producer.send(data, new SendOptions().setPartitionId(PARTITION_ID))).verifyComplete();

        processor = new EventProcessorClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .initialPartitionEventPosition(Collections.singletonMap(PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime)))
            .consumerGroup("$Default")
            .checkpointStore(new SampleCheckpointStore())
            .processEventBatch(eb -> {
                received.clear();
                eb.getEvents().forEach(e -> {
                    currentInProcess.set(Span.current());
                    received.add(e);
                    eb.updateCheckpoint();
                    throw new RuntimeException("foo");
                });
            }, 1)
            .processError(e -> fail("unexpected error", e.getThrowable()))
            .buildEventProcessorClient();

        processor.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> processed = findSpans(spans, "EventHubs.process")
            .stream().filter(p -> p == currentInProcess.get())
            .collect(toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), received, "EventHubs.process", StatusCode.ERROR);
    }

    private void assertMessageSpan(ReadableSpan actual, EventData message) {
        assertEquals("EventHubs.message", actual.getName());
        assertEquals(SpanKind.PRODUCER, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
        String traceparent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getSpanContext().getSpanId() + "-01";
        assertEquals(message.getProperties().get("Diagnostic-Id"), traceparent);
        assertEquals(message.getProperties().get("traceparent"), traceparent);
    }

    private void assertSendSpan(ReadableSpan actual, List<EventData> messages, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
        assertEquals(StatusCode.UNSET, actual.toSpanData().getStatus().getStatusCode());
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

        String messageTraceparent = (String) message.getProperties().get("traceparent");
        if (messageTraceparent == null) {
            assertFalse(actual.getParentSpanContext().isValid());
        } else {
            String parent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getParentSpanContext().getSpanId() + "-01";
            assertEquals(messageTraceparent, parent);
        }
    }

    private void assertConsumerSpan(ReadableSpan actual, List<EventData> messages, String spanName, StatusCode status) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(status, actual.toSpanData().getStatus().getStatusCode());
        assertEquals(messages.stream().filter(m -> m.getProperties().containsKey("traceparent")).count(), actual.toSpanData().getLinks().size());
        List<LinkData> links =  actual.toSpanData().getLinks();
        for (EventData data : messages) {
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
        private final ConcurrentLinkedDeque<ReadableSpan> spans = new ConcurrentLinkedDeque<>();
        private final String entityName;
        private final String namespace;

        private final AtomicReference<Consumer<ReadableSpan>> notifier = new AtomicReference<>();

        TestSpanProcessor(String namespace, String entityName) {
            this.namespace = namespace;
            this.entityName = entityName;
        }
        public List<ReadableSpan> getEndedSpans() {
            return spans.stream().collect(toList());
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
            assertEquals("Microsoft.EventHub", readableSpan.getAttribute(AttributeKey.stringKey("az.namespace")));
            assertEquals(entityName, readableSpan.getAttribute(AttributeKey.stringKey("messaging.destination.name")));
            assertEquals(namespace, readableSpan.getAttribute(AttributeKey.stringKey("net.peer.name")));
            assertEquals("eventhubs", readableSpan.getAttribute(AttributeKey.stringKey("messaging.system")));

            Consumer<ReadableSpan> filter = notifier.get();
            if (filter != null) {
                filter.accept(readableSpan);
            }
            spans.add(readableSpan);
        }

        public void notifyIfCondition(CountDownLatch countDownLatch, Predicate<ReadableSpan> filter) {
            notifier.set((span) -> {
                if (filter.test(span)) {
                    countDownLatch.countDown();
                }
            });
        }

        @Override
        public boolean isEndRequired() {
            return true;
        }
    }
}
