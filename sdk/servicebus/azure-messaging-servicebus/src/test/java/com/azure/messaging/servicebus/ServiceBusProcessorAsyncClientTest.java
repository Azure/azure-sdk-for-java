// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.core.credential.TokenCredential;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServiceBusProcessorAsyncClient}.
 */
public class ServiceBusProcessorAsyncClientTest {

    private static final String NAMESPACE = "namespace";
    private static final String ENTITY_NAME = "entity";

    private ServiceBusReceiverAsyncClient mockReceiver;
    private ServiceBusSessionReceiverAsyncClient mockSessionClient;

    /**
     * Messages flow to the async handler and, by default, each is completed after the handler {@link Mono} finishes.
     */
    @Test
    public void receivesMessagesAndAutoCompletes() throws InterruptedException {
        final Flux<ServiceBusMessageContext> messages = messageContexts(5);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);
        final CountDownLatch latch = new CountDownLatch(5);
        final AtomicInteger nextId = new AtomicInteger();
        final AtomicReference<Throwable> unexpectedError = new AtomicReference<>();

        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                assertEquals(String.valueOf(nextId.getAndIncrement()), context.getMessage().getMessageId());
                latch.countDown();
                return Mono.empty();
            }, error -> {
                unexpectedError.set(error.getException());
                return Mono.empty();
            }, options(1, false));

        processor.start().block();
        final boolean success = latch.await(5, TimeUnit.SECONDS);
        processor.close();

        assertTrue(success, "Failed to receive all expected messages");
        assertNull(unexpectedError.get(), "Unexpected error-handler invocation: " + unexpectedError.get());
        verify(mockReceiver, times(5)).complete(any());
        verify(mockReceiver, never()).abandon(any());
    }

    /**
     * Session messages flow to the async handler through the session receiver builder.
     */
    @Test
    public void sessionMessagesAreDispatched() throws InterruptedException {
        final Flux<ServiceBusMessageContext> messages = messageContexts(6);
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder = sessionBuilder(messages);
        final CountDownLatch latch = new CountDownLatch(6);

        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                latch.countDown();
                return Mono.empty();
            }, error -> Mono.empty(), options(1, false));

        processor.start().block();
        final boolean success = latch.await(5, TimeUnit.SECONDS);

        assertTrue(success, "Failed to receive all expected session messages");
        // Settlement parity with the non-session path: each dispatched session message is auto-completed.
        verify(mockReceiver, timeout(5000).times(6)).complete(any());
        processor.close();
    }

    /**
     * When the handler {@link Mono} signals an error and auto-complete is enabled, the error handler is invoked and the
     * message is abandoned (not completed).
     */
    @Test
    public void handlerErrorInvokesErrorHandlerAndAbandons() throws InterruptedException {
        final Flux<ServiceBusMessageContext> messages = messageContexts(1);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);
        final CountDownLatch errorLatch = new CountDownLatch(1);
        final AtomicReference<ServiceBusErrorSource> source = new AtomicReference<>();

        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.error(new IllegalStateException("boom")), error -> {
                if (error.getException() instanceof ServiceBusException) {
                    source.set(((ServiceBusException) error.getException()).getErrorSource());
                }
                errorLatch.countDown();
                return Mono.empty();
            }, options(1, false));

        processor.start().block();
        final boolean errored = errorLatch.await(5, TimeUnit.SECONDS);
        processor.close();

        assertTrue(errored, "Error handler was not invoked");
        assertEquals(ServiceBusErrorSource.USER_CALLBACK, source.get());
        verify(mockReceiver, times(1)).abandon(any());
        verify(mockReceiver, never()).complete(any());
    }

    /**
     * When auto-complete is disabled, the processor settles nothing itself - neither complete nor abandon is called.
     */
    @Test
    public void disableAutoCompleteSkipsSettlement() throws InterruptedException {
        final Flux<ServiceBusMessageContext> messages = messageContexts(3);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);
        final CountDownLatch latch = new CountDownLatch(3);

        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                latch.countDown();
                return Mono.empty();
            }, error -> Mono.empty(), options(1, true));

        processor.start().block();
        final boolean success = latch.await(5, TimeUnit.SECONDS);
        processor.close();

        assertTrue(success, "Failed to receive all expected messages");
        verify(mockReceiver, never()).complete(any());
        verify(mockReceiver, never()).abandon(any());
    }

    /**
     * A terminal error on the receive stream is surfaced to the error handler.
     */
    @Test
    public void receiverErrorInvokesErrorHandler() throws InterruptedException {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        when(mockReceiver.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        when(mockReceiver.getEntityPath()).thenReturn(ENTITY_NAME);
        when(mockReceiver.isConnectionClosed()).thenReturn(false);
        doNothing().when(mockReceiver).close();
        // First subscription errors; subsequent restart subscribes to a stream that never emits, bounding the retry.
        when(mockReceiver.receiveMessagesWithContextForAsyncProcessor())
            .thenReturn(Flux.error(new IllegalStateException("receive failed")))
            .thenReturn(Flux.never());

        final CountDownLatch errorLatch = new CountDownLatch(1);
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> Mono.empty(), error -> {
                errorLatch.countDown();
                return Mono.empty();
            }, options(1, false));

        processor.start().block();
        final boolean errored = errorLatch.await(5, TimeUnit.SECONDS);

        assertTrue(errored, "Receive error was not surfaced to the error handler");
        // The terminal receive error triggers a restart - a second receiver is built.
        verify(builder, timeout(15000).times(2)).buildAsyncClientForProcessor();
        processor.close();
        // close() must not trigger a further restart - the count stays at two.
        verify(builder, times(2)).buildAsyncClientForProcessor();
    }

    @Test
    public void persistentReceiveFailureWaitsForMonitor() {
        final AtomicInteger subscriptions = new AtomicInteger();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.defer(() -> {
            if (subscriptions.incrementAndGet() <= 5) {
                return Flux.error(new AmqpException(false, "The entity does not exist.", null));
            }
            return Flux.never();
        }));
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));
        try {
            processor.start().block();
            verify(builder, after(250).times(1)).buildAsyncClientForProcessor();
            verify(builder, timeout(12000).times(2)).buildAsyncClientForProcessor();
            processor.stop().block();
            verify(builder, after(250).times(2)).buildAsyncClientForProcessor();
        } finally {
            processor.close();
        }
    }

    @Test
    public void stoppedTerminatedReceiverRestartsOffNonBlockingThread() {
        final AtomicReference<FluxSink<ServiceBusMessageContext>> intake = new AtomicReference<>();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = nonSessionBuilder(Flux.create(intake::set));
        doAnswer(invocation -> {
            assertFalse(Schedulers.isInNonBlockingThread(), "Receiver cleanup must allow blocking");
            return null;
        }).when(mockReceiver).close();
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));
        try {
            processor.start().block();
            processor.stop().block();
            intake.get().error(new IllegalStateException("receive failed while stopped"));
            when(mockReceiver.receiveMessagesWithContextForAsyncProcessor()).thenReturn(Flux.never());
            processor.start().subscribeOn(Schedulers.parallel()).block(Duration.ofSeconds(5));
            assertTrue(processor.isRunning());
            verify(builder, times(2)).buildAsyncClientForProcessor();
            verify(mockReceiver).close();
        } finally {
            processor.close();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void stopSubscriptionDoesNotBlockDuringRecoveryCleanup(boolean recovering) throws Exception {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        final CountDownLatch cleanupEntered = new CountDownLatch(1);
        final CountDownLatch releaseCleanup = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (recovering && cleanupEntered.getCount() > 0) {
                cleanupEntered.countDown();
                if (!releaseCleanup.await(30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Recovery cleanup was not released");
                }
            }
            return null;
        }).when(mockReceiver).close();
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));
        final Scheduler scheduler = Schedulers.newSingle("processor-stop-nonblocking");
        final CountDownLatch subscribed = new CountDownLatch(1);
        final CountDownLatch stopped = new CountDownLatch(1);
        final AtomicBoolean nonBlocking = new AtomicBoolean();
        final AtomicReference<Throwable> stopError = new AtomicReference<>();
        try {
            processor.start().block(Duration.ofSeconds(5));
            final Mono<Void> stop = processor.stop();
            assertTrue(processor.isRunning(), "stop() must remain cold");
            final java.lang.reflect.Field field
                = ServiceBusProcessorAsyncClient.class.getDeclaredField("receiveDisposable");
            field.setAccessible(true);
            final Disposable cycle = (Disposable) ((AtomicReference<?>) field.get(processor)).get();
            final java.lang.reflect.Method restart
                = ServiceBusProcessorAsyncClient.class.getDeclaredMethod("restartMessageReceiver", Disposable.class);
            restart.setAccessible(true);
            final FutureTask<Void> recovery = new FutureTask<>(() -> {
                restart.invoke(processor, cycle);
                return null;
            });
            if (recovering) {
                new Thread(recovery, "processor-stop-recovery").start();
                assertTrue(cleanupEntered.await(5, TimeUnit.SECONDS));
            }

            scheduler.schedule(() -> {
                try {
                    nonBlocking.set(Schedulers.isInNonBlockingThread());
                    stop.subscribe(ignored -> {
                    }, stopError::set, stopped::countDown);
                } finally {
                    subscribed.countDown();
                }
            });
            assertTrue(subscribed.await(2, TimeUnit.SECONDS),
                "Subscribing to stop() blocked a Reactor non-blocking thread");
            assertTrue(nonBlocking.get());
            if (recovering) {
                assertEquals(1, stopped.getCount(), "Stop must still wait for recovery to release the monitor");
            }
            releaseCleanup.countDown();
            if (recovering) {
                recovery.get(5, TimeUnit.SECONDS);
            }
            assertTrue(stopped.await(5, TimeUnit.SECONDS));
            assertNull(stopError.get());
            assertFalse(processor.isRunning());
        } finally {
            releaseCleanup.countDown();
            scheduler.dispose();
            processor.close();
        }
    }

    /**
     * {@code start()} is idempotent and {@code isRunning()} reflects the lifecycle state.
     */
    @Test
    public void startIsIdempotentAndIsRunningReflectsState() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());

        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));

        assertFalse(processor.isRunning());
        processor.start().block();
        assertTrue(processor.isRunning());
        // Second start is a no-op - the receiver is built only once.
        processor.start().block();
        assertTrue(processor.isRunning());
        verify(builder, times(1)).buildAsyncClientForProcessor();

        processor.stop().block();
        assertFalse(processor.isRunning());
        processor.close();
    }

    /**
     * Closing a processor that was never started is a no-op and does not throw.
     */
    @Test
    public void closeWithoutStartIsNoOp() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));

        processor.close();
        assertFalse(processor.isRunning());
    }

    /**
     * Concurrency is bounded by {@code maxConcurrentCalls} - no more than that many handlers run at once.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void concurrencyBoundedByMaxConcurrentCalls(boolean sessions) throws InterruptedException {
        final int maxConcurrentCalls = 2;
        final int messageCount = 8;
        final Flux<ServiceBusMessageContext> messages = messageContexts(messageCount);

        final CountDownLatch latch = new CountDownLatch(messageCount);
        final AtomicInteger inFlight = new AtomicInteger();
        final AtomicInteger maxObserved = new AtomicInteger();

        final Function<ServiceBusReceivedMessageAsyncContext, Mono<Void>> handler = context -> Mono.fromRunnable(() -> {
            final int current = inFlight.incrementAndGet();
            maxObserved.accumulateAndGet(current, Math::max);
        }).then(Mono.delay(Duration.ofMillis(40))).then(Mono.fromRunnable(() -> {
            inFlight.decrementAndGet();
            latch.countDown();
        }));
        final ServiceBusProcessorAsyncClient processor = sessions
            ? new ServiceBusProcessorAsyncClient(sessionBuilder(messages), ENTITY_NAME, null, null, handler,
                error -> Mono.empty(), options(maxConcurrentCalls, false))
            : new ServiceBusProcessorAsyncClient(nonSessionBuilder(messages), ENTITY_NAME, null, null, handler,
                error -> Mono.empty(), options(maxConcurrentCalls, false));

        processor.start().block();
        final boolean success = latch.await(10, TimeUnit.SECONDS);
        processor.close();

        assertTrue(success, "Failed to process all messages");
        assertTrue(maxObserved.get() <= maxConcurrentCalls,
            "Observed " + maxObserved.get() + " concurrent handlers, expected at most " + maxConcurrentCalls);
        // Lower bound: the pump must actually reach the concurrency limit, otherwise a regression to serial dispatch
        // (concurrency 1) would still satisfy the upper bound and pass silently.
        assertTrue(maxObserved.get() >= maxConcurrentCalls, "Observed max " + maxObserved.get()
            + " concurrent handlers, expected the bound " + maxConcurrentCalls + " to be reached");
    }

    /**
     * {@code stop()} followed by {@code start()} resumes processing on the same receiver (no new receiver is built).
     */
    @Test
    public void stopThenStartResumesWithSameReceiver() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));

        processor.start().block();
        processor.stop().block();
        assertFalse(processor.isRunning());
        processor.start().block();
        assertTrue(processor.isRunning());
        // The receiver is reused across stop/start - it is built exactly once.
        verify(builder, times(1)).buildAsyncClientForProcessor();
        processor.close();
    }

    /**
     * {@code close()} blocks until an in-flight handler finishes settling against a still-open receiver.
     */
    @Test
    public void closeDrainsInFlightHandlerBeforeClosingReceiver() throws InterruptedException {
        final Flux<ServiceBusMessageContext> messages = messageContexts(1);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);
        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch allowFinish = new CountDownLatch(1);

        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.fromRunnable(handlerStarted::countDown).then(Mono.fromRunnable(() -> {
                try {
                    allowFinish.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).subscribeOn(Schedulers.boundedElastic())).then(), error -> Mono.empty(), options(1, false));

        processor.start().block();
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler never started");

        final AtomicBoolean closeReturned = new AtomicBoolean();
        final Thread closeThread = new Thread(() -> {
            processor.close();
            closeReturned.set(true);
        });
        closeThread.setDaemon(true);
        closeThread.start();

        // close() must still be draining while the handler is held.
        Thread.sleep(300);
        assertFalse(closeReturned.get(), "close() returned before the in-flight handler finished");

        allowFinish.countDown();
        closeThread.join(TimeUnit.SECONDS.toMillis(5));
        assertTrue(closeReturned.get(), "close() did not return after the handler finished");
        // The message was settled against the still-open receiver during the drain.
        verify(mockReceiver, times(1)).complete(any());
    }

    /**
     * A receive stream that completes triggers exactly one restart (not an unbounded loop).
     */
    @Test
    public void completingStreamTriggersExactlyOneRestart() throws InterruptedException {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        stubReceiverBasics();
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("hi"));
        message.setMessageId("0");
        // First stream emits one message then completes (triggers a restart); the second stream stays open.
        when(mockReceiver.receiveMessagesWithContextForAsyncProcessor())
            .thenReturn(Flux.just(new ServiceBusMessageContext(message)))
            .thenReturn(Flux.never());

        final CountDownLatch processed = new CountDownLatch(1);
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                processed.countDown();
                return Mono.empty();
            }, error -> Mono.empty(), options(1, false));

        processor.start().block();
        assertTrue(processed.await(5, TimeUnit.SECONDS), "Message was not processed");
        // Exactly one restart: the original receiver plus one rebuild. The second (open) stream does not restart.
        verify(builder, timeout(15000).times(2)).buildAsyncClientForProcessor();
        processor.close();
        verify(builder, times(2)).buildAsyncClientForProcessor();
    }

    /**
     * A failure to abandon a message (after a handler error) is swallowed and does not break the pump: a subsequent
     * message is still processed, and no spurious restart is triggered.
     */
    @Test
    public void abandonFailureIsSwallowed() throws InterruptedException {
        final AtomicReference<FluxSink<ServiceBusMessageContext>> sinkRef = new AtomicReference<>();
        final Flux<ServiceBusMessageContext> messages = Flux.create(sinkRef::set);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);
        when(mockReceiver.abandon(any())).thenReturn(Mono.error(new IllegalStateException("abandon failed")));
        final CountDownLatch errorLatch = new CountDownLatch(1);
        final CountDownLatch secondProcessed = new CountDownLatch(1);

        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                if ("boom".equals(context.getMessage().getMessageId())) {
                    return Mono.error(new IllegalStateException("boom"));
                }
                return Mono.fromRunnable(secondProcessed::countDown);
            }, error -> {
                errorLatch.countDown();
                return Mono.empty();
            }, options(1, false));

        processor.start().block();
        sinkRef.get().next(messageContext("boom"));
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS), "Error handler was not invoked");
        // The abandon failure must be swallowed - the pump survives, so a subsequent message is still processed...
        sinkRef.get().next(messageContext("ok"));
        assertTrue(secondProcessed.await(5, TimeUnit.SECONDS), "Pump did not survive the abandon failure");
        processor.close();

        verify(mockReceiver, times(1)).abandon(any());
        // ...and no spurious restart was triggered by the swallowed error (only the initial receiver was built).
        verify(builder, times(1)).buildAsyncClientForProcessor();
    }

    /**
     * The constructor rejects a {@code maxConcurrentCalls} value below 1 (fail-fast defense in depth beyond the
     * builder validation).
     */
    @Test
    public void constructorRejectsInvalidMaxConcurrentCalls() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        assertThrows(IllegalArgumentException.class, () -> new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME,
            null, null, context -> Mono.empty(), error -> Mono.empty(), options(0, false)));
    }

    /**
     * A completion failure (after the handler succeeds) is reported to the error handler with its OWN error source
     * ({@code COMPLETE}, not {@code USER_CALLBACK}) and does NOT trigger an abandon - the handler did its job.
     */
    @Test
    public void completeFailureIsReportedAndNotAbandoned() throws InterruptedException {
        final AtomicReference<FluxSink<ServiceBusMessageContext>> sinkRef = new AtomicReference<>();
        final Flux<ServiceBusMessageContext> messages = Flux.create(sinkRef::set);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);
        final ServiceBusException completeError
            = new ServiceBusException(new IllegalStateException("complete failed"), ServiceBusErrorSource.COMPLETE);
        when(mockReceiver.complete(any())).thenReturn(Mono.error(completeError));
        final CountDownLatch errorLatch = new CountDownLatch(1);
        final AtomicReference<ServiceBusErrorSource> source = new AtomicReference<>();

        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> Mono.empty(), error -> {
                source.set(error.getErrorSource());
                errorLatch.countDown();
                return Mono.empty();
            }, options(1, false));

        processor.start().block();
        sinkRef.get().next(messageContext("m1"));
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS), "Completion failure was not surfaced to the error handler");
        processor.close();

        // The completion failure keeps its COMPLETE source (not misclassified as USER_CALLBACK), and the message is
        // NOT abandoned - the handler succeeded.
        assertEquals(ServiceBusErrorSource.COMPLETE, source.get(), "error source should be COMPLETE");
        verify(mockReceiver, times(1)).complete(any());
        verify(mockReceiver, never()).abandon(any());
    }

    /**
     * {@code getIdentifier()} returns {@code null} before the first {@code start()} and delegates to the underlying
     * receiver afterwards.
     */
    @Test
    public void getIdentifierNullBeforeStartThenDelegates() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        when(mockReceiver.getIdentifier()).thenReturn("receiver-id-123");
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));

        assertNull(processor.getIdentifier(), "identifier must be null before start()");
        processor.start().block();
        assertEquals("receiver-id-123", processor.getIdentifier(), "identifier must delegate to the receiver");
        processor.close();
    }

    /**
     * An in-band receive error context (a message context carrying a throwable) is surfaced to the error handler and is
     * not settled.
     */
    @Test
    public void inBandErrorContextInvokesErrorHandler() throws InterruptedException {
        final ServiceBusMessageContext errorContext
            = new ServiceBusMessageContext("session", new IllegalStateException("receive error"));
        final Flux<ServiceBusMessageContext> messages = Flux.just(errorContext).concatWith(Flux.never());
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);
        final CountDownLatch errorLatch = new CountDownLatch(1);
        final AtomicBoolean handlerRan = new AtomicBoolean();

        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                handlerRan.set(true);
                return Mono.empty();
            }, error -> {
                errorLatch.countDown();
                return Mono.empty();
            }, options(1, false));

        processor.start().block();
        final boolean errored = errorLatch.await(5, TimeUnit.SECONDS);
        processor.close();

        assertTrue(errored, "Error context was not surfaced to the error handler");
        assertFalse(handlerRan.get(), "Message handler should not run for an in-band error context");
        verify(mockReceiver, never()).complete(any());
        verify(mockReceiver, never()).abandon(any());
    }

    /**
     * Closing stops PEEK_LOCK dispatch while retaining the receiver subscription for accepted handlers to settle.
     */
    @Test
    public void stopsReceivingPeekLockMessagesBeforeDraining() throws InterruptedException {
        final CountDownLatch intakeStopped = new CountDownLatch(1);
        final AtomicReference<FluxSink<ServiceBusMessageContext>> sinkRef = new AtomicReference<>();
        final Flux<ServiceBusMessageContext> messages = Flux.create(sink -> {
            sinkRef.set(sink);
            sink.onCancel(intakeStopped::countDown);
        });
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        stubReceiverBasics();
        when(mockReceiver.receiveMessagesWithContextForAsyncProcessor()).thenReturn(messages);
        final ReceiverOptions receiverOptions = mock(ReceiverOptions.class);
        when(receiverOptions.getReceiveMode()).thenReturn(ServiceBusReceiveMode.PEEK_LOCK);
        when(mockReceiver.getReceiverOptions()).thenReturn(receiverOptions);

        final CountDownLatch slowStarted = new CountDownLatch(1);
        final CountDownLatch allowFinish = new CountDownLatch(1);
        final Set<String> processedIds = Collections.synchronizedSet(new HashSet<>());
        // Spare handler slots ensure cancellation, rather than backpressure, prevents further dispatch.
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                processedIds.add(context.getMessage().getMessageId());
                if ("slow".equals(context.getMessage().getMessageId())) {
                    return Mono.fromRunnable(slowStarted::countDown).then(Mono.fromRunnable(() -> {
                        try {
                            allowFinish.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).subscribeOn(Schedulers.boundedElastic())).then();
                }
                return Mono.empty();
            }, error -> Mono.empty(), options(3, false));

        processor.start().block();
        sinkRef.get().next(messageContext("slow"));
        assertTrue(slowStarted.await(5, TimeUnit.SECONDS), "Slow handler never started");

        final AtomicBoolean closeReturned = new AtomicBoolean();
        final Thread closeThread = new Thread(() -> {
            processor.close();
            closeReturned.set(true);
        });
        closeThread.setDaemon(true);
        closeThread.start();
        assertFalse(intakeStopped.await(300, TimeUnit.MILLISECONDS),
            "The receiver subscription must stay open for settlement while draining");
        assertFalse(processor.isRunning(), "The processor must stop dispatching before draining");
        assertFalse(closeReturned.get(), "The accepted handler must still be draining");

        sinkRef.get().next(messageContext("fast1"));
        sinkRef.get().next(messageContext("fast2"));

        allowFinish.countDown();
        closeThread.join(TimeUnit.SECONDS.toMillis(5));
        assertTrue(closeReturned.get(), "close() did not return");
        verify(mockReceiver, times(1)).complete(any());
        assertTrue(processedIds.contains("slow"), "Slow handler should have run");
        assertTrue(intakeStopped.await(5, TimeUnit.SECONDS), "The receiver subscription must close after draining");
        assertFalse(processedIds.contains("fast1"), "fast1 must not be accepted during drain");
        assertFalse(processedIds.contains("fast2"), "fast2 must not be accepted during drain");
    }

    /**
     * RECEIVE_AND_DELETE also stops intake before draining the accepted handler, without attempting settlement.
     */
    @Test
    public void stopsReceivingReceiveAndDeleteMessagesBeforeDraining() throws InterruptedException {
        final CountDownLatch intakeStopped = new CountDownLatch(1);
        final AtomicReference<FluxSink<ServiceBusMessageContext>> sinkRef = new AtomicReference<>();
        final Flux<ServiceBusMessageContext> messages = Flux.create(sink -> {
            sinkRef.set(sink);
            sink.onCancel(intakeStopped::countDown);
        });
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        stubReceiverBasics();
        when(mockReceiver.receiveMessagesWithContextForAsyncProcessor()).thenReturn(messages);
        final ReceiverOptions receiverOptions = mock(ReceiverOptions.class);
        when(receiverOptions.getReceiveMode()).thenReturn(ServiceBusReceiveMode.RECEIVE_AND_DELETE);
        when(mockReceiver.getReceiverOptions()).thenReturn(receiverOptions);

        final Set<String> processedIds = Collections.synchronizedSet(new HashSet<>());
        final CountDownLatch slowStarted = new CountDownLatch(1);
        final CountDownLatch allowFinish = new CountDownLatch(1);
        // Spare handler slots ensure cancellation, rather than backpressure, prevents further dispatch.
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                final String id = context.getMessage().getMessageId();
                if ("slow".equals(id)) {
                    return Mono.fromRunnable(slowStarted::countDown).then(Mono.fromRunnable(() -> {
                        try {
                            allowFinish.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).subscribeOn(Schedulers.boundedElastic())).then();
                }
                return Mono.fromRunnable(() -> {
                    processedIds.add(id);
                });
            }, error -> Mono.empty(), options(3, false));

        processor.start().block();
        sinkRef.get().next(messageContext("slow"));
        assertTrue(slowStarted.await(5, TimeUnit.SECONDS), "Slow handler never started");

        final AtomicBoolean closeReturned = new AtomicBoolean();
        final Thread closeThread = new Thread(() -> {
            processor.close();
            closeReturned.set(true);
        });
        closeThread.setDaemon(true);
        closeThread.start();
        assertFalse(intakeStopped.await(300, TimeUnit.MILLISECONDS),
            "The receiver subscription must stay open while draining");
        assertFalse(processor.isRunning(), "The processor must stop dispatching before draining");
        assertFalse(closeReturned.get(), "The accepted handler must still be draining");

        sinkRef.get().next(messageContext("fast1"));
        sinkRef.get().next(messageContext("fast2"));

        allowFinish.countDown();
        closeThread.join(TimeUnit.SECONDS.toMillis(5));
        assertTrue(closeReturned.get(), "close() did not return");
        assertTrue(intakeStopped.await(5, TimeUnit.SECONDS), "The receiver subscription must close after draining");
        assertTrue(processedIds.isEmpty(), "No messages should be accepted during drain");
        // The broker already removed RECEIVE_AND_DELETE messages on delivery, so the processor must not settle them.
        verify(mockReceiver, never()).complete(any());
        verify(mockReceiver, never()).abandon(any());
    }

    @Test
    public void failedStartCanBeRetried() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        when(builder.buildAsyncClientForProcessor()).thenThrow(new IllegalStateException("initialization failed"))
            .thenReturn(mockReceiver);
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));

        assertThrows(IllegalStateException.class, () -> processor.start().block());
        assertFalse(processor.isRunning());
        processor.start().block();
        assertTrue(processor.isRunning());
        processor.close();
        verify(builder, times(2)).buildAsyncClientForProcessor();
    }

    @Test
    public void failedSubscriptionPreservesOriginalErrorAndCanBeRetried() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        final IllegalStateException initialization = new IllegalStateException("subscription initialization failed");
        final IllegalArgumentException cleanup = new IllegalArgumentException("receiver cleanup failed");
        when(mockReceiver.receiveMessagesWithContextForAsyncProcessor()).thenThrow(initialization)
            .thenReturn(Flux.never());
        doThrow(cleanup).doNothing().when(mockReceiver).close();
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));

        assertEquals(initialization, assertThrows(IllegalStateException.class, () -> processor.start().block()));
        assertTrue(java.util.Arrays.asList(initialization.getSuppressed()).contains(cleanup));
        assertFalse(processor.isRunning());
        processor.start().block();
        assertTrue(processor.isRunning());
        processor.close();
        verify(builder, times(2)).buildAsyncClientForProcessor();
    }

    @Test
    public void failedRestartSubscriptionIsClosedAndRetriedByMonitor() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = nonSessionBuilder(Flux.error(new IllegalStateException("receive failed")));
        when(mockReceiver.receiveMessagesWithContextForAsyncProcessor())
            .thenReturn(Flux.error(new IllegalStateException("receive failed")))
            .thenReturn(Flux.never());
        final ServiceBusReceiverAsyncClient broken = mock(ServiceBusReceiverAsyncClient.class);
        when(broken.receiveMessagesWithContextForAsyncProcessor())
            .thenThrow(new IllegalStateException("restart initialization failed"));
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver)
            .thenReturn(broken)
            .thenReturn(mockReceiver);
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));

        try {
            processor.start().block();
            verify(broken, timeout(15000)).close();
            verify(builder, timeout(15000).times(3)).buildAsyncClientForProcessor();
            assertTrue(processor.isRunning());
        } finally {
            processor.close();
        }
    }

    @Test
    public void manualSettlementPreservesServiceErrorSource() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messageContexts(1));
        when(mockReceiver.complete(any())).thenReturn(Mono
            .error(new ServiceBusException(new IllegalStateException("lock lost"), ServiceBusErrorSource.COMPLETE)));
        final AtomicReference<ServiceBusErrorSource> source = new AtomicReference<>();
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, ServiceBusReceivedMessageAsyncContext::complete, error -> {
                source.set(((ServiceBusException) error.getException()).getErrorSource());
                return Mono.empty();
            }, options(1, true));

        processor.start().block();
        assertEquals(ServiceBusErrorSource.COMPLETE, source.get());
        processor.close();
        verify(mockReceiver, never()).abandon(any());
    }

    @ParameterizedTest
    @CsvSource({ "false,false", "false,true", "true,false", "true,true" })
    public void publicBuildersSupportQueuesAndSubscriptions(boolean sessions, boolean subscription) {
        final ServiceBusClientBuilder builder
            = new ServiceBusClientBuilder().credential("namespace.servicebus.windows.net", mock(TokenCredential.class));
        final ServiceBusProcessorAsyncClient processor;
        if (sessions) {
            final ServiceBusClientBuilder.ServiceBusSessionProcessorAsyncClientBuilder asyncBuilder
                = builder.sessionProcessorAsync()
                    .processMessage(context -> Mono.empty())
                    .processError(error -> Mono.empty());
            if (subscription) {
                asyncBuilder.topicName("topic").subscriptionName("subscription");
            } else {
                asyncBuilder.queueName("queue");
            }
            processor = asyncBuilder.buildProcessorAsyncClient();
        } else {
            final ServiceBusClientBuilder.ServiceBusProcessorAsyncClientBuilder asyncBuilder
                = builder.processorAsync().processMessage(context -> Mono.empty()).processError(error -> Mono.empty());
            if (subscription) {
                asyncBuilder.topicName("topic").subscriptionName("subscription");
            } else {
                asyncBuilder.queueName("queue");
            }
            processor = asyncBuilder.buildProcessorAsyncClient();
        }
        try {
            assertEquals(subscription ? null : "queue", processor.getQueueName());
            assertEquals(subscription ? "topic" : null, processor.getTopicName());
            assertEquals(subscription ? "subscription" : null, processor.getSubscriptionName());
            assertFalse(processor.isRunning());
        } finally {
            processor.close();
        }
    }

    @Test
    public void publicBuildersRejectMissingHandlersAndInvalidConcurrency() {
        final ServiceBusClientBuilder builder
            = new ServiceBusClientBuilder().credential("namespace.servicebus.windows.net", mock(TokenCredential.class));
        assertThrows(NullPointerException.class,
            () -> builder.processorAsync()
                .queueName("queue")
                .processError(error -> Mono.empty())
                .buildProcessorAsyncClient());
        assertThrows(NullPointerException.class,
            () -> builder.sessionProcessorAsync()
                .queueName("queue")
                .processMessage(context -> Mono.empty())
                .buildProcessorAsyncClient());
        assertThrows(IllegalArgumentException.class, () -> builder.processorAsync().maxConcurrentCalls(0));
        assertThrows(IllegalArgumentException.class, () -> builder.sessionProcessorAsync().maxConcurrentCalls(0));
        assertThrows(IllegalArgumentException.class, () -> builder.sessionProcessorAsync().maxConcurrentSessions(0));
    }

    @Test
    public void manualCompletionWaitsForDownstreamFlush() {
        final Sinks.One<Void> flush = Sinks.one();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messageContexts(1));
        final AtomicBoolean subscribed = new AtomicBoolean();
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> flush.asMono().then(context.complete()).doOnSubscribe(ignored -> subscribed.set(true)),
            error -> Mono.empty(), options(1, true));

        processor.start().block();
        assertTrue(subscribed.get(), "Handler subscription must return without waiting for the flush");
        verify(mockReceiver, never()).complete(any());
        flush.tryEmitEmpty();
        verify(mockReceiver, times(1)).complete(any());
        processor.close();
    }

    @Test
    public void failedDownstreamFlushDoesNotCompleteMessage() {
        final Sinks.One<Void> flush = Sinks.one();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messageContexts(1));
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> flush.asMono().then(context.complete()), context -> {
                error.set(context.getException());
                return Mono.empty();
            }, options(1, true));

        processor.start().block();
        flush.tryEmitError(new IllegalStateException("flush failed"));
        assertTrue(error.get() instanceof ServiceBusException);
        verify(mockReceiver, never()).complete(any());
        processor.close();
    }

    @Test
    public void closeCancelsHandlersOnlyAfterDrainTimeout() {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messageContexts(1));
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.<Void>never().doOnCancel(() -> cancelled.set(true)), error -> Mono.empty(),
            options(1, false).setDrainTimeout(Duration.ofMillis(50)));

        processor.start().block();
        assertFalse(cancelled.get());
        processor.close();
        assertTrue(cancelled.get());
        verify(mockReceiver).close();
        verify(mockReceiver, never()).complete(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 0, 60 })
    public void sessionFactoryUsesV2AndPreservesDefaultsAndSubQueue(Integer renewalSeconds) throws Exception {
        final Duration renewalDuration = renewalSeconds == null ? null : Duration.ofSeconds(renewalSeconds);
        final Duration expectedDuration = renewalDuration == null ? Duration.ZERO : renewalDuration;
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder
            = new ServiceBusClientBuilder().credential("namespace.servicebus.windows.net", mock(TokenCredential.class))
                .sessionReceiver()
                .queueName("queue")
                .subQueue(com.azure.messaging.servicebus.models.SubQueue.DEAD_LETTER_QUEUE)
                .maxAutoLockRenewDuration(renewalDuration)
                .disableAutoComplete();
        try (ServiceBusSessionReceiverAsyncClient sessions = builder.buildAsyncClientForAsyncProcessor()) {
            final java.lang.reflect.Field field
                = ServiceBusSessionReceiverAsyncClient.class.getDeclaredField("sessionAcquirer");
            field.setAccessible(true);
            assertTrue(field.get(sessions) != null, "Async session processing must use the V2 acquirer");
            assertEquals("queue/$deadletterqueue", sessions.getEntityPath());
            assertEquals(new com.azure.core.amqp.AmqpRetryOptions().getTryTimeout(), sessions.getSessionIdleTimeout());
            assertFalse(sessions.getReceiverOptions().isEnableAutoComplete());
            assertEquals(expectedDuration, sessions.getReceiverOptions().getMaxLockRenewDuration());
            assertEquals(renewalSeconds != null && renewalSeconds > 0,
                sessions.getReceiverOptions().isAutoLockRenewEnabled());
        }
    }

    @Test
    public void lifecycleOperationsAreCold() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));
        final Mono<Void> start = processor.start();
        assertFalse(processor.isRunning());
        verify(builder, never()).buildAsyncClientForProcessor();
        start.block();
        final Mono<Void> stop = processor.stop();
        assertTrue(processor.isRunning());
        stop.block();
        assertFalse(processor.isRunning());
        processor.close();
    }

    @Test
    public void stopPreservesSubscriptionAndLetsAcceptedWorkFinish() {
        final AtomicInteger subscriptions = new AtomicInteger();
        final AtomicBoolean cancelled = new AtomicBoolean();
        final Sinks.Empty<Void> finish = Sinks.empty();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = nonSessionBuilder(messageContexts(2).doOnSubscribe(ignored -> subscriptions.incrementAndGet()));
        final AtomicInteger processed = new AtomicInteger();
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                processed.incrementAndGet();
                return finish.asMono().doOnCancel(() -> cancelled.set(true));
            }, error -> Mono.empty(), options(1, false));
        try {
            processor.start().block();
            processor.stop().block();
            finish.tryEmitEmpty();
            assertFalse(cancelled.get());
            assertEquals(1, processed.get());
            verify(mockReceiver).complete(any());
            processor.start().block();
            assertEquals(2, processed.get());
            assertEquals(1, subscriptions.get(), "Resume must not resubscribe to the receiver");
        } finally {
            finish.tryEmitEmpty();
            processor.close();
        }
    }

    @Test
    public void terminalErrorHandlerIsAwaitedDuringClose() throws Exception {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = nonSessionBuilder(Flux.error(new IllegalStateException("receive failed")));
        final Sinks.Empty<Void> reported = Sinks.empty();
        final CountDownLatch closing = new CountDownLatch(1);
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> reported.asMono(), options(1, false));
        processor.start().block();
        final FutureTask<Void> closed = new FutureTask<>(() -> {
            closing.countDown();
            processor.close();
        }, null);
        new Thread(closed, "processor-close").start();
        try {
            assertTrue(closing.await(5, TimeUnit.SECONDS));
            assertThrows(java.util.concurrent.TimeoutException.class, () -> closed.get(100, TimeUnit.MILLISECONDS));
            verify(builder).buildAsyncClientForProcessor();
        } finally {
            reported.tryEmitEmpty();
            closed.get(5, TimeUnit.SECONDS);
        }
        verify(builder).buildAsyncClientForProcessor();
    }

    @Test
    public void recoveryErrorHandlerIsAwaitedDuringClose() throws Exception {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver)
            .thenThrow(new IllegalStateException("receiver creation failed"));
        final Sinks.Empty<Void> reported = Sinks.empty();
        final CountDownLatch reporting = new CountDownLatch(1);
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> Mono.empty(), error -> {
                reporting.countDown();
                return reported.asMono();
            }, options(1, false));
        processor.start().block();
        final java.lang.reflect.Field field
            = ServiceBusProcessorAsyncClient.class.getDeclaredField("receiveDisposable");
        field.setAccessible(true);
        final Object cycle = ((AtomicReference<?>) field.get(processor)).get();
        final java.lang.reflect.Method restart
            = ServiceBusProcessorAsyncClient.class.getDeclaredMethod("restartMessageReceiver", Disposable.class);
        restart.setAccessible(true);
        restart.invoke(processor, cycle);
        assertTrue(reporting.await(5, TimeUnit.SECONDS));
        final FutureTask<Void> closed = new FutureTask<>(processor::close, null);
        new Thread(closed, "processor-close").start();
        try {
            assertThrows(java.util.concurrent.TimeoutException.class, () -> closed.get(100, TimeUnit.MILLISECONDS));
        } finally {
            reported.tryEmitEmpty();
            closed.get(5, TimeUnit.SECONDS);
        }
        verify(builder, times(2)).buildAsyncClientForProcessor();
    }

    @Test
    public void autoCompleteFailureEndsProcessingSpanWithError() {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messageContexts(1));
        final Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        when(tracer.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenReturn(Context.NONE);
        when(mockReceiver.getInstrumentation()).thenReturn(
            new ServiceBusReceiverInstrumentation(tracer, null, NAMESPACE, ENTITY_NAME, null, ReceiverKind.PROCESSOR));
        final ServiceBusException failure
            = new ServiceBusException(new IllegalStateException("complete failed"), ServiceBusErrorSource.COMPLETE);
        when(mockReceiver.complete(any())).thenReturn(Mono.error(failure));
        final AtomicReference<Throwable> reported = new AtomicReference<>();
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> Mono.empty(), error -> {
                reported.set(error.getException());
                return Mono.empty();
            }, options(1, false));
        try {
            processor.start().block();
            assertEquals(failure, reported.get());
            verify(tracer).end(isNull(), eq(failure), eq(Context.NONE));
            verify(mockReceiver, never()).abandon(any());
        } finally {
            processor.close();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void handlerFailureSpanIncludesErrorHandlingAndAbandon(boolean abandonFails) {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messageContexts(1));
        final Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        when(tracer.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenReturn(Context.NONE);
        when(mockReceiver.getInstrumentation()).thenReturn(
            new ServiceBusReceiverInstrumentation(tracer, null, NAMESPACE, ENTITY_NAME, null, ReceiverKind.PROCESSOR));
        final IllegalStateException failure = new IllegalStateException("handler failed");
        final Sinks.Empty<Void> errorHandled = Sinks.empty();
        final Sinks.Empty<Void> abandoned = Sinks.empty();
        when(mockReceiver.abandon(any())).thenReturn(abandoned.asMono());
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.error(failure), error -> errorHandled.asMono(), options(1, false));
        try {
            processor.start().block();
            verify(tracer, never()).end(any(), any(), any());
            verify(mockReceiver, never()).abandon(any());
            errorHandled.tryEmitEmpty();
            verify(mockReceiver).abandon(any());
            verify(tracer, never()).end(any(), any(), any());
            if (abandonFails) {
                abandoned.tryEmitError(new ServiceBusException(new IllegalStateException("abandon failed"),
                    ServiceBusErrorSource.ABANDON));
            } else {
                abandoned.tryEmitEmpty();
            }
            verify(tracer).end(isNull(), eq(failure), eq(Context.NONE));
            verify(mockReceiver, never()).complete(any());
            verify(builder).buildAsyncClientForProcessor();
        } finally {
            errorHandled.tryEmitEmpty();
            abandoned.tryEmitEmpty();
            processor.close();
        }
    }

    @Test
    public void staleRecoveryDoesNotCloseTheReplacementReceiver() throws Exception {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(Flux.never());
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));
        try {
            processor.start().block();
            final java.lang.reflect.Field field
                = ServiceBusProcessorAsyncClient.class.getDeclaredField("receiveDisposable");
            field.setAccessible(true);
            final Object reference = field.get(processor);
            final Object cycle = ((AtomicReference<?>) reference).get();
            final java.lang.reflect.Method restart
                = ServiceBusProcessorAsyncClient.class.getDeclaredMethod("restartMessageReceiver", Disposable.class);
            restart.setAccessible(true);
            restart.invoke(processor, cycle);
            restart.invoke(processor, cycle);
            verify(builder, times(2)).buildAsyncClientForProcessor();
            verify(mockReceiver).close();
        } finally {
            processor.close();
        }
    }

    @ParameterizedTest
    @CsvSource({
        "false, healthy",
        "true, healthy",
        "false, terminated",
        "true, terminated",
        "false, cleanup",
        "true, cleanup" })
    public void staleTerminalCallbackDoesNotChangeReplacementState(boolean complete, String replacementState)
        throws Exception {
        final AtomicReference<FluxSink<ServiceBusMessageContext>> intake = new AtomicReference<>();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = nonSessionBuilder(Flux.create(intake::set));
        final ServiceBusReceiverAsyncClient initialReceiver = mockReceiver;
        final ServiceBusReceiverAsyncClient replacement = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceiverAsyncClient recovered = mock(ServiceBusReceiverAsyncClient.class);
        stubReceiverBasics(replacement);
        stubReceiverBasics(recovered);
        final AtomicReference<FluxSink<ServiceBusMessageContext>> replacementIntake = new AtomicReference<>();
        when(replacement.receiveMessagesWithContextForAsyncProcessor()).thenReturn(Flux.create(replacementIntake::set));
        when(recovered.receiveMessagesWithContextForAsyncProcessor()).thenReturn(Flux.never());
        final CountDownLatch monitorObserved = new CountDownLatch(1);
        final AtomicInteger builds = new AtomicInteger();
        when(builder.buildAsyncClientForProcessor()).thenAnswer(invocation -> {
            final int count = builds.incrementAndGet();
            if (count == 1) {
                return initialReceiver;
            } else if (count == 2) {
                return replacement;
            }
            monitorObserved.countDown();
            return recovered;
        });
        when(replacement.isConnectionClosed()).thenAnswer(invocation -> {
            monitorObserved.countDown();
            return false;
        });
        final CountDownLatch terminalRead = new CountDownLatch(1);
        final CountDownLatch releaseTerminal = new CountDownLatch(1);
        final AtomicBoolean terminalReleased = new AtomicBoolean();
        final CountDownLatch terminalFinished = new CountDownLatch(1);
        final CountDownLatch cleanupStarted = new CountDownLatch(1);
        final Sinks.Empty<Void> handlerFinished = Sinks.empty();
        final AtomicBoolean handlerCancelled = new AtomicBoolean();
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> handlerFinished.asMono().doOnCancel(() -> handlerCancelled.set(true)),
            error -> Mono.empty(), options(1, false));
        final FutureTask<Void> terminal = new FutureTask<>(() -> {
            try {
                if (complete) {
                    intake.get().complete();
                } else {
                    intake.get().error(new IllegalStateException("old receiver terminated"));
                }
            } finally {
                terminalFinished.countDown();
            }
        }, null);
        final Thread terminalThread = new Thread(terminal, "processor-terminal");
        FutureTask<Void> restarted = null;
        try {
            processor.start().block();
            final java.lang.reflect.Method disposeMonitor
                = ServiceBusProcessorAsyncClient.class.getDeclaredMethod("disposeMonitor");
            disposeMonitor.setAccessible(true);
            disposeMonitor.invoke(processor);
            final java.lang.reflect.Field field
                = ServiceBusProcessorAsyncClient.class.getDeclaredField("receiveDisposable");
            field.setAccessible(true);
            final AtomicReference<?> cycles = spy((AtomicReference<?>) field.get(processor));
            field.set(processor, cycles);
            // Hold the identity read across replacement to force the old callback's check/write window.
            doAnswer(invocation -> {
                final Object cycle = invocation.callRealMethod();
                if (Thread.currentThread() == terminalThread) {
                    terminalRead.countDown();
                    terminalReleased.set(releaseTerminal.await(30, TimeUnit.SECONDS));
                }
                return cycle;
            }).when(cycles).get();
            final Object cycle = cycles.get();
            final java.lang.reflect.Method restart
                = ServiceBusProcessorAsyncClient.class.getDeclaredMethod("restartMessageReceiver", Disposable.class);
            restart.setAccessible(true);
            if ("cleanup".equals(replacementState)) {
                doAnswer(invocation -> {
                    cleanupStarted.countDown();
                    assertTrue(terminalFinished.await(5, TimeUnit.SECONDS),
                        "Terminal callback blocked behind receiver cleanup");
                    return null;
                }).when(initialReceiver).close();
            }
            terminalThread.start();
            assertTrue(terminalRead.await(5, TimeUnit.SECONDS), "Terminal callback did not read its receive cycle");
            restarted = new FutureTask<>(() -> {
                restart.invoke(processor, cycle);
                return null;
            });
            new Thread(restarted, "processor-restart").start();
            if ("cleanup".equals(replacementState)) {
                assertTrue(cleanupStarted.await(5, TimeUnit.SECONDS));
                releaseTerminal.countDown();
            }
            restarted.get(5, TimeUnit.SECONDS);
            replacementIntake.get().next(messageContext("replacement"));
            if ("terminated".equals(replacementState)) {
                replacementIntake.get().error(new IllegalStateException("replacement receiver terminated"));
            }
            releaseTerminal.countDown();
            terminal.get(5, TimeUnit.SECONDS);
            assertTrue(terminalReleased.get(), "Terminal callback exceeded the interleaving barrier timeout");
            final java.lang.reflect.Method startMonitor
                = ServiceBusProcessorAsyncClient.class.getDeclaredMethod("startMonitor");
            startMonitor.setAccessible(true);
            startMonitor.invoke(processor);
            assertTrue(monitorObserved.await(12, TimeUnit.SECONDS), "Recovery monitor did not inspect the replacement");
            if ("terminated".equals(replacementState)) {
                verify(builder, times(3)).buildAsyncClientForProcessor();
                verify(replacement).close();
            } else {
                verify(builder, times(2)).buildAsyncClientForProcessor();
                verify(replacement, never()).close();
                assertFalse(handlerCancelled.get(), "A stale terminal callback cancelled the replacement's handler");
                handlerFinished.tryEmitEmpty();
                verify(replacement).complete(any());
            }
        } finally {
            releaseTerminal.countDown();
            try {
                if (terminalThread.getState() != Thread.State.NEW) {
                    terminal.get(5, TimeUnit.SECONDS);
                }
                if (restarted != null) {
                    restarted.get(5, TimeUnit.SECONDS);
                }
            } finally {
                handlerFinished.tryEmitEmpty();
                processor.close();
            }
        }
    }

    @Test
    public void sessionIdleTimeoutWaitsForProcessingAndSettlement() throws Exception {
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder
            = sessionBuilder(messageContexts(1));
        when(mockSessionClient.getSessionIdleTimeout()).thenReturn(Duration.ofMillis(50));
        when(mockSessionClient.acceptNextSession()).thenReturn(Mono.just(mockReceiver)).thenReturn(Mono.never());
        final CountDownLatch receiverClosed = new CountDownLatch(1);
        org.mockito.Mockito.doAnswer(ignored -> {
            receiverClosed.countDown();
            return null;
        }).when(mockReceiver).close();
        final Sinks.Empty<Void> flush = Sinks.empty();
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> flush.asMono(), error -> Mono.empty(), options(1, false));
        try {
            processor.start().block();
            assertFalse(receiverClosed.await(200, TimeUnit.MILLISECONDS),
                "Idle rollover closed a session whose handler was still working");
            flush.tryEmitEmpty();
            assertTrue(receiverClosed.await(5, TimeUnit.SECONDS), "Idle session was not released");
            verify(mockSessionClient, timeout(5000).times(2)).acceptNextSession();
            final org.mockito.InOrder order = org.mockito.Mockito.inOrder(mockReceiver);
            order.verify(mockReceiver).complete(any());
            order.verify(mockReceiver).close();
        } finally {
            flush.tryEmitEmpty();
            processor.close();
        }
        verify(mockSessionClient).close();
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2 })
    public void concurrencyIsGlobalAcrossV2Sessions(int concurrency) throws Exception {
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder
            = sessionBuilder(messageContexts(1));
        final ServiceBusReceiverAsyncClient first = mockReceiver;
        final ServiceBusReceiverAsyncClient second = mock(ServiceBusReceiverAsyncClient.class);
        stubReceiverBasics(second);
        when(second.receiveMessagesWithContextForAsyncProcessor()).thenReturn(messageContexts(1));
        when(mockSessionClient.getReceiverOptions()).thenReturn(ReceiverOptions.createUnnamedSessionOptions(
            ServiceBusReceiveMode.PEEK_LOCK, 0, Duration.ZERO, false, 2, Duration.ofMinutes(1)));
        when(mockSessionClient.acceptNextSession()).thenReturn(Mono.just(first))
            .thenReturn(Mono.just(second))
            .thenReturn(Mono.never());
        final Sinks.Empty<Void> flush = Sinks.empty();
        final CountDownLatch entered = new CountDownLatch(concurrency);
        final CountDownLatch processed = new CountDownLatch(2);
        final AtomicInteger active = new AtomicInteger();
        final AtomicInteger maximum = new AtomicInteger();
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                maximum.accumulateAndGet(active.incrementAndGet(), Math::max);
                entered.countDown();
                return flush.asMono().doOnSuccess(ignored -> {
                    active.decrementAndGet();
                    processed.countDown();
                });
            }, error -> Mono.empty(), options(concurrency, false));
        try {
            processor.start().block();
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            assertEquals(concurrency, maximum.get());
            flush.tryEmitEmpty();
            assertTrue(processed.await(5, TimeUnit.SECONDS));
            assertEquals(concurrency, maximum.get(), "Concurrency must not multiply by the session count");
        } finally {
            flush.tryEmitEmpty();
            processor.close();
        }
        verify(first).close();
        verify(second).close();
        verify(mockSessionClient).close();
        verify(builder, never()).buildAsyncClientForProcessor();
    }

    @ParameterizedTest
    @CsvSource({ "true,true", "true,false", "false,true", "false,false" })
    public void invalidCallbacksAreReportedWithoutKillingThePump(boolean throwMessage, boolean throwError) {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messageContexts(2));
        final AtomicInteger errors = new AtomicInteger();
        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> {
                if ("0".equals(context.getMessage().getMessageId())) {
                    if (throwMessage) {
                        throw new IllegalStateException("handler threw");
                    }
                    return null;
                }
                return Mono.empty();
            }, error -> {
                errors.incrementAndGet();
                if (throwError) {
                    throw new IllegalStateException("error handler threw");
                }
                return null;
            }, options(1, false));
        try {
            processor.start().block();
            assertEquals(1, errors.get());
            verify(mockReceiver).abandon(any());
            verify(mockReceiver).complete(any());
            verify(builder).buildAsyncClientForProcessor();
        } finally {
            processor.close();
        }
    }

    @Test
    public void closeCancelsAnOutstandingV2SessionAcquire() {
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder = sessionBuilder(Flux.never());
        final AtomicBoolean cancelled = new AtomicBoolean();
        when(mockSessionClient.acceptNextSession())
            .thenReturn(Mono.<ServiceBusReceiverAsyncClient>never().doOnCancel(() -> cancelled.set(true)));
        final ServiceBusProcessorAsyncClient processor = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null,
            null, context -> Mono.empty(), error -> Mono.empty(), options(1, false));
        processor.start().block();
        processor.close();
        assertTrue(cancelled.get());
        verify(mockSessionClient).close();
        verify(mockReceiver, never()).close();
    }

    private ServiceBusProcessorClientOptions options(int maxConcurrentCalls, boolean disableAutoComplete) {
        return new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(maxConcurrentCalls)
            .setDisableAutoComplete(disableAutoComplete);
    }

    private static ServiceBusMessageContext messageContext(String messageId) {
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
        message.setMessageId(messageId);
        return new ServiceBusMessageContext(message);
    }

    private static Flux<ServiceBusMessageContext> messageContexts(int count) {
        final List<ServiceBusMessageContext> contexts = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < count; i++) {
            final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
            message.setMessageId(String.valueOf(i));
            message.setSessionId(String.valueOf(i % 3));
            contexts.add(new ServiceBusMessageContext(message));
        }
        // Emit the messages then stay open (like a live receiver stream). A completing stream would trigger the
        // processor's restart-on-complete path and re-subscribe this cold flux, re-emitting the same messages.
        return Flux.fromIterable(contexts).concatWith(Flux.never());
    }

    private ServiceBusClientBuilder.ServiceBusReceiverClientBuilder
        nonSessionBuilder(Flux<ServiceBusMessageContext> messages) {
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        stubReceiver(messages);
        return builder;
    }

    private ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder
        sessionBuilder(Flux<ServiceBusMessageContext> messages) {
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusSessionReceiverAsyncClient sessions = mock(ServiceBusSessionReceiverAsyncClient.class);
        mockSessionClient = sessions;
        when(builder.buildAsyncClientForAsyncProcessor()).thenReturn(sessions);
        when(sessions.acceptNextSession()).thenReturn(Mono.just(mockReceiver));
        when(sessions.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        when(sessions.getEntityPath()).thenReturn(ENTITY_NAME);
        when(sessions.getReceiverOptions()).thenReturn(ReceiverOptions.createUnnamedSessionOptions(
            ServiceBusReceiveMode.PEEK_LOCK, 0, Duration.ZERO, false, 1, Duration.ofMinutes(1)));
        when(sessions.getSessionIdleTimeout()).thenReturn(Duration.ofMinutes(1));
        stubReceiver(messages);
        return builder;
    }

    private void stubReceiver(Flux<ServiceBusMessageContext> messages) {
        stubReceiverBasics();
        when(mockReceiver.receiveMessagesWithContextForAsyncProcessor()).thenReturn(messages);
    }

    private void stubReceiverBasics() {
        stubReceiverBasics(mockReceiver);
    }

    private void stubReceiverBasics(ServiceBusReceiverAsyncClient receiver) {
        when(receiver.getInstrumentation()).thenReturn(
            new ServiceBusReceiverInstrumentation(null, null, NAMESPACE, ENTITY_NAME, null, ReceiverKind.PROCESSOR));
        when(receiver.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        when(receiver.getEntityPath()).thenReturn(ENTITY_NAME);
        when(receiver.isConnectionClosed()).thenReturn(false);
        when(receiver.complete(any())).thenReturn(Mono.empty());
        when(receiver.abandon(any())).thenReturn(Mono.empty());
        // Default the receiver to PEEK_LOCK so auto-settlement is exercised; tests that need RECEIVE_AND_DELETE
        // override getReceiverOptions() after calling this.
        final ReceiverOptions peekLockOptions = mock(ReceiverOptions.class);
        when(peekLockOptions.getReceiveMode()).thenReturn(ServiceBusReceiveMode.PEEK_LOCK);
        when(receiver.getReceiverOptions()).thenReturn(peekLockOptions);
        doNothing().when(receiver).close();
    }
}
