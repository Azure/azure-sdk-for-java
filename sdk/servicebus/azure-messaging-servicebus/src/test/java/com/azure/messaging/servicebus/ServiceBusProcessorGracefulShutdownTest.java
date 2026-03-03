// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessor.RollingMessagePump;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
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
        }, 1, true);

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
            // Give dispose a moment to start; it should be blocked in drainHandlers().
            Thread.sleep(200);

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
            // Give close a moment to start; it should be blocked in drainV1Handlers().
            Thread.sleep(200);

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
        when(client.getFullyQualifiedNamespace()).thenReturn("FQDN");
        when(client.getEntityPath()).thenReturn("entityPath");
        when(client.isConnectionClosed()).thenReturn(false);
        when(client.isAutoLockRenewRequested()).thenReturn(false);
        when(client.complete(any())).thenReturn(Mono.empty());
        doNothing().when(client).close();

        // Emit message1 immediately, then message2 after a short delay (to ensure message1's handler starts first).
        // Use concurrency=2 so flatMap can dispatch both handlers concurrently.
        when(client.nonSessionProcessorReceiveV2())
            .thenReturn(Flux
                .concat(Flux.just(message1), Flux.just(message2).delayElements(Duration.ofMillis(200)),
                    Flux.<ServiceBusReceivedMessage>never())
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic()));

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

        // Wait for handler1 to start.
        assertTrue(handler1Started.await(5, TimeUnit.SECONDS), "Handler1 should have started processing");

        // Call drainHandlers on a separate thread. This sets closing=true and waits for handler1.
        final CountDownLatch drainDone = new CountDownLatch(1);
        final AtomicBoolean drainResult = new AtomicBoolean(false);
        final Thread drainThread = new Thread(() -> {
            drainResult.set(pump.drainHandlers(Duration.ofSeconds(10)));
            drainDone.countDown();
        });
        drainThread.start();

        // Give time for drain to start (sets closing=true) and for message2 to arrive.
        Thread.sleep(500);

        // Release handler1 so the drain can complete.
        handler1CanProceed.countDown();

        // Wait for drain to finish.
        assertTrue(drainDone.await(5, TimeUnit.SECONDS), "Drain should complete after handler1 finishes");
        assertTrue(drainResult.get(), "Drain should return true (all handlers completed)");

        // The second message's handler should NOT have invoked processMessage because closing was true.
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

        // Give handler2's close() a moment to enter drainV1Handlers and start waiting.
        Thread.sleep(300);

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
            // Give close a moment to enter drainV1Handlers (sets v1Closing=true).
            Thread.sleep(200);

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
}
