// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.DeferOptions;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.LinkData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Execution(ExecutionMode.SAME_THREAD)
public class TracingIntegrationTests extends IntegrationTestBase {
    private ClientOptions clientOptions;
    private TestSpanProcessor spanProcessor;
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverClient receiverSync;
    private ServiceBusProcessorClient processor;

    public TracingIntegrationTests() {
        super(new ClientLogger(TracingIntegrationTests.class));
    }

    @Override
    protected void beforeTest() {
        spanProcessor = new TestSpanProcessor(TestUtils.getFullyQualifiedDomainName(true), getQueueName(0));
        OpenTelemetryTracingOptions tracingOptions = new OpenTelemetryTracingOptions()
            .setOpenTelemetry(OpenTelemetrySdk.builder()
                .setTracerProvider(
                    SdkTracerProvider.builder()
                        .addSpanProcessor(spanProcessor)
                        .build())
                .build());
        clientOptions = new ClientOptions().setTracingOptions(tracingOptions);

        final ServiceBusClientBuilder senderBuilder = getAuthenticatedBuilder();
        sender = toClose(senderBuilder
            .clientOptions(clientOptions)
            .sender()
            .queueName(getQueueName(0))
            .buildAsyncClient());

        final ServiceBusClientBuilder receiverBuilder = getAuthenticatedBuilder();
        receiver = toClose(receiverBuilder
            .clientOptions(clientOptions)
            .receiver()
            .maxAutoLockRenewDuration(Duration.ZERO)
            .queueName(getQueueName(0))
            .disableAutoComplete()
            .buildAsyncClient());

        final ServiceBusClientBuilder receiverSyncBuilder = getAuthenticatedBuilder();
        receiverSync = toClose(receiverSyncBuilder
            .clientOptions(clientOptions)
            .receiver()
            .queueName(getQueueName(0))
            .buildClient());
    }

    @Override
    protected void afterTest() {
        GlobalOpenTelemetry.resetForTest();
        sharedBuilder = null;
    }

