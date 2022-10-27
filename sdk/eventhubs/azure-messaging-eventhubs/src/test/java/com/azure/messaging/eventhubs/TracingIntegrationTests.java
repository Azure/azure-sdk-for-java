// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        producer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .buildAsyncProducerClient();

        consumer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .consumerGroup("$Default")
            .buildAsyncConsumerClient();

        consumerSync = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .eventHubName(getEventHubName())
            .consumerGroup("$Default")
            .buildConsumerClient();

        testStartTime = Instant.now().minusSeconds(1);
        data = new EventData(CONTENTS_BYTES);
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
            .filter(s -> s == receivedSpan.get()).collect(Collectors.toList());
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
            .filter(s -> s == receivedSpan.get()).collect(Collectors.toList());
        assertConsumerSpan(received.get(0), receivedMessage.get(), "EventHubs.consume");
    }

    @Test
    public void sendBuffered() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        EventHubBufferedProducerAsyncClient bufferedProducer =  new EventHubBufferedProducerClientBuilder()
            .connectionString(getConnectionString())
            .onSendBatchFailed(failed -> {
                fail("Exception occurred while sending messages." + failed.getThrowable());
            })
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
            .stream().collect(Collectors.toList());

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
            .stream().collect(Collectors.toList());

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
            .stream().collect(Collectors.toList());

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertEquals(0, findSpans(spans, "EventHubs.process").size());

        List<ReadableSpan> received = findSpans(spans, "EventHubs.receiveFromPartition");
        assertSyncConsumerSpan(received.get(0), receivedMessages, "EventHubs.receiveFromPartition");
        assertEquals(StatusCode.OK, received.get(0).toSpanData().getStatus().getStatusCode());
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
            .processError(e -> {
                fail("unexpected error", e.getThrowable());
            })
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
            .stream().filter(p -> p == currentInProcess.get()).collect(Collectors.toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), receivedMessage.get(), "EventHubs.process");
    }

    @Test
    public void sendAndProcessBatch() throws InterruptedException {
        EventData message1 = new EventData(CONTENTS_BYTES);
        EventData message2 = new EventData(CONTENTS_BYTES);
        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        List<EventData> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(latch, span -> span == currentInProcess.get() || span.getName().equals("EventHubs.send"));
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
            .processError(e -> {
                fail("unexpected error", e.getThrowable());
            })
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
            .stream().filter(p -> p == currentInProcess.get()).collect(Collectors.toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), received, "EventHubs.process");
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
            .processError(e -> {
                fail("unexpected error", e.getThrowable());
            })
            .buildEventProcessorClient();

        processor.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> processed = findSpans(spans, "EventHubs.process")
            .stream().filter(p -> p == currentInProcess.get())
            .collect(Collectors.toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), received, "EventHubs.process");
    }

    private void assertMessageSpan(ReadableSpan actual, EventData message) {
        assertEquals("EventHubs.message", actual.getName());
        assertEquals(SpanKind.PRODUCER, actual.getKind());
        String traceparent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getSpanContext().getSpanId() + "-01";
        assertEquals(message.getProperties().get("Diagnostic-Id"), traceparent);
        assertEquals(message.getProperties().get("traceparent"), traceparent);
    }

    private void assertSendSpan(ReadableSpan actual, List<EventData> messages, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
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
        List<LinkData> links = actual.toSpanData().getLinks();
        /* TODO (lmolkova) uncomment after azure-core-tracing-opentelemetry 1.0.0-beat.29 ships
        assertEquals(messages.size(), links.size());
        for (int i = 0; i < links.size(); i++) {
            String messageTraceparent = (String) messages.get(i).getData().getProperties().get("traceparent");
            SpanContext linkContext = links.get(i).getSpanContext();
            String linkTraceparent = "00-" + linkContext.getTraceId() + "-" + linkContext.getSpanId() + "-01";
            assertEquals(messageTraceparent, linkTraceparent);
            assertNotNull(links.get(i).getAttributes().get(AttributeKey.longKey(Tracer.MESSAGE_ENQUEUED_TIME)));
        }*/
    }

    private void assertConsumerSpan(ReadableSpan actual, EventData message, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(0, actual.toSpanData().getLinks().size());

        String messageTraceparent = (String) message.getProperties().get("traceparent");
        if (messageTraceparent == null) {
            assertFalse(actual.getParentSpanContext().isValid());
        } else {
            String parent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getParentSpanContext().getSpanId() + "-01";
            assertEquals(messageTraceparent, parent);
        }
    }

    private void assertConsumerSpan(ReadableSpan actual, List<EventData> messages, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        /* TODO (lmolkova) uncomment after azure-core-tracing-opentelemetry 1.0.0-beta.29 ships
        assertEquals(messages.size(), actual.toSpanData().getLinks().size());
        List<LinkData> links =  actual.toSpanData().getLinks();
        for (EventData data : messages) {
            String messageTraceparent = (String) data.getProperties().get("traceparent");
            List<LinkData> link = links.stream().filter(l -> {
                String linkedContext = "00-" + l.getSpanContext().getTraceId() + "-" + l.getSpanContext().getSpanId() + "-01";
                return linkedContext.equals(messageTraceparent);
            }).collect(Collectors.toList());
            assertEquals(1, link.size());
            assertNotNull(link.get(0).getAttributes().get(AttributeKey.longKey(Tracer.MESSAGE_ENQUEUED_TIME)));
        }*/
    }

    private List<ReadableSpan> findSpans(List<ReadableSpan> spans, String spanName) {
        return spans.stream()
            .filter(s -> s.getName().equals(spanName))
            .collect(Collectors.toList());
    }

    static class TestSpanProcessor implements SpanProcessor {
        private final ConcurrentLinkedDeque<ReadableSpan> spans = new ConcurrentLinkedDeque<>();
        private final String entityName;
        private final String namespace;

        private AtomicReference<Consumer<ReadableSpan>> notifier = new AtomicReference<>();

        TestSpanProcessor(String namespace, String entityName) {
            this.namespace = namespace;
            this.entityName = entityName;
        }
        public List<ReadableSpan> getEndedSpans() {
            return spans.stream().collect(Collectors.toList());
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
            assertEquals(entityName, readableSpan.getAttribute(AttributeKey.stringKey("message_bus.destination")));
            assertEquals(namespace, readableSpan.getAttribute(AttributeKey.stringKey("peer.address")));

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
