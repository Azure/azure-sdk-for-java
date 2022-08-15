// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.DeferOptions;
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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class TracingIntegrationTests extends IntegrationTestBase {
    private TestSpanProcessor spanProcessor;
    private ServiceBusSenderAsyncClient sender;
    ServiceBusReceiverAsyncClient receiver;
    ServiceBusReceiverClient receiverSync;
    ServiceBusProcessorClient processor;

    public TracingIntegrationTests() {
        super(new ClientLogger(TracingIntegrationTests.class));
    }

    @Override
    protected void beforeTest() {
        spanProcessor = new TestSpanProcessor(getFullyQualifiedDomainName(), getQueueName(0));
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(spanProcessor)
                    .build())
            .buildAndRegisterGlobal();

        sender = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .sender()
            .queueName(getQueueName(0))
            .buildAsyncClient();

        receiver = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .receiver()
            .queueName(getQueueName(0))
            .buildAsyncClient(false, false);

        receiverSync = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .receiver()
            .queueName(getQueueName(0))
            .buildClient();
    }

    @Override
    protected void afterTest() {
        GlobalOpenTelemetry.resetForTest();
        sharedBuilder = null;
        try {
            dispose(receiver, sender, processor, receiverSync);
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        }
    }

    @Test
    public void sendAndReceive() throws InterruptedException {
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES);
        StepVerifier.create(sender.sendMessage(message))
            .verifyComplete();

        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        StepVerifier.create(receiver.receiveMessages()
                .flatMap(rm -> receiver.complete(rm).thenReturn(rm))
                .take(1))
            .assertNext(rm -> {
                receivedMessage.set(rm);
            })
            .verifyComplete();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        assertMessageSpan(spans.get(0), message);
        assertSendSpan(spans.get(1), Collections.singletonList(message), "ServiceBus.send");

        List<ReadableSpan> received = findSpans(spans, "ServiceBus.process");
        assertConsumerSpan(received.get(0), receivedMessage.get(), "ServiceBus.process");

        List<ReadableSpan> completed = findSpans(spans, "ServiceBus.complete");
        assertReceiveSpan(completed.get(0), Collections.singletonList(receivedMessage.get()), "ServiceBus.complete");

    }

    @Test
    public void sendPeekRenewLockAndDefer() {
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(CONTENTS_BYTES)))
            .verifyComplete();

        Flux<ServiceBusReceivedMessage> receive = receiver.receiveMessages()
            .take(1)
            .flatMap(m -> receiver.renewMessageLock(m, Duration.ofSeconds(1)).thenReturn(m))
            .flatMap(m -> receiver.defer(m, new DeferOptions()).thenReturn(m))
            .flatMap(m -> receiver.receiveDeferredMessage(m.getSequenceNumber()).thenReturn(m));

        StepVerifier.create(receive.take(1))
            .assertNext(receivedMessage ->  {
                List<ReadableSpan> spans = spanProcessor.getEndedSpans();

                List<ReadableSpan> received = findSpans(spans, "ServiceBus.process");
                assertConsumerSpan(received.get(0), receivedMessage, "ServiceBus.process");

                List<ReadableSpan> renewMessageLock = findSpans(spans, "ServiceBus.renewMessageLock");
                assertReceiveSpan(renewMessageLock.get(0), Collections.singletonList(receivedMessage), "ServiceBus.renewMessageLock");

                List<ReadableSpan> defer = findSpans(spans, "ServiceBus.defer");
                assertReceiveSpan(defer.get(0), Collections.singletonList(receivedMessage), "ServiceBus.defer");

                List<ReadableSpan> receiveDeferredMessage = findSpans(spans, "ServiceBus.receiveDeferredMessage");
                assertReceiveSpan(receiveDeferredMessage.get(0), Collections.singletonList(receivedMessage), "ServiceBus.receiveDeferredMessage");
            })
            .verifyComplete();
    }

    @Test
    public void sendReceiveRenewLockAndDeferSync() {
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(CONTENTS_BYTES)))
            .verifyComplete();

        ServiceBusReceivedMessage receivedMessage = receiverSync.receiveMessages(1, Duration.ofSeconds(1)).stream().findFirst().get();

        receiverSync.renewMessageLock(receivedMessage);
        receiverSync.defer(receivedMessage, new DeferOptions());
        receiverSync.receiveDeferredMessage(receivedMessage.getSequenceNumber());

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> received = findSpans(spans, "ServiceBus.receiveMessages");
        assertReceiveSpan(received.get(0), Collections.singletonList(receivedMessage), "ServiceBus.receiveMessages");

        List<ReadableSpan> renewMessageLock = findSpans(spans, "ServiceBus.renewMessageLock");
        assertReceiveSpan(renewMessageLock.get(0), Collections.singletonList(receivedMessage), "ServiceBus.renewMessageLock");

        List<ReadableSpan> defer = findSpans(spans, "ServiceBus.defer");
        assertReceiveSpan(defer.get(0), Collections.singletonList(receivedMessage), "ServiceBus.defer");

        List<ReadableSpan> receiveDeferredMessage = findSpans(spans, "ServiceBus.receiveDeferredMessage");
        assertReceiveSpan(receiveDeferredMessage.get(0), Collections.singletonList(receivedMessage), "ServiceBus.receiveDeferredMessage");
    }

    @Test
    public void syncReceive() {
        List<ServiceBusMessage> messages = new ArrayList<>();
        messages.add(new ServiceBusMessage(CONTENTS_BYTES));
        messages.add(new ServiceBusMessage(CONTENTS_BYTES));

        StepVerifier.create(sender.sendMessages(messages))
            .verifyComplete();

        List<ServiceBusReceivedMessage> receivedMessages = receiverSync.receiveMessages(2, Duration.ofSeconds(10))
            .stream().collect(Collectors.toList());
        receivedMessages.forEach(receiverSync::complete);

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> received = findSpans(spans, "ServiceBus.receiveMessages");
        assertReceiveSpan(received.get(0), receivedMessages, "ServiceBus.receiveMessages");

        assertEquals(0, findSpans(spans, "ServiceBus.process").size());

        List<ReadableSpan> completed = findSpans(spans, "ServiceBus.complete");
        assertReceiveSpan(completed.get(0), Collections.singletonList(receivedMessages.get(0)), "ServiceBus.complete");
        assertReceiveSpan(completed.get(1), Collections.singletonList(receivedMessages.get(1)), "ServiceBus.complete");
    }

    @Test
    public void syncReceiveTimeout() {
        List<ServiceBusReceivedMessage> receivedMessages = receiverSync.receiveMessages(100, Duration.ofMillis(1))
            .stream().collect(Collectors.toList());

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> received = findSpans(spans, "ServiceBus.receiveMessages");
        assertReceiveSpan(received.get(0), receivedMessages, "ServiceBus.receiveMessages");
        assertEquals(StatusCode.OK, received.get(0).toSpanData().getStatus().getStatusCode());

        assertEquals(0, findSpans(spans, "ServiceBus.process").size());
    }

    @Test
    public void peekMessage() {
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(CONTENTS_BYTES)))
            .verifyComplete();

        StepVerifier.create(receiver.peekMessage())
            .assertNext(receivedMessage -> {
                List<ReadableSpan> received = findSpans(spanProcessor.getEndedSpans(), "ServiceBus.peekMessage");
                assertReceiveSpan(received.get(0), Collections.singletonList(receivedMessage), "ServiceBus.peekMessage");
            })
            .verifyComplete();
    }

    @Test
    public void sendAndProcess() throws InterruptedException {
        String messageId = UUID.randomUUID().toString();
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES)
            .setMessageId(messageId);

        StepVerifier.create(sender.sendMessage(message))
            .verifyComplete();

        String message1SpanId = message.getApplicationProperties().get("traceparent").toString().substring(36, 52);
        CountDownLatch completedFound = new CountDownLatch(1);
        spanProcessor.notifyIfCondition(completedFound, span -> {
            if (span.getName() != "ServiceBus.complete") {
                return false;
            }
            List<LinkData> links = span.toSpanData().getLinks();
            return links.size() > 0 && links.get(0).getSpanContext().getSpanId().equals(message1SpanId);
        });

        AtomicReference<Span> currentInProcess = new AtomicReference<>(Span.getInvalid());
        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        processor = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .processor()
            .queueName(getQueueName(0))
            .processMessage(mc -> {
                if (mc.getMessage().getMessageId().equals(messageId)) {
                    currentInProcess.set(Span.current());
                    receivedMessage.set(mc.getMessage());
                }
            })
            .processError(e -> {
                fail("unexpected error", e.getException());
            })
            .buildProcessorClient();

        processor.start();
        assertTrue(completedFound.await(10, TimeUnit.SECONDS));
        processor.stop();

        assertTrue(currentInProcess.get().getSpanContext().isValid());
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        assertMessageSpan(spans.get(0), message);
        assertSendSpan(spans.get(1), Collections.singletonList(message), "ServiceBus.send");

        List<ReadableSpan> processed = findSpans(spans, "ServiceBus.process")
            .stream().filter(p -> p == currentInProcess.get()).collect(Collectors.toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), receivedMessage.get(), "ServiceBus.process");

        List<ReadableSpan> completed = findSpans(spans, "ServiceBus.complete").stream()
            .filter(c -> c.toSpanData().getLinks().get(0).getSpanContext().getSpanId().equals(message1SpanId))
            .collect(Collectors.toList());
        assertEquals(1, completed.size());
        assertSendSpan(completed.get(0), Collections.singletonList(message), "ServiceBus.complete");
    }

    @Test
    public void sendProcessAndFail() throws InterruptedException {
        String messageId = UUID.randomUUID().toString();
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES)
            .setMessageId(messageId);

        StepVerifier.create(sender.sendMessage(message))
            .verifyComplete();

        String message1SpanId = message.getApplicationProperties().get("traceparent").toString().substring(36, 52);
        CountDownLatch messageProcessed = new CountDownLatch(1);
        spanProcessor.notifyIfCondition(messageProcessed, span ->
            span.getName() == "ServiceBus.process" && span.getParentSpanContext().getSpanId().equals(message1SpanId));

        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        processor = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .processor()
            .queueName(getQueueName(0))
            .processMessage(mc -> {
                if (mc.getMessage().getMessageId().equals(messageId)) {
                    receivedMessage.set(mc.getMessage());
                    throw new RuntimeException("foo");
                }
            })
            .processError(e -> { })
            .buildProcessorClient();

        processor.start();
        assertTrue(messageProcessed.await(10, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        List<ReadableSpan> processed = findSpans(spans, "ServiceBus.process")
            .stream().filter(p -> p.getParentSpanContext().isValid())
            .filter(p -> p.toSpanData().getStatus().getStatusCode() == StatusCode.ERROR)
            .collect(Collectors.toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), receivedMessage.get(), "ServiceBus.process");

        List<ReadableSpan> abandoned = findSpans(spans, "ServiceBus.abandon").stream()
            .filter(c -> c.toSpanData().getLinks().get(0).getSpanContext().getSpanId().equals(message1SpanId))
            .collect(Collectors.toList());
        assertEquals(1, abandoned.size());
        assertSendSpan(abandoned.get(0), Collections.singletonList(message), "ServiceBus.abandon");
    }

    @Test
    public void scheduleAndCancelMessage() {
        ServiceBusMessage message = new ServiceBusMessage("m");
        StepVerifier.create(
                sender.scheduleMessage(message, OffsetDateTime.now().plusSeconds(100))
                .flatMap(l -> sender.cancelScheduledMessage(l)))
            .verifyComplete();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertMessageSpan(spans.get(0), message);
        assertSendSpan(spans.get(1), Collections.singletonList(message), "ServiceBus.scheduleMessage");
        assertSendSpan(spans.get(2), Collections.emptyList(), "ServiceBus.cancelScheduledMessage");
    }

    private void assertMessageSpan(ReadableSpan actual, ServiceBusMessage message) {
        assertEquals("ServiceBus.message", actual.getName());
        assertEquals(SpanKind.PRODUCER, actual.getKind());
        String traceparent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getSpanContext().getSpanId() + "-01";
        assertEquals(message.getApplicationProperties().get("Diagnostic-Id"), traceparent);
        assertEquals(message.getApplicationProperties().get("traceparent"), traceparent);
    }

    private void assertSendSpan(ReadableSpan actual, List<ServiceBusMessage> messages, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
        List<LinkData> links = actual.toSpanData().getLinks();
        assertEquals(messages.size(), links.size());
        for (int i = 0; i < links.size(); i++) {
            String messageTraceparent = (String) messages.get(i).getApplicationProperties().get("traceparent");
            SpanContext linkContext = links.get(i).getSpanContext();
            String linkTraceparent = "00-" + linkContext.getTraceId() + "-" + linkContext.getSpanId() + "-01";
            assertEquals(messageTraceparent, linkTraceparent);
        }
    }

    private void assertReceiveSpan(ReadableSpan actual, List<ServiceBusReceivedMessage> messages, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
        List<LinkData> links = actual.toSpanData().getLinks();
        assertEquals(messages.size(), links.size());
        for (int i = 0; i < links.size(); i++) {
            String messageTraceparent = (String) messages.get(i).getApplicationProperties().get("traceparent");
            SpanContext linkContext = links.get(i).getSpanContext();
            String linkTraceparent = "00-" + linkContext.getTraceId() + "-" + linkContext.getSpanId() + "-01";
            assertEquals(messageTraceparent, linkTraceparent);
        }
    }

    private void assertConsumerSpan(ReadableSpan actual, ServiceBusReceivedMessage message, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(0, actual.toSpanData().getLinks().size());

        String messageTraceparent = (String) message.getApplicationProperties().get("traceparent");
        String parent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getParentSpanContext().getSpanId() + "-01";
        assertEquals(messageTraceparent, parent);
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
            assertEquals("Microsoft.ServiceBus", readableSpan.getAttribute(AttributeKey.stringKey("az.namespace")));
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
