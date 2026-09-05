// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        when(mockReceiver.receiveMessagesWithContext())
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
        verify(builder, timeout(5000).times(2)).buildAsyncClientForProcessor();
        processor.close();
        // close() must not trigger a further restart - the count stays at two.
        verify(builder, times(2)).buildAsyncClientForProcessor();
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
    @Test
    public void concurrencyBoundedByMaxConcurrentCalls() throws InterruptedException {
        final int maxConcurrentCalls = 2;
        final int messageCount = 8;
        final Flux<ServiceBusMessageContext> messages = messageContexts(messageCount);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = nonSessionBuilder(messages);

        final CountDownLatch latch = new CountDownLatch(messageCount);
        final AtomicInteger inFlight = new AtomicInteger();
        final AtomicInteger maxObserved = new AtomicInteger();

        final ServiceBusProcessorAsyncClient processor
            = new ServiceBusProcessorAsyncClient(builder, ENTITY_NAME, null, null, context -> Mono.fromRunnable(() -> {
                final int current = inFlight.incrementAndGet();
                maxObserved.accumulateAndGet(current, Math::max);
            }).then(Mono.delay(Duration.ofMillis(40))).then(Mono.fromRunnable(() -> {
                inFlight.decrementAndGet();
                latch.countDown();
            })), error -> Mono.empty(), options(maxConcurrentCalls, false));

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
        when(mockReceiver.receiveMessagesWithContext()).thenReturn(Flux.just(new ServiceBusMessageContext(message)))
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
        verify(builder, timeout(5000).times(2)).buildAsyncClientForProcessor();
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
     * During the close drain window, new PEEK_LOCK dispatches are skipped (not processed or settled) so the in-flight
     * count can reach zero, while the already in-flight handler still settles.
     */
    @Test
    public void skipsNewPeekLockDispatchesDuringDrain() throws InterruptedException {
        final AtomicReference<FluxSink<ServiceBusMessageContext>> sinkRef = new AtomicReference<>();
        final Flux<ServiceBusMessageContext> messages = Flux.create(sinkRef::set);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        stubReceiverBasics();
        when(mockReceiver.receiveMessagesWithContext()).thenReturn(messages);
        final ReceiverOptions receiverOptions = mock(ReceiverOptions.class);
        when(receiverOptions.getReceiveMode()).thenReturn(ServiceBusReceiveMode.PEEK_LOCK);
        when(mockReceiver.getReceiverOptions()).thenReturn(receiverOptions);

        final CountDownLatch slowStarted = new CountDownLatch(1);
        final CountDownLatch allowFinish = new CountDownLatch(1);
        final Set<String> processedIds = Collections.synchronizedSet(new HashSet<>());
        // maxConcurrentCalls = 3 so the two messages that arrive while the slow handler is held enter the dispatch
        // pipeline concurrently (rather than being buffered behind a single slot). This is what makes the skip path
        // actually exercised: at concurrency 1 they would never enter dispatch and the test could pass even if the
        // skip logic were removed.
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
        // close() is now draining (closing == true), waiting for the slow handler.
        Thread.sleep(300);

        // Messages that arrive during the drain window must be skipped, not processed/settled.
        sinkRef.get().next(messageContext("fast1"));
        sinkRef.get().next(messageContext("fast2"));
        Thread.sleep(300);

        allowFinish.countDown();
        closeThread.join(TimeUnit.SECONDS.toMillis(5));
        assertTrue(closeReturned.get(), "close() did not return");
        // Only the slow message was completed; the two PEEK_LOCK messages that arrived during drain were skipped.
        verify(mockReceiver, times(1)).complete(any());
        // Assert on the skip path directly: the slow handler ran, but the two PEEK_LOCK messages that entered the
        // dispatch pipeline during the drain window were skipped before reaching the handler.
        assertTrue(processedIds.contains("slow"), "Slow handler should have run");
        assertFalse(processedIds.contains("fast1"), "fast1 arrived during drain and must be skipped (PEEK_LOCK)");
        assertFalse(processedIds.contains("fast2"), "fast2 arrived during drain and must be skipped (PEEK_LOCK)");
    }

    /**
     * During the close drain window, {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} dispatches are NOT skipped - the
     * broker has already removed the message, so dropping it here would lose it. This is the asymmetric counterpart to
     * {@link #skipsNewPeekLockDispatchesDuringDrain()}: identical setup, only the receive mode differs, and the
     * messages that arrive during drain must still be handled.
     */
    @Test
    public void processesReceiveAndDeleteDispatchesDuringDrain() throws InterruptedException {
        final AtomicReference<FluxSink<ServiceBusMessageContext>> sinkRef = new AtomicReference<>();
        final Flux<ServiceBusMessageContext> messages = Flux.create(sinkRef::set);
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        mockReceiver = mock(ServiceBusReceiverAsyncClient.class);
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        stubReceiverBasics();
        when(mockReceiver.receiveMessagesWithContext()).thenReturn(messages);
        final ReceiverOptions receiverOptions = mock(ReceiverOptions.class);
        when(receiverOptions.getReceiveMode()).thenReturn(ServiceBusReceiveMode.RECEIVE_AND_DELETE);
        when(mockReceiver.getReceiverOptions()).thenReturn(receiverOptions);

        final Set<String> processedIds = Collections.synchronizedSet(new HashSet<>());
        final CountDownLatch slowStarted = new CountDownLatch(1);
        final CountDownLatch allowFinish = new CountDownLatch(1);
        final CountDownLatch fastProcessed = new CountDownLatch(2);
        // maxConcurrentCalls = 3 so the two drain-window messages run concurrently with the still-held slow handler.
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
                    fastProcessed.countDown();
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
        // close() is now draining (closing == true), waiting for the slow handler.
        Thread.sleep(300);

        // RECEIVE_AND_DELETE messages that arrive during the drain window must still be handled, not dropped. They are
        // processed while the slow handler is still held (close() still blocked), proving the not-skip branch.
        sinkRef.get().next(messageContext("fast1"));
        sinkRef.get().next(messageContext("fast2"));
        final boolean bothHandled = fastProcessed.await(5, TimeUnit.SECONDS);

        allowFinish.countDown();
        closeThread.join(TimeUnit.SECONDS.toMillis(5));
        assertTrue(closeReturned.get(), "close() did not return");
        assertTrue(bothHandled, "RECEIVE_AND_DELETE messages during drain must be processed, not skipped");
        assertTrue(processedIds.contains("fast1") && processedIds.contains("fast2"),
            "Both RECEIVE_AND_DELETE messages should have been handled during drain");
        // The broker already removed RECEIVE_AND_DELETE messages on delivery, so the processor must not settle them.
        verify(mockReceiver, never()).complete(any());
        verify(mockReceiver, never()).abandon(any());
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
        when(builder.buildAsyncClientForProcessor()).thenReturn(mockReceiver);
        stubReceiver(messages);
        return builder;
    }

    private void stubReceiver(Flux<ServiceBusMessageContext> messages) {
        stubReceiverBasics();
        when(mockReceiver.receiveMessagesWithContext()).thenReturn(messages);
    }

    private void stubReceiverBasics() {
        when(mockReceiver.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        when(mockReceiver.getEntityPath()).thenReturn(ENTITY_NAME);
        when(mockReceiver.isConnectionClosed()).thenReturn(false);
        when(mockReceiver.complete(any())).thenReturn(Mono.empty());
        when(mockReceiver.abandon(any())).thenReturn(Mono.empty());
        // Default the receiver to PEEK_LOCK so auto-settlement is exercised; tests that need RECEIVE_AND_DELETE
        // override getReceiverOptions() after calling this.
        final ReceiverOptions peekLockOptions = mock(ReceiverOptions.class);
        when(peekLockOptions.getReceiveMode()).thenReturn(ServiceBusReceiveMode.PEEK_LOCK);
        when(mockReceiver.getReceiverOptions()).thenReturn(peekLockOptions);
        doNothing().when(mockReceiver).close();
    }
}
