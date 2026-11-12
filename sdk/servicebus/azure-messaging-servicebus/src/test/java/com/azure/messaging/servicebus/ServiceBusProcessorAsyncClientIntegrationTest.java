// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live AMQP coverage for reactive handler lifetime, renewal, settlement, and drain.
 */
@Isolated
public class ServiceBusProcessorAsyncClientIntegrationTest extends IntegrationTestBase {
    private final AtomicInteger renewalAttempts = new AtomicInteger();
    private final AtomicInteger successfulRenewals = new AtomicInteger();

    ServiceBusProcessorAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusProcessorAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    void manualSettlementAfterFlushRenewsAndDrains(MessagingEntityType entityType, boolean sessions) throws Exception {
        final String messageId = UUID.randomUUID().toString();
        final Sinks.Empty<Void> flush = Sinks.empty();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch settled = new CountDownLatch(1);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(sessions), sessions, false).buildAsyncClient());
        sender.sendMessage(getMessage(messageId, sessions).setMessageId(messageId)).block(TIMEOUT);

        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, sessions, context -> {
            assertEquals(messageId, context.getMessage().getMessageId());
            entered.countDown();
            return flush.asMono().then(context.complete()).doOnSuccess(ignored -> settled.countDown());
        }, context -> {
            error.compareAndSet(null, context.getException());
            return Mono.empty();
        }, true, ServiceBusReceiveMode.PEEK_LOCK));

        try {
            processor.start().block(TIMEOUT);
            assertTrue(entered.await(30, TimeUnit.SECONDS), "The handler did not start");
            assertFalse(settled.await(100, TimeUnit.MILLISECONDS), "Settlement preceded downstream flush");
            assertNotNull(peek(entityType, sessions), "The broker must retain the message before flush succeeds");
            // Fixture message/session locks last 15 seconds. A session message's locked-until annotation is not
            // its session lock expiry, so wait beyond the fixture duration rather than using that annotation.
            Mono.delay(Duration.ofSeconds(17)).block(TIMEOUT);
            final CompletableFuture<Void> closed = CompletableFuture.runAsync(processor::close);
            for (int attempt = 0; processor.isRunning() && attempt < 500; attempt++) {
                Thread.sleep(10);
            }
            assertFalse(processor.isRunning(), "Close did not enter the drain phase");
            assertFalse(settled.await(100, TimeUnit.MILLISECONDS), "Drain must not settle unfinished work");
            assertFalse(closed.isDone(), "Close returned before the handler finished");
            flush.tryEmitEmpty();
            assertTrue(settled.await(20, TimeUnit.SECONDS), "Settlement failed after renewal or during drain");
            closed.get(30, TimeUnit.SECONDS);
            assertNull(error.get(), "Unexpected processor error: " + error.get());
            assertTrue(successfulRenewals.get() > 0, "The delayed handler did not successfully renew its lock");
            assertEntityEmpty(entityType, sessions);
        } finally {
            flush.tryEmitEmpty();
            processor.close();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    void failedFlushIsAbandonedAndRedelivered(MessagingEntityType entityType, boolean sessions) throws Exception {
        final String messageId = UUID.randomUUID().toString();
        final CountDownLatch redelivered = new CountDownLatch(1);
        final AtomicInteger attempts = new AtomicInteger();
        final AtomicReference<ServiceBusErrorSource> source = new AtomicReference<>();
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(sessions), sessions, false).buildAsyncClient());
        sender.sendMessage(getMessage(messageId, sessions).setMessageId(messageId)).block(TIMEOUT);
        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, sessions, context -> {
            assertEquals(messageId, context.getMessage().getMessageId());
            if (attempts.incrementAndGet() == 1) {
                return Mono.error(new IllegalStateException("Downstream flush failed"));
            }
            assertTrue(context.getMessage().getDeliveryCount() > 0, "The failed message must be redelivered");
            redelivered.countDown();
            return Mono.empty();
        }, context -> {
            source.set(((ServiceBusException) context.getException()).getErrorSource());
            return Mono.empty();
        }, false, ServiceBusReceiveMode.PEEK_LOCK));
        processor.start().block(TIMEOUT);
        assertTrue(redelivered.await(30, TimeUnit.SECONDS), "The failed message was not abandoned for redelivery");
        processor.close();
        assertEquals(ServiceBusErrorSource.USER_CALLBACK, source.get());
        assertEntityEmpty(entityType, sessions);
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void failedManualHandlerStopsRenewalAndRedelivers(MessagingEntityType entityType) throws Exception {
        final String messageId = UUID.randomUUID().toString();
        final CountDownLatch failed = new CountDownLatch(1);
        final CountDownLatch redelivered = new CountDownLatch(1);
        final AtomicInteger attempts = new AtomicInteger();
        final AtomicReference<Throwable> processingError = new AtomicReference<>();
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(false), false, false).buildAsyncClient());
        sender.sendMessage(getMessage(messageId, false).setMessageId(messageId)).block(TIMEOUT);
        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, false, context -> {
            assertEquals(messageId, context.getMessage().getMessageId());
            if (attempts.incrementAndGet() == 1) {
                return Mono.delay(Duration.ofSeconds(17)).then(Mono.defer(() -> {
                    assertTrue(successfulRenewals.get() > 0, "The active handler must successfully renew its lock");
                    return Mono.error(new IllegalStateException("manual handler failed after renewal"));
                }));
            }
            assertTrue(context.getMessage().getDeliveryCount() > 0);
            return context.complete().doOnSuccess(ignored -> redelivered.countDown());
        }, context -> {
            processingError.compareAndSet(null, context.getException());
            failed.countDown();
            return Mono.empty();
        }, true, ServiceBusReceiveMode.PEEK_LOCK, Duration.ofMinutes(3)));
        try {
            processor.start().block(TIMEOUT);
            assertTrue(failed.await(45, TimeUnit.SECONDS), "The first manual handler did not fail");
            assertTrue(redelivered.await(30, TimeUnit.SECONDS),
                "Renewal after handler failure prevented redelivery while the processor remained open");
            assertTrue(processor.isRunning());
            assertEquals(2, attempts.get());
            assertTrue(processingError.get() instanceof ServiceBusException);
            final ServiceBusException error = (ServiceBusException) processingError.get();
            assertEquals(ServiceBusErrorSource.USER_CALLBACK, error.getErrorSource());
            assertEquals("manual handler failed after renewal", error.getCause().getMessage());
            assertEntityEmpty(entityType, false);
        } finally {
            processor.close();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    void receiveAndDeleteProcessesWithoutSettlement(MessagingEntityType entityType, boolean sessions) throws Exception {
        final String messageId = UUID.randomUUID().toString();
        final CountDownLatch received = new CountDownLatch(1);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(sessions), sessions, false).buildAsyncClient());
        sender.sendMessage(getMessage(messageId, sessions).setMessageId(messageId)).block(TIMEOUT);
        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, sessions, context -> {
            assertEquals(messageId, context.getMessage().getMessageId());
            return Mono.delay(Duration.ofMillis(100)).then(Mono.fromRunnable(received::countDown));
        }, context -> {
            error.set(context.getException());
            return Mono.empty();
        }, false, ServiceBusReceiveMode.RECEIVE_AND_DELETE));
        processor.start().block(TIMEOUT);
        assertTrue(received.await(30, TimeUnit.SECONDS), "The message was not processed");
        processor.close();
        assertNull(error.get(), "RECEIVE_AND_DELETE must not attempt settlement");
        assertEntityEmpty(entityType, sessions);
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    void stopResumesAfterAcceptedWorkSettles(MessagingEntityType entityType, boolean sessions) throws Exception {
        final String firstId = UUID.randomUUID().toString();
        final String secondId = UUID.randomUUID().toString();
        final Sinks.Empty<Void> flush = Sinks.empty();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch firstSettled = new CountDownLatch(1);
        final CountDownLatch secondSettled = new CountDownLatch(1);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(sessions), sessions, false).buildAsyncClient());
        sender.sendMessage(getMessage(firstId, sessions).setMessageId(firstId))
            .then(sender.sendMessage(getMessage(secondId, sessions).setMessageId(secondId)))
            .block(TIMEOUT);
        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, sessions, context -> {
            if (firstId.equals(context.getMessage().getMessageId())) {
                entered.countDown();
                return flush.asMono().then(context.complete()).doOnSuccess(ignored -> firstSettled.countDown());
            }
            assertEquals(secondId, context.getMessage().getMessageId());
            return context.complete().doOnSuccess(ignored -> secondSettled.countDown());
        }, context -> {
            error.compareAndSet(null, context.getException());
            return Mono.empty();
        }, true, ServiceBusReceiveMode.PEEK_LOCK));
        try {
            processor.start().block(TIMEOUT);
            assertTrue(entered.await(30, TimeUnit.SECONDS));
            processor.stop().block(TIMEOUT);
            flush.tryEmitEmpty();
            assertTrue(firstSettled.await(20, TimeUnit.SECONDS), "Stop cancelled accepted work or its receiver");
            assertFalse(secondSettled.await(200, TimeUnit.MILLISECONDS),
                "Stopped processor dispatched another message");
            processor.start().block(TIMEOUT);
            assertTrue(secondSettled.await(30, TimeUnit.SECONDS), "Resume did not receive the second message");
            processor.close();
            assertNull(error.get(), "Unexpected lifecycle error: " + error.get());
            assertEntityEmpty(entityType, sessions);
        } finally {
            flush.tryEmitEmpty();
            processor.close();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    void disabledMessageRenewalCannotSettleAfterTheFixtureLockExpires(MessagingEntityType entityType) throws Exception {
        final boolean sessions = false;
        final String messageId = UUID.randomUUID().toString();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch failed = new CountDownLatch(1);
        final Sinks.Empty<Void> flush = Sinks.empty();
        final AtomicInteger attempts = new AtomicInteger();
        final AtomicReference<Throwable> settlementFailure = new AtomicReference<>();
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(sessions), sessions, false).buildAsyncClient());
        sender.sendMessage(getMessage(messageId, sessions).setMessageId(messageId)).block(TIMEOUT);
        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, sessions, context -> {
            assertEquals(messageId, context.getMessage().getMessageId());
            if (attempts.incrementAndGet() != 1) {
                return Mono.empty();
            }
            entered.countDown();
            return flush.asMono().then(context.complete()).doOnError(error -> {
                settlementFailure.set(error);
                failed.countDown();
            });
        }, context -> Mono.empty(), true, ServiceBusReceiveMode.PEEK_LOCK, Duration.ZERO));
        try {
            processor.start().block(TIMEOUT);
            assertTrue(entered.await(30, TimeUnit.SECONDS));
            Mono.delay(Duration.ofSeconds(17)).block(TIMEOUT);
            flush.tryEmitEmpty();
            assertTrue(failed.await(20, TimeUnit.SECONDS), "Settlement succeeded after expiry without renewal");
            assertNotNull(settlementFailure.get());
            processor.close();
            assertEquals(0, renewalAttempts.get(), "Disabled message renewal still attempted renewal");
            assertNotNull(peek(entityType, sessions), "Failed settlement removed the message");
            final CountDownLatch cleaned = new CountDownLatch(1);
            final AtomicReference<Throwable> cleanupError = new AtomicReference<>();
            final ServiceBusProcessorAsyncClient cleanup = toClose(processor(entityType, sessions,
                context -> context.complete().doOnSuccess(ignored -> cleaned.countDown()), context -> {
                    cleanupError.compareAndSet(null, context.getException());
                    return Mono.empty();
                }, true, ServiceBusReceiveMode.PEEK_LOCK));
            cleanup.start().block(TIMEOUT);
            assertTrue(cleaned.await(30, TimeUnit.SECONDS), "The retained message could not be consumed");
            cleanup.close();
            assertNull(cleanupError.get());
            assertEntityEmpty(entityType, sessions);
        } finally {
            flush.tryEmitEmpty();
            processor.close();
        }
    }

    @ParameterizedTest
    @MethodSource("sessionEntities")
    void disabledSessionRenewalDoesNotRenewOrSettleUnfinishedWork(MessagingEntityType entityType) throws Exception {
        final String messageId = UUID.randomUUID().toString();
        final CountDownLatch entered = new CountDownLatch(1);
        final Sinks.Empty<Void> flush = Sinks.empty();
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(true), true, false).buildAsyncClient());
        sender.sendMessage(getMessage(messageId, true).setMessageId(messageId)).block(TIMEOUT);
        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, true, context -> {
            assertEquals(messageId, context.getMessage().getMessageId());
            entered.countDown();
            return flush.asMono();
        }, context -> Mono.empty(), true, ServiceBusReceiveMode.PEEK_LOCK, Duration.ZERO));
        try {
            processor.start().block(TIMEOUT);
            assertTrue(entered.await(30, TimeUnit.SECONDS));
            // Session activity can retain the broker lock without an explicit renewal request.
            // Check the configured client behavior, not an assumed wall-clock session expiry.
            Mono.delay(Duration.ofSeconds(17)).block(TIMEOUT);
            assertEquals(0, renewalAttempts.get(), "Disabled session renewal still attempted renewal");
            assertNotNull(peek(entityType, true), "Unfinished work was settled");
            flush.tryEmitEmpty();
            processor.close();
            assertEquals(0, renewalAttempts.get());
            assertNotNull(peek(entityType, true), "Manual processing settled a message without a settlement call");

            final CountDownLatch cleaned = new CountDownLatch(1);
            final AtomicReference<Throwable> cleanupError = new AtomicReference<>();
            final ServiceBusProcessorAsyncClient cleanup = toClose(processor(entityType, true,
                context -> context.complete().doOnSuccess(ignored -> cleaned.countDown()), context -> {
                    cleanupError.compareAndSet(null, context.getException());
                    return Mono.empty();
                }, true, ServiceBusReceiveMode.PEEK_LOCK));
            cleanup.start().block(TIMEOUT);
            assertTrue(cleaned.await(30, TimeUnit.SECONDS), "The retained message could not be consumed");
            cleanup.close();
            assertNull(cleanupError.get());
            assertEntityEmpty(entityType, true);
        } finally {
            flush.tryEmitEmpty();
            processor.close();
        }
    }

    @ParameterizedTest
    @MethodSource("sessionEntities")
    void sessionsRollOverAfterPendingWorkFinishes(MessagingEntityType entityType) throws Exception {
        final ServiceBusSenderAsyncClient sender
            = toClose(getSenderBuilder(entityType, entityIndex(true), true, false).buildAsyncClient());
        final String firstSession = UUID.randomUUID().toString();
        final String secondSession = UUID.randomUUID().toString();
        for (String id : new String[] { firstSession, secondSession }) {
            for (int message = 0; message < 2; message++) {
                sender
                    .sendMessage(
                        new ServiceBusMessage("rollover").setSessionId(id).setMessageId(UUID.randomUUID().toString()))
                    .block(TIMEOUT);
            }
        }
        final List<String> observed = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch processed = new CountDownLatch(4);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final ServiceBusProcessorAsyncClient processor = toClose(processor(entityType, true, context -> {
            observed.add(context.getMessage().getSessionId());
            return context.complete().doOnSuccess(ignored -> processed.countDown());
        }, context -> {
            error.compareAndSet(null, context.getException());
            return Mono.empty();
        }, true, ServiceBusReceiveMode.PEEK_LOCK));
        processor.start().block(TIMEOUT);
        assertTrue(processed.await(30, TimeUnit.SECONDS), "The single session slot did not roll to the next session");
        processor.close();
        assertNull(error.get());
        assertEquals(4, observed.size());
        assertEquals(observed.get(0), observed.get(1));
        assertEquals(observed.get(2), observed.get(3));
        assertFalse(observed.get(0).equals(observed.get(2)));
        assertEntityEmpty(entityType, true);
    }

    private static Stream<MessagingEntityType> sessionEntities() {
        return Stream.of(MessagingEntityType.QUEUE, MessagingEntityType.SUBSCRIPTION);
    }

    private ServiceBusProcessorAsyncClient processor(MessagingEntityType entityType, boolean sessions,
        Function<ServiceBusReceivedMessageAsyncContext, Mono<Void>> handler,
        Function<ServiceBusErrorContext, Mono<Void>> errors, boolean manual, ServiceBusReceiveMode receiveMode) {
        return processor(entityType, sessions, handler, errors, manual, receiveMode, Duration.ofMinutes(1));
    }

    private ServiceBusProcessorAsyncClient processor(MessagingEntityType entityType, boolean sessions,
        Function<ServiceBusReceivedMessageAsyncContext, Mono<Void>> handler,
        Function<ServiceBusErrorContext, Mono<Void>> errors, boolean manual, ServiceBusReceiveMode receiveMode,
        Duration maxAutoLockRenewDuration) {
        final OpenTelemetrySdk telemetry = toClose(OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(new SpanProcessor() {
                @Override
                public void onStart(Context parent, ReadWriteSpan span) {
                    if ("ServiceBus.renewSessionLock".equals(span.getName())
                        || "ServiceBus.renewMessageLock".equals(span.getName())) {
                        renewalAttempts.incrementAndGet();
                    }
                }

                @Override
                public boolean isStartRequired() {
                    return true;
                }

                @Override
                public void onEnd(ReadableSpan span) {
                    if (("ServiceBus.renewSessionLock".equals(span.getName())
                        || "ServiceBus.renewMessageLock".equals(span.getName()))
                        && span.toSpanData().getStatus().getStatusCode() != StatusCode.ERROR) {
                        successfulRenewals.incrementAndGet();
                    }
                }

                @Override
                public boolean isEndRequired() {
                    return true;
                }
            }).build())
            .build());
        final ServiceBusClientBuilder builder = getBuilder().clientOptions(
            new ClientOptions().setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(telemetry)));
        final int index = entityIndex(sessions);
        if (sessions) {
            // Session acquisition includes CLI authentication, which can exceed the shared three-second test timeout.
            final ServiceBusClientBuilder.ServiceBusSessionProcessorAsyncClientBuilder asyncBuilder
                = builder.retryOptions(new AmqpRetryOptions(RETRY_OPTIONS).setTryTimeout(OPERATION_TIMEOUT))
                    .sessionProcessorAsync()
                    .processMessage(handler)
                    .processError(errors)
                    .maxAutoLockRenewDuration(maxAutoLockRenewDuration)
                    .sessionIdleTimeout(Duration.ofSeconds(2))
                    .receiveMode(receiveMode)
                    .drainTimeout(Duration.ofSeconds(30));
            if (manual) {
                asyncBuilder.disableAutoComplete();
            }
            if (entityType == MessagingEntityType.QUEUE) {
                asyncBuilder.queueName(getSessionQueueName(index));
            } else {
                asyncBuilder.topicName(getTopicName(index))
                    .subscriptionName(TestUtils.getSessionSubscriptionBaseName());
            }
            return asyncBuilder.buildProcessorAsyncClient();
        }
        final ServiceBusClientBuilder.ServiceBusProcessorAsyncClientBuilder asyncBuilder = builder.processorAsync()
            .processMessage(handler)
            .processError(errors)
            .maxAutoLockRenewDuration(maxAutoLockRenewDuration)
            .receiveMode(receiveMode)
            .drainTimeout(Duration.ofSeconds(30));
        if (manual) {
            asyncBuilder.disableAutoComplete();
        }
        if (entityType == MessagingEntityType.QUEUE) {
            asyncBuilder.queueName(getQueueName(index));
        } else {
            asyncBuilder.topicName(getTopicName(index)).subscriptionName(TestUtils.getSubscriptionBaseName());
        }
        return asyncBuilder.buildProcessorAsyncClient();
    }

    private void assertEntityEmpty(MessagingEntityType entityType, boolean sessions) {
        assertNull(peek(entityType, sessions), "The entity still has a message");
    }

    private int entityIndex(boolean sessions) {
        return sessions ? TestUtils.USE_CASE_ASYNC_SESSION_PROCESSOR : TestUtils.USE_CASE_ASYNC_PROCESSOR;
    }

    private ServiceBusReceivedMessage peek(MessagingEntityType entityType, boolean sessions) {
        // Peek ignores delivery locks. A fresh receiver avoids reusing an advanced peek cursor.
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiver = getBuilder().receiver();
        final int index = entityIndex(sessions);
        if (entityType == MessagingEntityType.QUEUE) {
            receiver.queueName(sessions ? getSessionQueueName(index) : getQueueName(index));
        } else {
            receiver.topicName(getTopicName(index))
                .subscriptionName(
                    sessions ? TestUtils.getSessionSubscriptionBaseName() : TestUtils.getSubscriptionBaseName());
        }
        return toClose(receiver.buildAsyncClient()).peekMessage().block(TIMEOUT);
    }
}