    @Test
    public void sendAndReceive() throws InterruptedException {
        ServiceBusMessage message1 = new ServiceBusMessage(CONTENTS_BYTES);
        ServiceBusMessage message2 = new ServiceBusMessage(CONTENTS_BYTES);
        List<ServiceBusMessage> messages = Arrays.asList(message1, message2);
        StepVerifier.create(sender.sendMessages(messages))
            .expectComplete()
            .verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(processedFound, s -> s.getName().equals("ServiceBus.process"));

        List<ServiceBusReceivedMessage> received = new ArrayList<>();
        Disposable subscription = receiver.receiveMessages()
            .take(2)
            .doOnNext(msg -> {
                received.add(msg);
                String traceparent = (String) msg.getApplicationProperties().get("traceparent");
                String traceId = Span.current().getSpanContext().getTraceId();

                // context created for the message and current are the same
                assertTrue(traceparent.startsWith("00-" + traceId));
                assertFalse(((ReadableSpan) Span.current()).hasEnded());
                receiver.complete(msg).block();
            })
            .subscribe();
        toClose(subscription);
        assertTrue(processedFound.await(20, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> messageSpans = findSpans(spans, "ServiceBus.message");
        assertMessageSpan(messageSpans.get(0), message1);
        assertMessageSpan(messageSpans.get(1), message2);

        List<ReadableSpan> send = findSpans(spans, "ServiceBus.send");
        assertClientProducerSpan(send.get(0), messages, "ServiceBus.send", "publish");

        List<ReadableSpan> processed = findSpans(spans, "ServiceBus.process");
        assertConsumerSpan(processed.get(0), received.get(0), "ServiceBus.process");
        assertConsumerSpan(processed.get(1), received.get(1), "ServiceBus.process");

        List<ReadableSpan> completed = findSpans(spans, "ServiceBus.complete");
        assertClientSpan(completed.get(0), Collections.singletonList(received.get(0)), "ServiceBus.complete", "settle");
        assertClientSpan(completed.get(1), Collections.singletonList(received.get(1)), "ServiceBus.complete", "settle");
        assertSettledVsProcessed(completed, processed, messages.size());
    }

    @Test
    public void receiveAndRenewLockWithDuration() throws InterruptedException {
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES);
        StepVerifier.create(sender.sendMessage(message)).expectComplete().verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(processedFound, s -> s.getName().equals("ServiceBus.process") || s.getName().equals("ServiceBus.renewMessageLock"));

        AtomicReference<ServiceBusReceivedMessage> received = new AtomicReference<>();
        StepVerifier.create(receiver.receiveMessages()
            .next()
            .flatMap(msg -> receiver.renewMessageLock(msg, Duration.ofSeconds(2))
                    .thenReturn(msg)))
            .assertNext(msg -> received.compareAndSet(null, msg))
            .expectComplete()
            .verify(TIMEOUT);
        assertTrue(processedFound.await(30, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> processed = findSpans(spans, "ServiceBus.process");
        assertConsumerSpan(processed.get(0), received.get(), "ServiceBus.process");

        List<ReadableSpan> renewLock = findSpans(spans, "ServiceBus.renewMessageLock");
        assertClientSpan(renewLock.get(0), Collections.singletonList(received.get()), "ServiceBus.renewMessageLock", null);
    }

    @Test
    public void receiveAndRenewSessionLockWithDuration() throws InterruptedException {
        String sessionId = "10";
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES)
            .setSessionId(sessionId);

        OpenTelemetry otel = configureOTel(TestUtils.getFullyQualifiedDomainName(true), getSessionQueueName(0));
        final ServiceBusClientBuilder senderBuilder = getAuthenticatedBuilder();
        sender = toClose(senderBuilder
            .clientOptions(new ClientOptions().setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(otel)))
            .sender()
            .queueName(getSessionQueueName(0))
            .buildAsyncClient());

        final ServiceBusClientBuilder receiverBuilder = getAuthenticatedBuilder();
        ServiceBusSessionReceiverAsyncClient sessionReceiver = toClose(receiverBuilder
            .clientOptions(new ClientOptions().setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(otel)))
            .sessionReceiver()
            .disableAutoComplete()
            .queueName(getSessionQueueName(0))
            .buildAsyncClient());

        StepVerifier.create(sender.sendMessage(message)).expectComplete().verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(processedFound, s -> s.getName().equals("ServiceBus.process") || s.getName().equals("ServiceBus.renewSessionLock"));

        AtomicReference<ServiceBusReceivedMessage> received = new AtomicReference<>();
        StepVerifier.create(
            sessionReceiver
                .acceptSession(sessionId)
                .flatMapMany(rec -> rec
                    .renewSessionLock()
                    .then(rec.receiveMessages().next())))
            .assertNext(msg -> {
                received.set(msg);
                logger.atInfo()
                    .addKeyValue("lockedUntil", msg.getLockedUntil())
                    .addKeyValue("sessionId", msg.getSessionId())
                    .log("message received");
            })
            .expectComplete()
            .verify(TIMEOUT);

        assertTrue(processedFound.await(30, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> processed = findSpans(spans, "ServiceBus.process");
        assertConsumerSpan(processed.get(0), received.get(), "ServiceBus.process");

        List<ReadableSpan> acceptSession = findSpans(spans, "ServiceBus.acceptSession");
        assertClientSpan(acceptSession.get(0), Collections.emptyList(), "ServiceBus.acceptSession", null);

        List<ReadableSpan> renewLock = findSpans(spans, "ServiceBus.renewSessionLock");
        assertClientSpan(renewLock.get(0), Collections.emptyList(), "ServiceBus.renewSessionLock", null);
    }

    @Test
    public void receiveCheckSubscribe() throws InterruptedException {
        ServiceBusMessage message1 = new ServiceBusMessage(CONTENTS_BYTES);
        ServiceBusMessage message2 = new ServiceBusMessage(CONTENTS_BYTES);
        List<ServiceBusMessage> messages = Arrays.asList(message1, message2);
        StepVerifier.create(sender.sendMessages(messages))
            .expectComplete()
            .verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(processedFound, s -> s.getName().equals("ServiceBus.process"));

        Disposable subscription = receiver.receiveMessages()
            .take(messages.size())
            .subscribe(msg -> {
                String traceparent = (String) msg.getApplicationProperties().get("traceparent");
                String traceId = Span.current().getSpanContext().getTraceId();

                // context created for the message and current are the same
                assertTrue(traceparent.startsWith("00-" + traceId));
                assertFalse(((ReadableSpan) Span.current()).hasEnded());
                receiver.complete(msg).block();
            });
        toClose(subscription);
        assertTrue(processedFound.await(20, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertSettledVsProcessed(findSpans(spans, "ServiceBus.complete"), findSpans(spans, "ServiceBus.process"), messages.size());
    }

    @Test
    public void sendAndReceiveParallelNoAutoCompleteAndLockRenewal() throws InterruptedException {
        int messageCount = 5;
        StepVerifier.create(sender.createMessageBatch()
            .doOnNext(batch -> {
                for (int i = 0; i < messageCount; i++) {
                    batch.tryAddMessage(new ServiceBusMessage(CONTENTS_BYTES));
                }
            })
            .flatMap(batch -> sender.sendMessages(batch)))
            .expectComplete()
            .verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(messageCount);
        spanProcessor.notifyIfCondition(processedFound, span -> span.getName().equals("ServiceBus.process"));

        final ServiceBusClientBuilder receiverBuilder = getAuthenticatedBuilder();
        ServiceBusReceiverAsyncClient receiverWithLockRenewal = toClose(receiverBuilder
            .clientOptions(clientOptions)
            .receiver()
            .queueName(getQueueName(0))
            .disableAutoComplete()
            .buildAsyncClient());

        StepVerifier.create(
                receiverWithLockRenewal.receiveMessages()
                    .take(messageCount)
                    .doOnNext(msg -> {
                        if (Span.current().getSpanContext().isValid()) {
                            String traceparent = (String) msg.getApplicationProperties().get("traceparent");
                            String traceId = Span.current().getSpanContext().getTraceId();

                            // context created for the message and current are the same
                            assertTrue(traceparent.startsWith("00-" + traceId));
                            assertFalse(((ReadableSpan) Span.current()).hasEnded());
                        }
                        receiver.complete(msg).block();
                    })
                    .parallel(messageCount, 1)
                    .runOn(Schedulers.boundedElastic(), 1))
            .expectNextCount(messageCount)
            .expectComplete()
            .verify(TIMEOUT);

        assertTrue(processedFound.await(20, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        // all completed spans should have a parent, but complete call may start after the parent span has ended
        // the last part heavily depends on how above code is written wrt parallelization.
        // in the current form (receive -> doonnext (complete) -> parallel)
        // complete is happens synchronously from the receive perspective
        // so complete will finish before processing is completed.
        //
        // if complete call is done differently (e.g. receive -> parallel -> runon -> doonnext (complete))
        // then doonnext callbacks become async and may happen after receiver call has completed
        // then complete will be a child of process, but will last longer than processing
        // TODO (limolkova): this is another good reason to rename receiver's span to delivery instead of processing.
        assertSettledVsProcessed(findSpans(spans, "ServiceBus.complete"), findSpans(spans, "ServiceBus.process"), messageCount);
    }

    @Test
    public void sendAndReceiveParallelAutoComplete() throws InterruptedException {
        int messageCount = 5;
        StepVerifier.create(sender.createMessageBatch()
            .doOnNext(batch -> {
                for (int i = 0; i < messageCount; i++) {
                    batch.tryAddMessage(new ServiceBusMessage(CONTENTS_BYTES));
                }
            })
            .flatMap(batch -> sender.sendMessages(batch)))
            .expectComplete()
            .verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(messageCount);
        spanProcessor.notifyIfCondition(processedFound, span -> span.getName().equals("ServiceBus.process"));

        final ServiceBusClientBuilder receiverBuilder = getAuthenticatedBuilder();
        ServiceBusReceiverAsyncClient receiverAutoComplete = toClose(receiverBuilder
            .clientOptions(clientOptions)
            .receiver()
            .queueName(getQueueName(0))
            .buildAsyncClient());

        StepVerifier.create(
                receiverAutoComplete.receiveMessages()
                .take(messageCount)
                .doOnNext(msg -> {
                    if (Span.current().getSpanContext().isValid()) {
                        String traceparent = (String) msg.getApplicationProperties().get("traceparent");
                        String traceId = Span.current().getSpanContext().getTraceId();

                        // context created for the message and current are the same
                        assertTrue(traceparent.startsWith("00-" + traceId));
                        assertFalse(((ReadableSpan) Span.current()).hasEnded());
                    }
                })
                .parallel(messageCount, 1)
                .runOn(Schedulers.boundedElastic(), 1))
            .expectNextCount(messageCount)
            .expectComplete()
            .verify(TIMEOUT);
        assertTrue(processedFound.await(20, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertSettledVsProcessed(findSpans(spans, "ServiceBus.complete"), findSpans(spans, "ServiceBus.process"), messageCount);
    }

    @Test
    public void sendReceiveRenewLockAndDefer() throws InterruptedException {
        String traceId = IdGenerator.random().generateTraceId();
        String traceparent = "00-" + traceId + "-" + IdGenerator.random().generateSpanId() + "-01";
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES);
        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        message.getApplicationProperties().put("traceparent", traceparent);

        StepVerifier.create(sender.sendMessage(message)).expectComplete().verify(TIMEOUT);

        CountDownLatch latch = new CountDownLatch(1);
        spanProcessor.notifyIfCondition(latch, s -> s.getName().equals("ServiceBus.receiveDeferredMessage"));
        toClose(receiver.receiveMessages()
            .filter(m -> traceparent.equals(m.getApplicationProperties().get("traceparent")))
            .flatMap(m -> receiver.renewMessageLock(m).thenReturn(m))
            .flatMap(m -> receiver.defer(m, new DeferOptions()).thenReturn(m))
            .flatMap(m -> receiver.receiveDeferredMessage(m.getSequenceNumber()).thenReturn(m))
            .subscribe(m -> {
                receivedMessage.compareAndSet(null, m);
            }));
        assertTrue(latch.await(120, TimeUnit.SECONDS));

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        assertEquals(0, findSpans(spans, "ServiceBus.message").size());

        List<ReadableSpan> send = findSpans(spans, "ServiceBus.send");
        assertClientProducerSpan(send.get(0), Collections.singletonList(message), "ServiceBus.send", "publish");

        List<ReadableSpan> process = findSpans(spans, "ServiceBus.process", traceId);
        assertConsumerSpan(process.get(0), receivedMessage.get(), "ServiceBus.process");

        List<ReadableSpan> renewMessageLock = findSpans(spans, "ServiceBus.renewMessageLock", traceId);
        assertClientSpan(renewMessageLock.get(0), Collections.singletonList(receivedMessage.get()), "ServiceBus.renewMessageLock", null);
        assertParent(renewMessageLock.get(0), process.get(0));

        // for correlation to work after first async call, we need to enable otel rector instrumentations,
        // so no correlation beyond this point
        List<ReadableSpan> defer = findSpans(spans, "ServiceBus.defer");
        assertClientSpan(defer.get(0), Collections.singletonList(receivedMessage.get()), "ServiceBus.defer", "settle");

        List<ReadableSpan> receiveDeferredMessage = findSpans(spans, "ServiceBus.receiveDeferredMessage");
        assertClientSpan(receiveDeferredMessage.get(0), Collections.singletonList(receivedMessage.get()), "ServiceBus.receiveDeferredMessage", "receive");
    }

    @Test
    public void sendReceiveRenewLockAndDeferSync() {
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(CONTENTS_BYTES)))
            .expectComplete()
            .verify(TIMEOUT);

        ServiceBusReceivedMessage receivedMessage = null;
        while (receivedMessage == null) {
            IterableStream<ServiceBusReceivedMessage> messages = receiverSync.receiveMessages(1, Duration.ofSeconds(10));
            if (messages.iterator().hasNext()) {
                receivedMessage = messages.iterator().next();
            }
        }

        receiverSync.renewMessageLock(receivedMessage);
        receiverSync.defer(receivedMessage, new DeferOptions());
        receiverSync.receiveDeferredMessage(receivedMessage.getSequenceNumber());

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> received = findSpans(spans, "ServiceBus.receiveMessages");
        assertClientSpan(received.get(0), Collections.singletonList(receivedMessage), "ServiceBus.receiveMessages", "receive");

        List<ReadableSpan> renewMessageLock = findSpans(spans, "ServiceBus.renewMessageLock");
        assertClientSpan(renewMessageLock.get(0), Collections.singletonList(receivedMessage), "ServiceBus.renewMessageLock", null);

        List<ReadableSpan> defer = findSpans(spans, "ServiceBus.defer");
        assertClientSpan(defer.get(0), Collections.singletonList(receivedMessage), "ServiceBus.defer", "settle");

        List<ReadableSpan> receiveDeferredMessage = findSpans(spans, "ServiceBus.receiveDeferredMessage");
        assertClientSpan(receiveDeferredMessage.get(0), Collections.singletonList(receivedMessage), "ServiceBus.receiveDeferredMessage", "receive");
    }

    @Test
    public void syncReceive() {
        List<ServiceBusMessage> messages = new ArrayList<>();
        messages.add(new ServiceBusMessage(CONTENTS_BYTES));
        messages.add(new ServiceBusMessage(CONTENTS_BYTES));

        StepVerifier.create(sender.sendMessages(messages))
            .expectComplete()
            .verify(TIMEOUT);

        List<ServiceBusReceivedMessage> receivedMessages = receiverSync.receiveMessages(2, Duration.ofSeconds(10))
            .stream().collect(Collectors.toList());
        receivedMessages.forEach(receiverSync::complete);

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> received = findSpans(spans, "ServiceBus.receiveMessages");
        assertClientSpan(received.get(0), receivedMessages, "ServiceBus.receiveMessages", "receive");

        assertEquals(0, findSpans(spans, "ServiceBus.process").size());

        List<ReadableSpan> completed = findSpans(spans, "ServiceBus.complete");
        assertClientSpan(completed.get(0), Collections.singletonList(receivedMessages.get(0)), "ServiceBus.complete", "settle");
        assertClientSpan(completed.get(1), Collections.singletonList(receivedMessages.get(1)), "ServiceBus.complete", "settle");
    }

    @Test
    public void syncReceiveTimeout() {
        List<ServiceBusReceivedMessage> receivedMessages = receiverSync.receiveMessages(100, Duration.ofMillis(1))
            .stream().collect(Collectors.toList());

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> received = findSpans(spans, "ServiceBus.receiveMessages");
        assertClientSpan(received.get(0), receivedMessages, "ServiceBus.receiveMessages", "receive");
        assertEquals(StatusCode.UNSET, received.get(0).toSpanData().getStatus().getStatusCode());

        assertEquals(0, findSpans(spans, "ServiceBus.process").size());
    }

    @Test
    public void peekMessage() {
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(CONTENTS_BYTES)))
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(receiver.peekMessage())
            .assertNext(receivedMessage -> {
                ReadableSpan received = findSpans(spanProcessor.getEndedSpans(), "ServiceBus.peekMessage").get(0);
                if (receivedMessage.getApplicationProperties().containsKey("traceparent")) {
                    logger.atInfo()
                        .addKeyValue("traceparent", receivedMessage.getApplicationProperties().get("traceparent"))
                        .log("span should have link");
                    assertClientSpan(received, Collections.singletonList(receivedMessage), "ServiceBus.peekMessage", "receive");
                } else {
                    assertEquals("ServiceBus.peekMessage", received.getName());
                    assertEquals(SpanKind.CLIENT, received.getKind());
                    assertEquals(0, received.toSpanData().getLinks().size());
                    assertEquals("receive", received.getAttribute(AttributeKey.stringKey("messaging.operation")));
                }
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void peekNonExistingMessage() {
        StepVerifier.create(receiver.peekMessage(Long.MAX_VALUE - 1))
            .expectComplete()
            .verify(TIMEOUT);

        List<ReadableSpan> received = findSpans(spanProcessor.getEndedSpans(), "ServiceBus.peekMessage");
        assertClientSpan(received.get(0), Collections.emptyList(), "ServiceBus.peekMessage", "receive");
    }

    @Test
    public void sendAndProcessNoAutoComplete() throws InterruptedException {
        String messageId = UUID.randomUUID().toString();
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES)
            .setMessageId(messageId);

        StepVerifier.create(sender.sendMessage(message))
            .expectComplete()
            .verify(TIMEOUT);

        String message1SpanId = message.getApplicationProperties().get("traceparent").toString().substring(36, 52);
        CountDownLatch processFound = new CountDownLatch(1);
        spanProcessor.notifyIfCondition(processFound, span -> span.getName().equals("ServiceBus.process") && span.getParentSpanContext().getSpanId().equals(message1SpanId));

        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        final ServiceBusClientBuilder processorBuilder = getAuthenticatedBuilder();
        processor = toClose(processorBuilder
            .clientOptions(clientOptions)
            .processor()
            .queueName(getQueueName(0))
            .processMessage(mc -> {
                if (mc.getMessage().getMessageId().equals(messageId)) {
                    currentInProcess.compareAndSet(null, Span.current());
                    receivedMessage.compareAndSet(null, mc.getMessage());
                }
                mc.complete();
            })
            .processError(e -> fail("unexpected error", e.getException()))
            .buildProcessorClient());

        toClose((AutoCloseable) () -> processor.stop());
        processor.start();
        assertTrue(processFound.await(20, TimeUnit.SECONDS));
        processor.stop();

        assertTrue(currentInProcess.get().getSpanContext().isValid());
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        assertMessageSpan(spans.get(0), message);
        assertClientProducerSpan(spans.get(1), Collections.singletonList(message), "ServiceBus.send", "publish");

        assertEquals(0, findSpans(spans, "ServiceBus.consume").size());
        List<ReadableSpan> processed = findSpans(spans, "ServiceBus.process")
            .stream().filter(p -> p.equals(currentInProcess.get())).collect(Collectors.toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), receivedMessage.get(), "ServiceBus.process");

        List<ReadableSpan> completed = findSpans(spans, "ServiceBus.complete").stream()
            .filter(c -> {
                List<LinkData> links = c.toSpanData().getLinks();
                return !links.isEmpty() && links.get(0).getSpanContext().getSpanId().equals(message1SpanId);
            })
            .collect(Collectors.toList());
        assertEquals(1, completed.size());
        assertClientProducerSpan(completed.get(0), Collections.singletonList(message), "ServiceBus.complete", "settle");

        assertSettledVsProcessed(completed, processed, 1);
    }

    @Test
    public void sendAndProcessSessionNoAutoComplete() throws InterruptedException {
        String sessionQueueName = getSessionQueueName(0);
        String sessionId = UUID.randomUUID().toString();
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES).setSessionId(sessionId);

        OpenTelemetry otel = configureOTel(TestUtils.getFullyQualifiedDomainName(true), sessionQueueName);
        clientOptions = new ClientOptions().setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(otel));
        final ServiceBusClientBuilder senderBuilder = getAuthenticatedBuilder();
        sender = toClose(senderBuilder
            .clientOptions(clientOptions)
            .sender()
            .queueName(sessionQueueName)
            .buildAsyncClient());

        StepVerifier.create(sender.sendMessage(message)).expectComplete().verify(TIMEOUT);

        CountDownLatch processFound = new CountDownLatch(2);
        spanProcessor.notifyIfCondition(processFound, span -> span.getName().equals("ServiceBus.process") || span.getName().equals("ServiceBus.complete"));

        AtomicReference<Span> currentInProcess = new AtomicReference<>();
        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        final ServiceBusClientBuilder processorBuilder = getAuthenticatedBuilder();
        processor = toClose(processorBuilder
            .clientOptions(clientOptions)
            .sessionProcessor()
            .maxConcurrentSessions(1)
            .queueName(sessionQueueName)
            .disableAutoComplete()
            .processMessage(mc -> {
                currentInProcess.compareAndSet(null, Span.current());
                receivedMessage.compareAndSet(null, mc.getMessage());
                mc.complete();
            })
            .processError(e -> fail("unexpected error", e.getException()))
            .buildProcessorClient());

        toClose((AutoCloseable) () -> processor.stop());
        processor.start();
        assertTrue(processFound.await(20, TimeUnit.SECONDS));
        processor.stop();

        assertTrue(currentInProcess.get().getSpanContext().isValid());
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();

        List<ReadableSpan> processed = findSpans(spans, "ServiceBus.process")
            .stream().filter(p -> p.equals(currentInProcess.get())).collect(Collectors.toList());
        assertEquals(1, processed.size());
        assertConsumerSpan(processed.get(0), receivedMessage.get(), "ServiceBus.process");

        List<ReadableSpan> completed = findSpans(spans, "ServiceBus.complete");
        assertEquals(1, completed.size());
        assertClientSpan(completed.get(0), Collections.singletonList(receivedMessage.get()), "ServiceBus.complete", "settle");
        assertSettledVsProcessed(completed, processed, 1);
    }

    @Test
    public void sendAndProcessParallel() throws InterruptedException {
        int messageCount = 10;
        StepVerifier.create(sender.createMessageBatch()
                .doOnNext(batch -> {
                    for (int i = 0; i < messageCount; i++) {
                        batch.tryAddMessage(new ServiceBusMessage(CONTENTS_BYTES)
                            .setMessageId(UUID.randomUUID().toString()));
                    }
                })
                .flatMap(batch -> {
                    logMessages(batch.getMessages(), sender.getEntityPath(), "sending");
                    return sender.sendMessages(batch);
                }))
            .expectComplete()
            .verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(messageCount * 2);
        spanProcessor.notifyIfCondition(processedFound, span -> span.getName().equals("ServiceBus.process") || span.getName().equals("ServiceBus.complete"));

        final ServiceBusClientBuilder processorBuilder = getAuthenticatedBuilder();
        processor = toClose(processorBuilder
            .clientOptions(clientOptions)
            .processor()
            .queueName(getQueueName(0))
            .maxConcurrentCalls(messageCount)
            .processMessage(mc -> {
                logMessage(mc.getMessage(), processor.getQueueName(), "processing");
                String traceparent = (String) mc.getMessage().getApplicationProperties().get("traceparent");
                String traceId = Span.current().getSpanContext().getTraceId();

                // context created for the message and current are the same
                assertTrue(traceparent.startsWith("00-" + traceId));
                assertFalse(((ReadableSpan) Span.current()).hasEnded());
            })
            .processError(e -> fail("unexpected error", e.getException()))
            .buildProcessorClient());
        toClose((AutoCloseable) () -> processor.stop());
        processor.start();
        assertTrue(processedFound.await(30, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertSettledVsProcessed(findSpans(spans, "ServiceBus.complete"), findSpans(spans, "ServiceBus.process"), messageCount);
    }

    @Test
    public void sendAndProcessParallelNoAutoComplete() throws InterruptedException {
        int messageCount = 5;
        StepVerifier.create(sender.createMessageBatch()
            .doOnNext(batch -> {
                for (int i = 0; i < messageCount; i++) {
                    batch.tryAddMessage(new ServiceBusMessage(CONTENTS_BYTES));
                }
            })
            .flatMap(batch -> sender.sendMessages(batch)))
            .expectComplete()
            .verify(TIMEOUT);

        CountDownLatch processedFound = new CountDownLatch(messageCount);
        spanProcessor.notifyIfCondition(processedFound, span -> span.getName().equals("ServiceBus.process"));

        final ServiceBusClientBuilder processorBuilder = getAuthenticatedBuilder();
        processor = toClose(processorBuilder
            .clientOptions(clientOptions)
            .processor()
            .queueName(getQueueName(0))
            .maxConcurrentCalls(messageCount)
            .disableAutoComplete()
            .processMessage(mc -> {
                String traceparent = (String) mc.getMessage().getApplicationProperties().get("traceparent");
                String traceId = Span.current().getSpanContext().getTraceId();

                // context created for the message and current are the same
                assertTrue(traceparent.startsWith("00-" + traceId));
                mc.complete();
            })
            .processError(e -> fail("unexpected error", e.getException()))
            .buildProcessorClient());
        toClose((AutoCloseable) () -> processor.stop());
        processor.start();
        assertTrue(processedFound.await(20, TimeUnit.SECONDS));
        processor.stop();

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertSettledVsProcessed(findSpans(spans, "ServiceBus.complete"), findSpans(spans, "ServiceBus.process"), messageCount);
    }

    @Test
    public void sendProcessAndFail() throws InterruptedException {
        String messageId = UUID.randomUUID().toString();
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS_BYTES)
            .setMessageId(messageId);

        StepVerifier.create(sender.sendMessage(message))
            .expectComplete()
            .verify(TIMEOUT);

        String message1SpanId = message.getApplicationProperties().get("traceparent").toString().substring(36, 52);

        CountDownLatch messageProcessed = new CountDownLatch(1);
        spanProcessor.notifyIfCondition(messageProcessed, span ->
            "ServiceBus.process".equals(span.getName()) && span.getParentSpanContext().getSpanId().equals(message1SpanId));

        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        final ServiceBusClientBuilder processorBuilder = getAuthenticatedBuilder();
        processor = toClose(processorBuilder
            .clientOptions(clientOptions)
            .processor()
            .queueName(getQueueName(0))
            .processMessage(mc -> {
                if (mc.getMessage().getMessageId().equals(messageId)) {
                    receivedMessage.compareAndSet(null, mc.getMessage());
                    throw new RuntimeException("foo");
                }
            })
            .processError(e -> { })
            .buildProcessorClient());
        toClose((AutoCloseable) () -> processor.stop());
        processor.start();
        assertTrue(messageProcessed.await(30, TimeUnit.SECONDS));
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
        assertClientProducerSpan(abandoned.get(0), Collections.singletonList(message), "ServiceBus.abandon", "settle");
        assertSettledVsProcessed(abandoned, processed, 1);
    }

    @Test
    public void scheduleAndCancelMessage() {
        ServiceBusMessage message = new ServiceBusMessage("m");
        StepVerifier.create(
                sender.scheduleMessage(message, OffsetDateTime.now().plusSeconds(100))
                .flatMap(l -> sender.cancelScheduledMessage(l)))
            .expectComplete()
            .verify(TIMEOUT);

        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertMessageSpan(spans.get(0), message);
        assertClientProducerSpan(spans.get(1), Collections.singletonList(message), "ServiceBus.scheduleMessage", "publish");
        assertClientProducerSpan(spans.get(2), Collections.emptyList(), "ServiceBus.cancelScheduledMessage", null);
    }

    private void assertMessageSpan(ReadableSpan actual, ServiceBusMessage message) {
        assertEquals("ServiceBus.message", actual.getName());
        assertEquals(SpanKind.PRODUCER, actual.getKind());
        assertNull(actual.getAttribute(AttributeKey.stringKey("messaging.operation")));
        String traceparent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getSpanContext().getSpanId() + "-01";
        assertEquals(message.getApplicationProperties().get("Diagnostic-Id"), traceparent);
        assertEquals(message.getApplicationProperties().get("traceparent"), traceparent);
    }

    private void assertClientProducerSpan(ReadableSpan actual, List<ServiceBusMessage> messages, String spanName, String operationName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
        List<LinkData> links = actual.toSpanData().getLinks();
        assertEquals(messages.size(), links.size());
        assertEquals(operationName, actual.getAttribute(AttributeKey.stringKey("messaging.operation")));
        if (messages.size() > 1) {
            assertEquals(messages.size(), actual.getAttribute(AttributeKey.longKey("messaging.batch.message_count")));
        }

        for (int i = 0; i < links.size(); i++) {
            String messageTraceparent = (String) messages.get(i).getApplicationProperties().get("traceparent");
            SpanContext linkContext = links.get(i).getSpanContext();
            String linkTraceparent = "00-" + linkContext.getTraceId() + "-" + linkContext.getSpanId() + "-01";
            assertEquals(messageTraceparent, linkTraceparent);
        }
    }

    private void assertClientSpan(ReadableSpan actual, List<ServiceBusReceivedMessage> messages, String spanName, String operationName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CLIENT, actual.getKind());
        List<LinkData> links = actual.toSpanData().getLinks();
        assertEquals(messages.size(), links.size());
        assertEquals(operationName, actual.getAttribute(AttributeKey.stringKey("messaging.operation")));
        if (messages.size() > 1) {
            assertEquals(messages.size(), actual.getAttribute(AttributeKey.longKey("messaging.batch.message_count")));
        }
        for (int i = 0; i < links.size(); i++) {
            String messageTraceparent = (String) messages.get(i).getApplicationProperties().get("traceparent");
            SpanContext linkContext = links.get(i).getSpanContext();
            String linkTraceparent = "00-" + linkContext.getTraceId() + "-" + linkContext.getSpanId() + "-01";
            assertEquals(messageTraceparent, linkTraceparent);
            assertNotNull(links.get(i).getAttributes().get(AttributeKey.longKey(ServiceBusTracer.MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME)));
        }
    }

    private void assertConsumerSpan(ReadableSpan actual, ServiceBusReceivedMessage message, String spanName) {
        assertEquals(spanName, actual.getName());
        assertEquals(SpanKind.CONSUMER, actual.getKind());
        assertEquals(0, actual.toSpanData().getLinks().size());
        assertEquals("process", actual.getAttribute(AttributeKey.stringKey("messaging.operation")));
        String messageTraceparent = (String) message.getApplicationProperties().get("traceparent");
        String parent = "00-" + actual.getSpanContext().getTraceId() + "-" + actual.getParentSpanContext().getSpanId() + "-01";
        assertEquals(messageTraceparent, parent);
    }

    private void assertParent(ReadableSpan child, ReadableSpan parent) {
        assertEquals(child.getParentSpanContext().getTraceId(), parent.getSpanContext().getTraceId());
        assertEquals(child.getParentSpanContext().getSpanId(), parent.getSpanContext().getSpanId());
    }

    private void assertSettledVsProcessed(List<ReadableSpan> settled, List<ReadableSpan> processed, int expectedCount) {
        assertTrue(settled.size() >= expectedCount,
            String.format("Expected at least %d completed spans, but found %d", expectedCount, settled.size()));
        assertTrue(processed.size() >= expectedCount,
            String.format("Expected at least %d processed spans, but found %d", expectedCount, processed.size()));

        // there could be more completed spans than processed because completion happens before processing, and we can have more messages in the queue than we expected.
        assertTrue(settled.size() >= processed.size(),
            String.format("Expected at least as many completed spans as processed spans, but found %d completed and %d processed", settled.size(), processed.size()));

        int processedFound = 0;
        for (ReadableSpan readableSpan : settled) {
            ReadableSpan parent = findParent(readableSpan, processed);
            if (parent != null) {
                processedFound++;
                assertTrue(readableSpan.getLatencyNanos() <= parent.getLatencyNanos(), () ->
                    String.format("Expected settled span to have less latency than processed span, but found %d vs %d",
                        readableSpan.getLatencyNanos(), parent.getLatencyNanos()));
            }
        }

        assertEquals(expectedCount, processedFound);
    }

    private ReadableSpan findParent(ReadableSpan child, List<ReadableSpan> possibleParents) {
        String traceId = child.getParentSpanContext().getTraceId();
        String parentId = child.getParentSpanContext().getSpanId();
        for (ReadableSpan p : possibleParents) {
            if (traceId.equals(p.getSpanContext().getTraceId())
                    && parentId.equals(p.getSpanContext().getSpanId())) {
                return p;
            }
        }

        return null;
    }

    private List<ReadableSpan> findSpans(List<ReadableSpan> spans, String spanName) {
        return spans.stream()
            .filter(s -> s.getName().equals(spanName))
            .collect(Collectors.toList());
    }

    private List<ReadableSpan> findSpans(List<ReadableSpan> spans, String spanName, String traceId) {
        return spans.stream()
            .filter(s -> s.getName().equals(spanName))
            .filter(s -> s.getSpanContext().getTraceId().equals(traceId))
            .collect(Collectors.toList());
    }

    private OpenTelemetry configureOTel(String namespace, String entityName) {
        GlobalOpenTelemetry.resetForTest();
        spanProcessor = new TestSpanProcessor(namespace, entityName);
        return OpenTelemetrySdk.builder().setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .build()).build();
    }

    static class TestSpanProcessor implements SpanProcessor {
        private static final ClientLogger LOGGER = new ClientLogger(TestSpanProcessor.class);
        private static final AttributeKey<String> AZ_NAMESPACE = AttributeKey.stringKey("az.namespace");
        private static final AttributeKey<String> MESSAGING_SYSTEM = AttributeKey.stringKey("messaging.system");
        private static final AttributeKey<String> MESSAGING_DESTINATION_NAME
            = AttributeKey.stringKey("messaging.destination.name");
        private static final AttributeKey<String> NET_PEER_NAME = AttributeKey.stringKey("net.peer.name");
        private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");

        private final ConcurrentLinkedDeque<ReadableSpan> spans = new ConcurrentLinkedDeque<>();
        private final String entityName;
        private final String namespace;

        private final AtomicReference<Consumer<ReadableSpan>> notifier = new AtomicReference<>();

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
            LOGGER.info(readableSpan.toString());
            assertEquals("Microsoft.ServiceBus", readableSpan.getAttribute(AZ_NAMESPACE));
            assertEquals("servicebus", readableSpan.getAttribute(MESSAGING_SYSTEM));
            assertEquals(entityName, readableSpan.getAttribute(MESSAGING_DESTINATION_NAME));

            // Depending on the version of OpenTelemetry being used, the attribute name for the peer name may be
            // different. The attribute name was changed from "net.peer.name" to "server.address".
            String actualNamespace = readableSpan.getAttribute(NET_PEER_NAME);
            if (actualNamespace == null) {
                actualNamespace = readableSpan.getAttribute(SERVER_ADDRESS);
            }

            assertEquals(namespace, actualNamespace);

            spans.add(readableSpan);
            Consumer<ReadableSpan> filter = notifier.get();
            if (filter != null) {
                filter.accept(readableSpan);
            }
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
