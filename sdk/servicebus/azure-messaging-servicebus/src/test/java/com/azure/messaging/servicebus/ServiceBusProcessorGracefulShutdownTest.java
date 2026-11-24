// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessor.RollingMessagePump;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for graceful shutdown behavior during processor close.
 * <p>
 * Validates that when a processor is closed, in-flight message handlers are allowed to complete
 * (including message settlement) before the underlying client is disposed. This prevents
 * {@link IllegalStateException} when handlers attempt to settle messages on a disposed receiver.
 * </p>
 *
 * <h3>Coverage Matrix</h3>
 * <ul>
 *   <li><b>V2 Non-Session</b> — {@link #v2CloseShouldWaitForInFlightHandlerBeforeClosingClient()}:
 *       Tests drain in {@code RollingMessagePump.dispose()} → {@code MessagePump.drainHandlers()}</li>
 *   <li><b>V1 Non-Session</b> — {@link #v1CloseShouldWaitForInFlightHandlerBeforeClosingClient()}:
 *       Tests drain in {@code ServiceBusProcessorClient.close()} → {@code drainV1Handlers()}</li>
 *   <li><b>Drain Timeout</b> — {@link #v2DrainShouldRespectTimeout()}:
 *       Tests {@code MessagePump.drainHandlers()} timeout behavior directly</li>
 *   <li><b>Re-entrant (single)</b> — {@link #v2DrainFromWithinHandlerShouldNotDeadlock()}:
 *       Tests re-entrant drain with no other concurrent handlers (returns true immediately)</li>
 *   <li><b>Closing Flag</b> — {@link #v2ClosingFlagPreventsNewHandlersAfterDrainStarts()}:
 *       Tests that the V2 closing flag prevents new handler dispatch during drain</li>
 *   <li><b>V1 Closing Flag</b> — {@link #v1ClosingFlagPreventsNewHandlersAfterDrainStarts()}:
 *       Tests that the V1 closing flag prevents new handler dispatch in the drain-to-cancel window</li>
 *   <li><b>V1 Restart</b> — {@link #v1StartAfterCloseResetsClosingFlag()}:
 *       Tests that {@code start()} after {@code close()} resets {@code v1Closing} so handlers run</li>
 *   <li><b>Re-entrant (concurrent)</b> — {@link #v1ReentrantCloseWaitsForOtherConcurrentHandlers()}:
 *       Tests re-entrant drain with concurrent handlers — waits for other handlers before closing</li>
 *   <li><b>Monitor Released During Drain</b> — {@link #v1CloseShouldNotHoldClientMonitorDuringDrain()}:
 *       Tests that {@code close()} releases the instance monitor across the drain wait, so handlers
 *       calling synchronized accessors (e.g. {@code isRunning()}) do not stall shutdown until the
 *       drain timeout expires</li>
 *   <li><b>Concurrent Start During Close</b> — {@link #v1ConcurrentStartDuringCloseDrainIsIgnored()}:
 *       Tests that a concurrent {@code start()} during {@code close()}'s drain window is ignored
 *       so it does not create new resources that the in-progress {@code close()} would tear down</li>
 *   <li><b>getIdentifier() During Close</b> — {@link #v1GetIdentifierDuringAndAfterCloseDoesNotCreateNewReceiver()}:
 *       Tests that {@code getIdentifier()} returns the cached identifier during/after close instead of
 *       lazy-creating a new receiver that would leak past the shutdown path</li>
 *   <li><b>Concurrent Close Ownership</b> — {@link #v1ConcurrentCloseCallsDoNotRace()}:
 *       Tests that only the first concurrent {@code close()} performs cleanup; the others return
 *       immediately so they cannot dispose state created after the owner cleared the in-progress flag</li>
 *   <li><b>V2 Concurrent Start During Close</b> — {@link #v2ConcurrentStartDuringCloseDrainIsIgnored()}:
 *       Tests that a concurrent {@code start()} during {@code processorV2.close()}'s drain window
 *       is ignored, mirroring the V1 guarantee</li>
 *   <li><b>RECEIVE_AND_DELETE No Skip (V2)</b> — {@link #v2ReceiveAndDeleteModeDoesNotSkipDuringDrain()}:
 *       Tests that the V2 pump's drain skip-path does NOT drop messages in RECEIVE_AND_DELETE mode -
 *       the broker has already removed those messages, so skipping would lose them permanently</li>
 *   <li><b>RECEIVE_AND_DELETE No Skip (V1)</b> — {@link #v1ReceiveAndDeleteModeDoesNotSkipDuringDrain()}:
 *       Mirrors the V2 guarantee for the V1 onNext path</li>
 *   <li><b>V2 Session</b> — Not directly unit-testable. The drain in
 *       {@code SessionsMessagePump.RollingSessionReceiver.terminate()} uses the identical
 *       {@code AtomicInteger} + {@code Object} monitor wait/notifyAll pattern as {@code MessagePump}.
 *       {@code SessionsMessagePump} requires a {@code ServiceBusSessionAcquirer} (AMQP connections)
 *       and {@code RollingSessionReceiver} is a private inner class, making unit testing infeasible.
 *       The session drain behavior should be verified via live/integration tests.</li>
 * </ul>
 *
 * @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/45716">Issue #45716</a>
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class ServiceBusProcessorGracefulShutdownTest {
    private static final ServiceBusReceiverInstrumentation INSTRUMENTATION
        = new ServiceBusReceiverInstrumentation(null, null, "FQDN", "entityPath", null, ReceiverKind.PROCESSOR);

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Polls the supplied predicate every 5 ms (up to 5 seconds) and fails the test if it never
     * becomes true. Used to wait for asynchronous lifecycle transitions deterministically without
     * relying on a fixed {@link Thread#sleep(long)}.
     */
    private static void waitFor(java.util.function.BooleanSupplier condition, String description)
        throws InterruptedException {
        final long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting for " + description);
            }
            Thread.sleep(5);
        }
    }

    /**
     * Returns a real {@link ReceiverOptions} configured for PEEK_LOCK. Every test in this file
     * targets the PEEK_LOCK shutdown semantics (broker re-delivers any message dropped during
     * drain), so production code reading {@code client.getReceiverOptions().getReceiveMode()}
     * must see PEEK_LOCK on the mocked async clients to take the drain-aware fast path. A
     * RECEIVE_AND_DELETE-specific test would build a different value here.
     *
     * <p>Uses the real {@code ReceiverOptions} factory rather than a Mockito mock to avoid the
     * "UnfinishedStubbing" trap that arises when this helper is invoked inside another
     * {@code when(...).thenReturn(...)} clause.</p>
     */
    private static ReceiverOptions peekLockOptions() {
        return ReceiverOptions.createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1, null, false);
    }

    /**
     * Verifies that when the V2 processor pump is disposed, in-flight message handlers
     * are allowed to complete before the underlying client is closed.
     * <p>
     * Regression test for <a href="https://github.com/Azure/azure-sdk-for-java/issues/45716">#45716</a>.
     * Before the fix, disposing the pump would immediately cancel the reactive chain (interrupting
     * handler threads via Reactor's publishOn worker disposal), then close the client. Handlers
     * that called {@code client.complete(message).block()} would fail with
     * {@link IllegalStateException}: "Cannot perform operation on a disposed receiver".
     * </p>
     * <p>
     * The fix drains in-flight handlers in {@code RollingMessagePump.dispose()} BEFORE disposing
     * the subscription, ensuring handlers complete message settlement first.
     * </p>
     */
    @Test
    public void v2CloseShouldWaitForInFlightHandlerBeforeClosingClient() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClientForProcessor()).thenReturn(client);
        when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(client.getReceiverOptions()).thenReturn(peekLockOptions());
        when(client.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(client.getEntityPath()).thenReturn("entityPath");
        when(client.isConnectionClosed()).thenReturn(false);
        when(client.isAutoLockRenewRequested()).thenReturn(false);
        // Emit one message on boundedElastic then hang. publishOn ensures the handler doesn't block
        // the subscription thread when concurrency=1 (which uses Schedulers.immediate() for the worker).
        when(client.nonSessionProcessorReceiveV2())
            .thenReturn(Flux.concat(Flux.just(message), Flux.<ServiceBusReceivedMessage>never())
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        when(client.complete(any())).thenReturn(Mono.empty());
        doNothing().when(client).close();

        // Latches to coordinate between the handler thread and the test thread.
        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch handlerCanProceed = new CountDownLatch(1);
        final AtomicBoolean handlerCompleted = new AtomicBoolean(false);

        // The handler signals when it starts, then waits for the test to allow it to proceed.
        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                handlerCanProceed.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            handlerCompleted.set(true);
        };

        final RollingMessagePump pump = new RollingMessagePump(builder, messageConsumer, e -> {
        }, 1, true, Duration.ofSeconds(30));

        // Start the pump.
        pump.begin();

        // Wait for the handler to start processing the message.
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started processing");

        // Dispose the pump while the handler is still in-flight.
        // dispose() now drains FIRST (before cancelling the subscription), so it blocks until
        // the handler completes. Run on a separate thread to avoid blocking the test.
        final CountDownLatch disposeDone = new CountDownLatch(1);
        final Thread disposeThread = new Thread(() -> {
            pump.dispose();
            disposeDone.countDown();
        });
        disposeThread.start();

        try {
            // Wait deterministically for dispose to be blocked in drainHandlers() (the drain
            // sets the closing flag and blocks on the in-flight handler counter monitor, so the
            // thread transitions to WAITING/TIMED_WAITING). Avoids the flakiness of a fixed sleep.
            waitFor(
                () -> disposeThread.getState() == Thread.State.WAITING
                    || disposeThread.getState() == Thread.State.TIMED_WAITING,
                "dispose() to be blocked in drainHandlers()");

            // Verify: client has NOT been closed yet (handler is still running, drain is blocking dispose).
            verify(client, never()).close();
            assertFalse(handlerCompleted.get(), "Handler should still be in-flight");

            // Now let the handler complete.
            handlerCanProceed.countDown();

            // Wait for dispose to finish.
            assertTrue(disposeDone.await(5, TimeUnit.SECONDS), "Dispose should complete after handler finishes");
            assertTrue(handlerCompleted.get(), "Handler should have completed");

            // Verify the client was closed (after the handler completed and drain returned).
            verify(client, timeout(2000)).close();
            // Verify complete was called (auto-disposition is enabled).
            verify(client).complete(any());
        } finally {
            handlerCanProceed.countDown();
            disposeThread.join(5000);
        }
    }

    /**
     * Verifies that when the V1 processor is closed, in-flight message handlers are allowed to
     * complete before the underlying client is closed.
     * <p>
     * Regression test for <a href="https://github.com/Azure/azure-sdk-for-java/issues/45716">#45716</a>.
     * Before the fix, the V1 path would cancel subscriptions (which interrupts handler threads
     * via Reactor's publishOn worker disposal) and then immediately close the async client.
     * </p>
     * <p>
     * The fix drains in-flight handlers BEFORE cancelling subscriptions. Setting
     * {@code isRunning = false} prevents new message requests while the drain waits for
     * in-flight handlers to complete.
     * </p>
     */
    @Test
    public void v1CloseShouldWaitForInFlightHandlerBeforeClosingClient() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final Flux<ServiceBusReceivedMessage> messageFlux = Flux.concat(Flux.just(message), Flux.never());

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        final ServiceBusReceiverInstrumentation instrumentation
            = new ServiceBusReceiverInstrumentation(null, null, "FQDN", "entityPath", null, ReceiverKind.PROCESSOR);
        when(asyncClient.getInstrumentation()).thenReturn(instrumentation);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        // V1 path uses receiveMessagesWithContext, publishOn(boundedElastic) matches real behavior
        // and ensures the handler runs on a separate thread (needed for drain testing).
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux.map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        // Latches to coordinate between the handler thread and the test thread.
        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch handlerCanProceed = new CountDownLatch(1);
        final AtomicBoolean handlerCompleted = new AtomicBoolean(false);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                handlerCanProceed.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            handlerCompleted.set(true);
        };

        // Build V1 processor (isV2 = false by NOT setting options.setV2(true))
        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1);
        // V1 path: do not set V2
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        // Start the processor (V1 path).
        processorClient.start();

        // Wait for the handler to start processing the message.
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started processing");

        // Close the processor while the handler is still in-flight.
        // close() now drains FIRST (before cancelling subscriptions), so it blocks until
        // the handler completes. Run on a separate thread to avoid blocking the test.
        final CountDownLatch closeDone = new CountDownLatch(1);
        final Thread closeThread = new Thread(() -> {
            processorClient.close();
            closeDone.countDown();
        });
        closeThread.start();

        try {
            // Wait deterministically for close() to enter drainV1Handlers(). close() sets
            // isRunning=false inside its first synchronized block before entering drain, so once
            // the predicate returns true we know close has at least taken ownership.
            waitFor(() -> !processorClient.isRunning(), "close() to have set isRunning=false");

            // Verify: client has NOT been closed yet (handler is still running, drain is blocking close).
            verify(asyncClient, never()).close();
            assertFalse(handlerCompleted.get(), "Handler should still be in-flight");

            // Now let the handler complete.
            handlerCanProceed.countDown();

            // Wait for close to finish.
            assertTrue(closeDone.await(5, TimeUnit.SECONDS), "Close should complete after handler finishes");
            assertTrue(handlerCompleted.get(), "Handler should have completed");

            // Verify the client was closed (after the handler completed).
            verify(asyncClient, timeout(2000)).close();
        } finally {
            handlerCanProceed.countDown();
            closeThread.join(5000);
        }
    }

    /**
     * Verifies that the V2 drain mechanism respects the timeout. If a handler takes longer than
     * the drain timeout, {@code drainHandlers} returns false and the processor doesn't hang
     * indefinitely.
     * <p>
     * This tests the drain mechanism directly on a {@link MessagePump} without going through
     * the full RollingMessagePump dispose path. The handler blocks forever, and we verify
     * that {@code drainHandlers()} with a short timeout returns false after approximately
     * the timeout duration.
     * </p>
     */
    @Test
    public void v2DrainShouldRespectTimeout() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClientForProcessor()).thenReturn(client);
        when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(client.getReceiverOptions()).thenReturn(peekLockOptions());
        when(client.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(client.getEntityPath()).thenReturn("entityPath");
        when(client.isConnectionClosed()).thenReturn(false);
        when(client.isAutoLockRenewRequested()).thenReturn(false);
        when(client.nonSessionProcessorReceiveV2())
            .thenReturn(Flux.concat(Flux.just(message), Flux.<ServiceBusReceivedMessage>never())
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        when(client.complete(any())).thenReturn(Mono.empty());
        doNothing().when(client).close();

        // Handler blocks forever (never releases the latch).
        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch neverReleasedLatch = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                neverReleasedLatch.await(); // Block forever
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        final MessagePump pump = new MessagePump(client, messageConsumer, e -> {
        }, 1, false);

        // Subscribe to start pumping.
        final AtomicReference<reactor.core.Disposable> subscription = new AtomicReference<>();
        subscription.set(pump.begin().subscribe());

        // Wait for the handler to start.
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started processing");

        // Call drainHandlers with a very short timeout while the handler is still running.
        // DO NOT dispose the subscription first — disposing cancels the reactive chain, which
        // interrupts the handler's thread via Reactor's publishOn worker disposal. The drain must
        // be called while the subscription (and handler) is still active.
        final long startTime = System.nanoTime();
        final boolean drained = pump.drainHandlers(Duration.ofMillis(500));
        final long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Drain should return false (timed out) and should take close to 500ms.
        assertFalse(drained, "Drain should return false when timeout expires with active handlers");
        assertTrue(elapsed >= 400,
            "Drain should wait at least close to the timeout duration, but took " + elapsed + "ms");
        assertTrue(elapsed < 3000, "Drain should not take excessively long, but took " + elapsed + "ms");

        // Clean up: release the blocked handler and dispose the subscription.
        neverReleasedLatch.countDown();
        subscription.get().dispose();
    }

    /**
     * Verifies that calling {@code drainHandlers()} from within a message handler (re-entrant)
     * does not deadlock. This simulates a user calling {@code processor.close()} from inside
     * their {@code processMessage} callback when only this handler is active (no concurrent handlers).
     * <p>
     * Without the re-entrancy guard, the handler thread would enter {@code drainHandlers()},
     * which waits for {@code activeHandlerCount} to reach 0. But the handler itself has
     * incremented the counter and won't decrement it until it returns — classic self-deadlock.
     * </p>
     * <p>
     * The fix detects the re-entrant call via a {@link ThreadLocal} flag and uses a threshold of 1
     * (only wait for OTHER handlers). With no other handlers active, it returns {@code true}
     * immediately.
     * </p>
     */
    @Test
    public void v2DrainFromWithinHandlerShouldNotDeadlock() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(client.getReceiverOptions()).thenReturn(peekLockOptions());
        when(client.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(client.getEntityPath()).thenReturn("entityPath");
        when(client.isConnectionClosed()).thenReturn(false);
        when(client.isAutoLockRenewRequested()).thenReturn(false);
        when(client.nonSessionProcessorReceiveV2())
            .thenReturn(Flux.concat(Flux.just(message), Flux.<ServiceBusReceivedMessage>never())
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        when(client.complete(any())).thenReturn(Mono.empty());
        doNothing().when(client).close();

        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch handlerDone = new CountDownLatch(1);
        final AtomicBoolean drainReturnedTrue = new AtomicBoolean(false);

        // Create the pump first, then reference it inside the handler via AtomicReference.
        final AtomicReference<MessagePump> pumpRef = new AtomicReference<>();

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            // Simulate user calling close() from within processMessage, which triggers drainHandlers().
            // With only the current handler active (no other concurrent handlers), the re-entrant drain
            // should return true immediately (nothing to drain) instead of deadlocking.
            boolean result = pumpRef.get().drainHandlers(Duration.ofSeconds(5));
            drainReturnedTrue.set(result);
            handlerDone.countDown();
        };

        final MessagePump pump = new MessagePump(client, messageConsumer, e -> {
        }, 1, false);
        pumpRef.set(pump);

        // Subscribe to start pumping.
        final AtomicReference<reactor.core.Disposable> subscription = new AtomicReference<>();
        subscription.set(pump.begin().subscribe());

        // Wait for the handler to start and complete (should NOT deadlock).
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started processing");
        assertTrue(handlerDone.await(5, TimeUnit.SECONDS),
            "Handler should have completed without deadlocking on re-entrant drainHandlers()");

        // Re-entrant drain with no other concurrent handlers should return true (nothing to drain).
        assertTrue(drainReturnedTrue.get(),
            "Re-entrant drainHandlers() with no other concurrent handlers should return true");

        // Clean up.
        subscription.get().dispose();
    }

    /**
     * Verifies that the closing flag prevents new message handlers from processing after
     * {@code drainHandlers()} is called.
     * <p>
     * Race condition scenario: with {@code flatMap(concurrency=2)}, two messages are emitted.
     * The first handler blocks (simulating in-flight work). {@code drainHandlers()} is called
     * on a separate thread, which sets {@code closing = true} and waits for the first handler
     * to complete. A second message arrives while closing is true. The second handler should
     * see the closing flag and skip processing.
     * </p>
     * <p>
     * Without the closing flag, the second handler could start real work (including settlement)
     * between drain returning and subscription disposal, reintroducing the original failure mode.
     * </p>
     */
    @Test
    public void v2ClosingFlagPreventsNewHandlersAfterDrainStarts() throws InterruptedException {
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(client.getReceiverOptions()).thenReturn(peekLockOptions());
        when(client.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(client.getEntityPath()).thenReturn("entityPath");
        when(client.isConnectionClosed()).thenReturn(false);
        when(client.isAutoLockRenewRequested()).thenReturn(false);
        when(client.complete(any())).thenReturn(Mono.empty());
        doNothing().when(client).close();

        // Use a Sinks.Many for fully controlled emission timing - we choose exactly when each
        // message enters the pipeline rather than relying on a wall-clock delay. concurrency=2
        // lets flatMap dispatch handlers in parallel.
        final reactor.core.publisher.Sinks.Many<ServiceBusReceivedMessage> messageSink
            = reactor.core.publisher.Sinks.many().unicast().onBackpressureBuffer();
        when(client.nonSessionProcessorReceiveV2())
            .thenReturn(messageSink.asFlux().publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));

        final CountDownLatch handler1Started = new CountDownLatch(1);
        final CountDownLatch handler1CanProceed = new CountDownLatch(1);
        final AtomicBoolean handler2ProcessMessageInvoked = new AtomicBoolean(false);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            if (messageContext.getMessage() == message1) {
                handler1Started.countDown();
                try {
                    handler1CanProceed.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // If this executes, the closing flag did NOT prevent the second handler.
                handler2ProcessMessageInvoked.set(true);
            }
        };

        final MessagePump pump = new MessagePump(client, messageConsumer, e -> {
        }, 2, false);

        final AtomicReference<reactor.core.Disposable> subscription = new AtomicReference<>();
        subscription.set(pump.begin().subscribe());

        // Emit message1 explicitly and wait for handler1 to start.
        messageSink.tryEmitNext(message1);
        assertTrue(handler1Started.await(5, TimeUnit.SECONDS), "Handler1 should have started processing");

        // Call drainHandlers on a separate thread. This sets closing=true and waits for handler1.
        final CountDownLatch drainDone = new CountDownLatch(1);
        final AtomicBoolean drainResult = new AtomicBoolean(false);
        final Thread drainThread = new Thread(() -> {
            drainResult.set(pump.drainHandlers(Duration.ofSeconds(10)));
            drainDone.countDown();
        });
        drainThread.start();

        // Wait deterministically for drainHandlers() to enter its wait loop. The drain thread
        // sets closing=true and then blocks on the in-flight handler counter monitor, so once
        // it transitions to WAITING/TIMED_WAITING we know the closing flag has been set. Avoids
        // the flakiness of a fixed sleep on slow/contended CI.
        waitFor(
            () -> drainThread.getState() == Thread.State.WAITING
                || drainThread.getState() == Thread.State.TIMED_WAITING,
            "drainHandlers() to enter waiting state (closing flag set)");

        // Now emit message2 - it enters the pipeline AFTER closing=true has been set, so its
        // onNext path must observe closing=true and skip dispatch.
        messageSink.tryEmitNext(message2);

        // Release handler1 so the drain can complete.
        handler1CanProceed.countDown();

        // Wait for drain to finish. drainHandlers returns when activeHandlerCount drops to 0,
        // which can only happen after BOTH handler1 (in-flight) and message2's handleMessage
        // (closing-flag check increments and decrements the counter even when it skips dispatch)
        // have completed. Drain completion is therefore a deterministic signal that message2 has
        // been processed - if the closing flag worked, the consumer was never invoked for
        // message2; if it did not, handler2ProcessMessageInvoked would be true.
        assertTrue(drainDone.await(5, TimeUnit.SECONDS), "Drain should complete after handler1 finishes");
        assertTrue(drainResult.get(), "Drain should return true (all handlers completed)");

        assertFalse(handler2ProcessMessageInvoked.get(), "Second handler should have been skipped by the closing flag");

        // Clean up.
        subscription.get().dispose();
    }

    /**
     * Verifies that when a V1 handler calls {@code close()} re-entrantly with other concurrent
     * handlers running, the re-entrant drain waits for those other handlers to complete before
     * proceeding to cancel subscriptions and close the underlying client.
     * <p>
     * With {@code maxConcurrentCalls=2}, two handlers run concurrently on separate
     * {@code boundedElastic} threads. Handler B calls {@code processorClient.close()} while
     * Handler A is still processing. {@code drainV1Handlers()} detects the re-entrant call
     * (threshold=1) and waits until only the calling handler remains before allowing
     * {@code close()} to proceed with subscription cancellation and client disposal.
     * </p>
     * <p>
     * Without this fix, the re-entrant drain would return immediately, and {@code close()}
     * would cancel subscriptions and call {@code asyncClient.close()} while Handler A
     * is mid-settlement, reintroducing the original failure mode from issue #45716.
     * </p>
     */
    @Test
    public void v1ReentrantCloseWaitsForOtherConcurrentHandlers() throws InterruptedException {
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        // Two messages arrive on separate parallel rails, processed concurrently.
        when(asyncClient.receiveMessagesWithContext()).thenReturn(Flux.just(message1, message2)
            .map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        final CountDownLatch handler1Started = new CountDownLatch(1);
        final CountDownLatch handler1CanProceed = new CountDownLatch(1);
        final CountDownLatch handler2Started = new CountDownLatch(1);
        final AtomicBoolean handler1Completed = new AtomicBoolean(false);
        final AtomicReference<ServiceBusProcessorClient> processorRef = new AtomicReference<>();

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            if (messageContext.getMessage() == message1) {
                handler1Started.countDown();
                try {
                    handler1CanProceed.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                handler1Completed.set(true);
            } else {
                handler2Started.countDown();
                // Wait for handler1 to start before calling close(), ensuring both handlers
                // are running concurrently when the re-entrant close occurs.
                try {
                    handler1Started.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Re-entrant close from within a handler. The drain should wait for handler1
                // (the other concurrent handler) before proceeding to close the client.
                processorRef.get().close();
            }
        };

        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(2);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);
        processorRef.set(processorClient);

        processorClient.start();

        // Wait for both handlers to start.
        assertTrue(handler1Started.await(5, TimeUnit.SECONDS), "Handler1 should have started processing");
        assertTrue(handler2Started.await(5, TimeUnit.SECONDS), "Handler2 should have started processing");

        // Wait deterministically for handler2's re-entrant close() to take ownership and enter
        // drainV1Handlers(). close() sets isRunning=false inside its first synchronized block,
        // so once the predicate returns true we know the re-entrant close has progressed past
        // ownership acquisition.
        waitFor(() -> !processorClient.isRunning(), "handler2's re-entrant close() to have set isRunning=false");

        // Handler1 is still running, handler2 is blocked in close() waiting for handler1 to finish.
        verify(asyncClient, never()).close();
        assertFalse(handler1Completed.get(), "Handler1 should still be in-flight");

        // Release handler1.
        handler1CanProceed.countDown();

        // Handler2's close() should now complete (handler1 finished, drain threshold reached).
        verify(asyncClient, timeout(5000)).close();
        assertTrue(handler1Completed.get(), "Handler1 should have completed before client was closed");
    }

    /**
     * Verifies that the V1 closing flag prevents new message handlers from executing user
     * callback/settlement after {@code drainV1Handlers()} is triggered.
     * <p>
     * Race condition scenario: with {@code maxConcurrentCalls=1}, a single message is
     * in-flight when {@code close()} is called. {@code drainV1Handlers()} sets
     * {@code v1Closing = true} and waits for the handler. Meanwhile, the subscriber still
     * has an outstanding {@code request(1)}, so a second message can arrive via
     * {@code onNext} during the drain-to-cancel window. The closing flag ensures that
     * no user callback runs for messages arriving after shutdown begins.
     * </p>
     */
    @Test
    public void v1ClosingFlagPreventsNewHandlersAfterDrainStarts() throws InterruptedException {
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        // Emit message1 immediately, then message2 after a delay (simulating a message arriving
        // during the drain-to-cancel window).
        when(asyncClient.receiveMessagesWithContext()).thenReturn(Flux
            .concat(Flux.just(message1), Flux.just(message2).delayElements(Duration.ofMillis(300)),
                Flux.<ServiceBusReceivedMessage>never())
            .map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        final CountDownLatch handler1Started = new CountDownLatch(1);
        final CountDownLatch handler1CanProceed = new CountDownLatch(1);
        final AtomicBoolean handler2ProcessMessageInvoked = new AtomicBoolean(false);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            if (messageContext.getMessage() == message1) {
                handler1Started.countDown();
                try {
                    handler1CanProceed.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // If this executes, the V1 closing flag did NOT prevent the second handler.
                handler2ProcessMessageInvoked.set(true);
            }
        };

        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        processorClient.start();

        // Wait for handler1 to start processing.
        assertTrue(handler1Started.await(5, TimeUnit.SECONDS), "Handler1 should have started processing");

        // Close the processor on a separate thread. This sets v1Closing=true and drains handler1.
        final CountDownLatch closeDone = new CountDownLatch(1);
        final Thread closeThread = new Thread(() -> {
            processorClient.close();
            closeDone.countDown();
        });
        closeThread.start();

        try {
            // Wait deterministically for close() to enter drainV1Handlers and have set
            // v1Closing=true (close() sets isRunning=false inside its first synchronized block
            // before entering drain, so once the predicate returns true we know close has
            // taken ownership).
            waitFor(() -> !processorClient.isRunning(), "close() to have set isRunning=false");

            // Release handler1 so the drain completes. After drain returns, close() proceeds to
            // cancel subscriptions. Message2 may arrive in this window via the outstanding request(1).
            handler1CanProceed.countDown();

            // Wait for close to finish.
            assertTrue(closeDone.await(5, TimeUnit.SECONDS), "Close should complete");
        } finally {
            handler1CanProceed.countDown();
            closeThread.join(5000);
        }

        // The second message's handler should NOT have invoked processMessage because v1Closing was true.
        assertFalse(handler2ProcessMessageInvoked.get(),
            "Second handler should have been skipped by the V1 closing flag");
    }

    /**
     * Verifies that calling {@code start()} after {@code close()} resets the {@code v1Closing}
     * flag so that the processor can begin a new processing cycle. Without the reset, all
     * {@code onNext} calls would short-circuit and no messages would be processed.
     */
    @Test
    public void v1StartAfterCloseResetsClosingFlag() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient1 = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceiverAsyncClient asyncClient2 = mock(ServiceBusReceiverAsyncClient.class);

        // First call returns asyncClient1 (constructor), second returns asyncClient2 (restart after close).
        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient1, asyncClient2);

        for (ServiceBusReceiverAsyncClient client : new ServiceBusReceiverAsyncClient[] {
            asyncClient1,
            asyncClient2 }) {
            when(client.getFullyQualifiedNamespace()).thenReturn("FQDN");
            when(client.getEntityPath()).thenReturn("entityPath");
            when(client.isConnectionClosed()).thenReturn(false);
            when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
            when(client.getReceiverOptions()).thenReturn(peekLockOptions());
            doNothing().when(client).close();
        }

        // First cycle: emit nothing (just close immediately).
        when(asyncClient1.receiveMessagesWithContext()).thenReturn(
            Flux.<ServiceBusMessageContext>never().publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));

        // Second cycle: emit one message, then never complete.
        when(asyncClient2.receiveMessagesWithContext()).thenReturn(Flux.just(message)
            .map(ServiceBusMessageContext::new)
            .concatWith(Flux.<ServiceBusMessageContext>never())
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));

        final CountDownLatch messageProcessed = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            messageProcessed.countDown();
        };

        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        try {
            // First cycle: start then close (sets v1Closing=true during drain).
            processorClient.start();
            processorClient.close();

            // Second cycle: start again. If v1Closing is not reset, onNext will skip the handler.
            processorClient.start();

            // Verify the handler runs, proving v1Closing was reset.
            assertTrue(messageProcessed.await(5, TimeUnit.SECONDS),
                "Handler should run after restart, proving v1Closing was reset");
        } finally {
            processorClient.close();
        }
    }

    /**
     * Verifies that {@code close()} releases the {@code ServiceBusProcessorClient} instance monitor
     * while waiting for in-flight handlers to drain. If the monitor were held throughout the drain,
     * any handler that called a {@code synchronized} accessor on the same client (e.g.
     * {@link ServiceBusProcessorClient#isRunning()}, {@link ServiceBusProcessorClient#getIdentifier()})
     * would block on the monitor that {@code close()} holds while {@code close()} waits for that
     * handler's count to reach zero - a stalemate that resolves only when the drain timeout elapses.
     * Releasing the monitor across the drain wait lets the handler call those accessors freely,
     * complete, and decrement the in-flight counter so {@code close()} returns promptly.
     * <p>
     * Regression test for the deadlock concern raised on
     * <a href="https://github.com/Azure/azure-sdk-for-java/pull/48192#discussion_r3198124414">PR #48192</a>.
     * </p>
     */
    @Test
    public void v1CloseShouldNotHoldClientMonitorDuringDrain() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final Flux<ServiceBusReceivedMessage> messageFlux = Flux.concat(Flux.just(message), Flux.never());

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux.map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        // The handler will (1) signal that it has started, (2) wait until close() has been invoked
        // on a different thread, then (3) call isRunning() - which is synchronized on the same
        // monitor that close() acquires. Without the fix, that call blocks until the drain timeout
        // expires; with the fix, it returns immediately because close() released the monitor before
        // waiting for the drain.
        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch closeStarted = new CountDownLatch(1);
        final AtomicReference<Boolean> handlerSawIsRunning = new AtomicReference<>();
        final AtomicReference<ServiceBusProcessorClient> clientRef = new AtomicReference<>();

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                assertTrue(closeStarted.await(5, TimeUnit.SECONDS), "close() should have started");
                handlerSawIsRunning.set(clientRef.get().isRunning());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // Use the default 30-second drain timeout: a regression that re-introduces the monitor hold
        // would force close() to wait the full 30s, which is well beyond the 5s assertion below.
        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);
        clientRef.set(processorClient);

        processorClient.start();

        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started");

        final long startNanos = System.nanoTime();
        final CountDownLatch closeDone = new CountDownLatch(1);
        final Thread closeThread = new Thread(() -> {
            processorClient.close();
            closeDone.countDown();
        });
        closeThread.start();

        try {
            // Wait deterministically for close() to take ownership before signalling the handler
            // to call isRunning(). Polling avoids the flakiness of a fixed Thread.sleep on
            // slow/contended CI.
            waitFor(() -> !processorClient.isRunning(), "close() to have set isRunning=false");
            closeStarted.countDown();

            assertTrue(closeDone.await(5, TimeUnit.SECONDS),
                "close() should complete promptly after the handler returns. If it stalled until "
                    + "the 30s drain timeout, the instance monitor was held across the drain wait.");
            final Duration closeDuration = Duration.ofNanos(System.nanoTime() - startNanos);
            assertTrue(closeDuration.getSeconds() < 5,
                "close() took " + closeDuration + ", expected < 5s (drain timeout is 30s).");

            assertTrue(handlerSawIsRunning.get() != null, "Handler should have observed an isRunning() result");
            assertFalse(handlerSawIsRunning.get(),
                "isRunning() should return false because close() set isRunning=false before draining");
        } finally {
            closeStarted.countDown();
            closeThread.join(5000);
        }
    }

    /**
     * Verifies that a concurrent {@code start()} call during {@code close()}'s drain window is
     * ignored, so it does not create new resources that the in-progress {@code close()} would
     * immediately tear down.
     * <p>
     * Background: {@code close()} releases the client's instance monitor across the drain wait
     * (see {@link #v1CloseShouldNotHoldClientMonitorDuringDrain()}). Without an explicit
     * "close in progress" guard, a concurrent {@code start()} could acquire the monitor during
     * that window, reset {@code v1Closing}, create a fresh async client, and start the connection
     * monitor - only for {@code close()} to proceed into its cleanup phase and dispose those
     * brand-new resources, leaving the user with a processor that appears started but has no
     * working subscription.
     * </p>
     * <p>
     * Regression test for the lifecycle race raised on
     * <a href="https://github.com/Azure/azure-sdk-for-java/pull/48192#discussion_r3198194844">PR #48192</a>.
     * </p>
     */
    @Test
    public void v1ConcurrentStartDuringCloseDrainIsIgnored() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final Flux<ServiceBusReceivedMessage> messageFlux = Flux.concat(Flux.just(message), Flux.never());

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        // The builder should be invoked exactly once by the original start(); a concurrent start()
        // during close drain must NOT create a second receiver.
        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux.map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch handlerCanProceed = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                handlerCanProceed.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        processorClient.start();
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started");

        // Run close() on a separate thread - it will block in the drain wait until the handler
        // returns (handlerCanProceed has not yet been counted down).
        final CountDownLatch closeDone = new CountDownLatch(1);
        final Thread closeThread = new Thread(() -> {
            processorClient.close();
            closeDone.countDown();
        });
        closeThread.start();

        try {
            // Wait deterministically for close() to take ownership (sets isRunning=false then
            // v1CloseInProgress=true inside its first synchronized block). Polling isRunning()
            // avoids the flakiness of a fixed Thread.sleep on slow/contended CI.
            waitFor(() -> !processorClient.isRunning(), "close() to have set isRunning=false");

            // Concurrent start() during the drain window. Without the v1CloseInProgress guard,
            // this would create a new receiver and mark the processor running again, only for
            // close() to subsequently dispose it. With the guard, start() returns without taking
            // any action.
            processorClient.start();

            // The receiver builder should still have been invoked exactly once - the concurrent
            // start() must not have created a second client.
            verify(receiverBuilder, Mockito.times(1)).buildAsyncClientForProcessor();
            // The processor must not be reported as running while close is mid-shutdown.
            assertFalse(processorClient.isRunning(), "Processor should not be running during close()'s drain window");

            // Allow the original handler to complete so close() can finish.
            handlerCanProceed.countDown();
            assertTrue(closeDone.await(5, TimeUnit.SECONDS), "close() should complete");

            // After close() returns, isRunning should still be false and no extra receiver was created.
            assertFalse(processorClient.isRunning(), "Processor should be stopped after close()");
            verify(receiverBuilder, Mockito.times(1)).buildAsyncClientForProcessor();
            // Original asyncClient was closed exactly once by close().
            verify(asyncClient, timeout(2000)).close();
        } finally {
            handlerCanProceed.countDown();
            closeThread.join(5000);
        }
    }

    /**
     * Verifies that {@code getIdentifier()} does not lazy-create a fresh receiver during or after
     * {@code close()}, so it cannot leak a receiver that the shutdown path is no longer responsible
     * for disposing.
     * <p>
     * Background: {@code close()} releases the instance monitor across the drain wait. The original
     * {@code getIdentifier()} would lazy-create a new receiver whenever {@code asyncClient} was
     * {@code null}, so a concurrent {@code getIdentifier()} call after {@code close()} nulled the
     * client (or after {@code close()} returned) would invoke the receiver builder again and leave
     * a fresh, unmanaged client behind. The fix caches the identifier whenever it is observed and
     * once more in {@code close()} before nulling the client, so {@code getIdentifier()} can return
     * a stable value without creating a receiver during/after shutdown.
     * </p>
     * <p>
     * Regression test for the lazy-receiver leak raised on
     * <a href="https://github.com/Azure/azure-sdk-for-java/pull/48192#discussion_r3198250076">PR #48192</a>.
     * </p>
     */
    @Test
    public void v1GetIdentifierDuringAndAfterCloseDoesNotCreateNewReceiver() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final Flux<ServiceBusReceivedMessage> messageFlux = Flux.concat(Flux.just(message), Flux.never());

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        // Builder must be invoked exactly once - by the original start(). Neither getIdentifier()
        // during the drain window nor after close() returned may trigger a second invocation.
        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        when(asyncClient.getIdentifier()).thenReturn("processor-id-1");
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux.map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch handlerCanProceed = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                handlerCanProceed.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        processorClient.start();
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started");

        // Confirm getIdentifier() works while running (also seeds the cache).
        assertTrue("processor-id-1".equals(processorClient.getIdentifier()),
            "getIdentifier() should return the live client's identifier while running");

        // Begin close on a separate thread; it will block in drainV1Handlers().
        final CountDownLatch closeDone = new CountDownLatch(1);
        final Thread closeThread = new Thread(() -> {
            processorClient.close();
            closeDone.countDown();
        });
        closeThread.start();

        try {
            // Wait deterministically for close() to enter the drain window. Polling avoids the
            // flakiness of a fixed Thread.sleep on slow/contended CI.
            waitFor(() -> !processorClient.isRunning(), "close() to have set isRunning=false");

            // Concurrent getIdentifier() during the drain window. Must NOT create a new receiver
            // (cachedV1Identifier was seeded by the call above; before fix, it would have hit the
            // synchronized monitor and stalled; with monitor released and no cache, would have
            // lazily created a receiver).
            assertTrue("processor-id-1".equals(processorClient.getIdentifier()),
                "getIdentifier() during close drain should return the cached identifier");

            // Allow the handler to complete so close() can finish.
            handlerCanProceed.countDown();
            assertTrue(closeDone.await(5, TimeUnit.SECONDS), "close() should complete");

            // After close(), getIdentifier() must still return the cached identifier and must not
            // create a new receiver via the builder.
            assertTrue("processor-id-1".equals(processorClient.getIdentifier()),
                "getIdentifier() after close should return the cached identifier");

            // Builder was invoked exactly once - no leaked receiver from any getIdentifier() call.
            verify(receiverBuilder, Mockito.times(1)).buildAsyncClientForProcessor();
        } finally {
            handlerCanProceed.countDown();
            closeThread.join(5000);
        }
    }

    /**
     * Verifies that concurrent {@code close()} calls do not race: only the first call performs
     * the V1 cleanup; the others return early so they cannot dispose state created after the
     * first call cleared {@code v1CloseInProgress}.
     * <p>
     * Background: Without ownership, two concurrent {@code close()} calls would both proceed
     * through drain + cleanup. The first to finish would clear the flag and let a concurrent
     * {@code start()} create new resources, which the still-running second {@code close()} would
     * then dispose. The fix uses {@code v1CloseInProgress.compareAndSet(false, true)} so only one
     * close call wins ownership; the others return immediately.
     * </p>
     * <p>
     * Regression test for the concurrent-close race raised on
     * <a href="https://github.com/Azure/azure-sdk-for-java/pull/48192#discussion_r3198281771">PR #48192</a>.
     * </p>
     */
    @Test
    public void v1ConcurrentCloseCallsDoNotRace() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final Flux<ServiceBusReceivedMessage> messageFlux = Flux.concat(Flux.just(message), Flux.never());

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        when(asyncClient.getIdentifier()).thenReturn("processor-id");
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux.map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch handlerCanProceed = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                handlerCanProceed.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        processorClient.start();
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started");

        // First close() owns the V1 shutdown - it will block until the handler returns.
        final CountDownLatch firstCloseDone = new CountDownLatch(1);
        final Thread firstCloseThread = new Thread(() -> {
            processorClient.close();
            firstCloseDone.countDown();
        });
        firstCloseThread.start();

        // Wait deterministically for the first close() to take ownership (sets isRunning=false
        // then v1CloseInProgress=true). Polling isRunning() avoids the flakiness of a fixed
        // Thread.sleep on slow/contended CI - if the first close thread were delayed, the second
        // close could win ownership and block on the drain, failing the immediate-return
        // assertion below.
        waitFor(() -> !processorClient.isRunning(), "first close() to have taken ownership");

        // Second close() while the first is still draining - must return immediately because the
        // first close owns the shutdown. We bound it at 1 second to catch a regression where the
        // second close also waits on the drain (would take the full 30s drain timeout if it waited
        // for the handler that the test deliberately keeps running).
        final long secondCloseStart = System.nanoTime();
        final CountDownLatch secondCloseDone = new CountDownLatch(1);
        final Thread secondCloseThread = new Thread(() -> {
            processorClient.close();
            secondCloseDone.countDown();
        });
        secondCloseThread.start();

        try {
            assertTrue(secondCloseDone.await(1, TimeUnit.SECONDS),
                "Second close() should return immediately when another close() owns the shutdown.");
            final Duration secondDuration = Duration.ofNanos(System.nanoTime() - secondCloseStart);
            assertTrue(secondDuration.toMillis() < 500,
                "Second close() took " + secondDuration + ", expected immediate return.");

            // The first close() is still blocked on the handler. Verify the asyncClient has NOT
            // been closed yet - the second close() must not have torn things down.
            verify(asyncClient, never()).close();

            // Allow the handler to complete so the first close() can finish.
            handlerCanProceed.countDown();
            assertTrue(firstCloseDone.await(5, TimeUnit.SECONDS), "First close() should complete");

            // The asyncClient is closed exactly once - by the first (owning) close() during cleanup.
            verify(asyncClient, timeout(2000).times(1)).close();
        } finally {
            handlerCanProceed.countDown();
            firstCloseThread.join(5000);
            secondCloseThread.join(5000);
        }
    }

    /**
     * Verifies that a concurrent {@code start()} call during a V2 {@code close()}'s drain window
     * is ignored, mirroring the V1 guarantee in {@link #v1ConcurrentStartDuringCloseDrainIsIgnored()}.
     * <p>
     * Background: {@code close()} releases the outer {@code ServiceBusProcessorClient} monitor
     * before delegating to {@code processorV2.close()} (whose internal drain blocks for in-flight
     * handler settlement). Without an explicit {@code v2CloseInProgress} guard, a concurrent
     * {@code start()} could acquire the outer monitor during the drain window and call
     * {@code processorV2.start()}, leaving the inner processor running after the outer
     * {@code close()} returns - even though the caller observed {@code close()} returning
     * successfully.
     * </p>
     * <p>
     * Regression test for the V2 lifecycle race raised on
     * <a href="https://github.com/Azure/azure-sdk-for-java/pull/48192#discussion_r3198481760">PR #48192</a>.
     * </p>
     */
    @Test
    public void v2ConcurrentStartDuringCloseDrainIsIgnored() throws InterruptedException {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        // Builder must be invoked exactly once - by the original start(). A concurrent start()
        // during V2 close drain must NOT invoke the builder a second time.
        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(asyncClient.getReceiverOptions()).thenReturn(peekLockOptions());
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.isAutoLockRenewRequested()).thenReturn(false);
        when(asyncClient.complete(any())).thenReturn(Mono.empty());
        // Emit one message then hang so the V2 drain has something to wait for.
        when(asyncClient.nonSessionProcessorReceiveV2())
            .thenReturn(Flux.concat(Flux.just(message), Flux.<ServiceBusReceivedMessage>never())
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        final CountDownLatch handlerStarted = new CountDownLatch(1);
        final CountDownLatch handlerCanProceed = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            handlerStarted.countDown();
            try {
                handlerCanProceed.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // V2 path: setV2(true) so the outer ServiceBusProcessorClient delegates to a real
        // ServiceBusProcessor (RollingMessagePump under the hood).
        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1).setV2(true);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        processorClient.start();
        assertTrue(handlerStarted.await(5, TimeUnit.SECONDS), "Handler should have started");

        // Begin V2 close on a separate thread; it will block in processorV2.close()'s drain
        // until the handler returns.
        final CountDownLatch closeDone = new CountDownLatch(1);
        final Thread closeThread = new Thread(() -> {
            processorClient.close();
            closeDone.countDown();
        });
        closeThread.start();

        try {
            // Wait deterministically for V2 close to be in-progress. Polling isRunning() is the
            // closest observable signal: processorV2's close() sets its internal isRunning=false
            // before draining, and our outer v2CloseInProgress flag is set in the same critical
            // section that captures processorV2 for the close call.
            waitFor(() -> !processorClient.isRunning(), "V2 close() to be in progress");

            // Concurrent start() during the V2 drain window. Without the v2CloseInProgress guard,
            // this would invoke processorV2.start() and create a new RollingMessagePump that the
            // outer close() can't track; with the guard, start() returns without taking action.
            processorClient.start();

            // The receiver builder should still have been invoked exactly once - the concurrent
            // start() must not have created a second client behind the in-flight close().
            verify(receiverBuilder, Mockito.times(1)).buildAsyncClientForProcessor();
            assertFalse(processorClient.isRunning(),
                "Processor should not report running while V2 close()'s drain is in progress");

            // Allow the handler to complete so V2 close() can finish.
            handlerCanProceed.countDown();
            assertTrue(closeDone.await(5, TimeUnit.SECONDS), "V2 close() should complete");

            // After close() returns, no extra receivers were created and the processor remains stopped.
            assertFalse(processorClient.isRunning(), "Processor should be stopped after V2 close()");
            verify(receiverBuilder, Mockito.times(1)).buildAsyncClientForProcessor();
        } finally {
            handlerCanProceed.countDown();
            closeThread.join(5000);
        }
    }

    /**
     * Verifies that in {@code RECEIVE_AND_DELETE} mode the V2 pump's drain skip-path does NOT
     * drop messages: every message that arrives during the drain window must still reach
     * {@code processMessage}.
     * <p>
     * Background: the broker settles RECEIVE_AND_DELETE messages on delivery, so any message
     * already in the pipeline when {@code drainHandlers()} sets {@code closing=true} has been
     * removed from the entity. Skipping the user callback for those messages would lose them
     * permanently. The fix gates the skip on PEEK_LOCK only.
     * </p>
     * <p>
     * Regression test for the data-loss concern raised on
     * <a href="https://github.com/Azure/azure-sdk-for-java/pull/48192#discussion_r3204430541">PR #48192</a>.
     * </p>
     */
    @Test
    public void v2ReceiveAndDeleteModeDoesNotSkipDuringDrain() throws InterruptedException {
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
        // RECEIVE_AND_DELETE - production code must NOT take the drain skip-path on this client.
        when(client.getReceiverOptions()).thenReturn(
            ReceiverOptions.createNonSessionOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1, null, false));
        when(client.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(client.getEntityPath()).thenReturn("entityPath");
        when(client.isConnectionClosed()).thenReturn(false);
        when(client.isAutoLockRenewRequested()).thenReturn(false);

        final reactor.core.publisher.Sinks.Many<ServiceBusReceivedMessage> messageSink
            = reactor.core.publisher.Sinks.many().unicast().onBackpressureBuffer();
        when(client.nonSessionProcessorReceiveV2())
            .thenReturn(messageSink.asFlux().publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(client).close();

        final CountDownLatch handler1Started = new CountDownLatch(1);
        final CountDownLatch handler1CanProceed = new CountDownLatch(1);
        final CountDownLatch handler2Invoked = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            if (messageContext.getMessage() == message1) {
                handler1Started.countDown();
                try {
                    handler1CanProceed.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // RECEIVE_AND_DELETE: this MUST be invoked even while drain is in progress,
                // otherwise message2 is permanently lost.
                handler2Invoked.countDown();
            }
        };

        // enableAutoDisposition=false: matches RECEIVE_AND_DELETE semantics (no settlement).
        // concurrency=2 lets flatMap dispatch both handlers in parallel.
        final MessagePump pump = new MessagePump(client, messageConsumer, e -> {
        }, 2, false);
        final AtomicReference<reactor.core.Disposable> subscription = new AtomicReference<>();
        subscription.set(pump.begin().subscribe());

        messageSink.tryEmitNext(message1);
        assertTrue(handler1Started.await(5, TimeUnit.SECONDS), "Handler1 should have started");

        // Start drain on a separate thread - it sets closing=true and waits for handler1.
        final CountDownLatch drainDone = new CountDownLatch(1);
        final Thread drainThread = new Thread(() -> {
            pump.drainHandlers(Duration.ofSeconds(10));
            drainDone.countDown();
        });
        drainThread.start();

        // Wait deterministically for drainHandlers() to enter its wait loop (closing=true is set
        // before the wait begins, so once the thread is parked we know the flag is observable).
        waitFor(
            () -> drainThread.getState() == Thread.State.WAITING
                || drainThread.getState() == Thread.State.TIMED_WAITING,
            "drainHandlers() to enter waiting state (closing flag set)");

        // Emit message2 AFTER closing=true. In PEEK_LOCK this would be skipped; in
        // RECEIVE_AND_DELETE it MUST be delivered to handler2 because the broker has already
        // settled it.
        messageSink.tryEmitNext(message2);

        assertTrue(handler2Invoked.await(5, TimeUnit.SECONDS),
            "Handler2 must run during RECEIVE_AND_DELETE drain - skipping it would lose the message permanently");

        // Release handler1 so drain can complete.
        handler1CanProceed.countDown();
        assertTrue(drainDone.await(5, TimeUnit.SECONDS), "Drain should complete");
        subscription.get().dispose();
    }

    /**
     * Verifies that in {@code RECEIVE_AND_DELETE} mode the V1 processor's drain skip-path does
     * NOT drop messages: every message that arrives during the drain window must still reach
     * {@code processMessage}. Mirrors the V2 guarantee in
     * {@link #v2ReceiveAndDeleteModeDoesNotSkipDuringDrain()}.
     * <p>
     * Regression test for the data-loss concern raised on
     * <a href="https://github.com/Azure/azure-sdk-for-java/pull/48192#discussion_r3204430650">PR #48192</a>.
     * </p>
     */
    @Test
    public void v1ReceiveAndDeleteModeDoesNotSkipDuringDrain() throws InterruptedException {
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);

        final ServiceBusReceiverClientBuilder receiverBuilder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(asyncClient.getEntityPath()).thenReturn("entityPath");
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        // RECEIVE_AND_DELETE - V1 onNext must NOT take the v1Closing skip-path on this client.
        when(asyncClient.getReceiverOptions()).thenReturn(
            ReceiverOptions.createNonSessionOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1, null, false));

        final reactor.core.publisher.Sinks.Many<ServiceBusReceivedMessage> messageSink
            = reactor.core.publisher.Sinks.many().unicast().onBackpressureBuffer();
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageSink.asFlux()
            .map(ServiceBusMessageContext::new)
            .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));
        doNothing().when(asyncClient).close();

        final CountDownLatch handler1Started = new CountDownLatch(1);
        final CountDownLatch handler1CanProceed = new CountDownLatch(1);
        final CountDownLatch handler2Invoked = new CountDownLatch(1);

        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            if (messageContext.getMessage() == message1) {
                handler1Started.countDown();
                try {
                    handler1CanProceed.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                handler2Invoked.countDown();
            }
        };

        // maxConcurrentCalls=2 so V1 uses the parallel/runOn path (concurrency=1 takes the
        // single-subscriber path that only request(1)s after each handler completes, which would
        // not deliver message2 because isRunning becomes false during close).
        final ServiceBusProcessorClientOptions options
            = new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(2);
        final ServiceBusProcessorClient processorClient
            = new ServiceBusProcessorClient(receiverBuilder, "entityPath", null, null, messageConsumer, error -> {
            }, options);

        processorClient.start();
        messageSink.tryEmitNext(message1);
        assertTrue(handler1Started.await(5, TimeUnit.SECONDS), "Handler1 should have started");

        // Begin close on a separate thread. close() will set v1Closing=true and block on the drain.
        final CountDownLatch closeDone = new CountDownLatch(1);
        final Thread closeThread = new Thread(() -> {
            processorClient.close();
            closeDone.countDown();
        });
        closeThread.start();

        try {
            // Wait deterministically for close() to take ownership and set v1Closing=true.
            waitFor(() -> !processorClient.isRunning(), "close() to have set isRunning=false");

            // Emit message2 during the drain window. In PEEK_LOCK this would be skipped; in
            // RECEIVE_AND_DELETE the broker has already removed the message, so V1 onNext MUST
            // dispatch it to handler2.
            messageSink.tryEmitNext(message2);

            assertTrue(handler2Invoked.await(5, TimeUnit.SECONDS),
                "Handler2 must run during RECEIVE_AND_DELETE drain - skipping it would lose the message permanently");

            // Release handler1 so close() can finish.
            handler1CanProceed.countDown();
            assertTrue(closeDone.await(5, TimeUnit.SECONDS), "close() should complete");
        } finally {
            handler1CanProceed.countDown();
            closeThread.join(5000);
        }
    }
}
