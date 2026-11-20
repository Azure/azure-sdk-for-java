// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.PUMP_ID_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.CONCURRENCY_PER_CORE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.CORES_VS_CONCURRENCY_MESSAGE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * Abstraction to pump messages using a {@link ServiceBusReceiverAsyncClient} (associated with a session unaware entity)
 * as long as the client is healthy.
 *
 * <p>The pumping starts upon subscribing to the Mono that {@link MessagePump#begin} returns. The pumping can
 * be stopped by cancelling the subscription that {@link MessagePump#begin} returned.</p>
 *
 * <p>Once the Mono from {@link MessagePump#begin} terminates, to restart the pumping a new {@link MessagePump}
 * instance should be obtained.</p>
 *
 * <p>The abstraction {@link ServiceBusProcessor} takes care of managing a {@link MessagePump} and obtaining
 * the next MessagePump when current one terminates.</p>
 */
final class MessagePump {
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final Duration CONNECTION_STATE_POLL_INTERVAL = Duration.ofSeconds(20);
    private final long pumpId;
    private final ServiceBusReceiverAsyncClient client;
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final ClientLogger logger;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final int concurrency;
    private final boolean enableAutoDisposition;
    private final boolean enableAutoLockRenew;
    private final Scheduler workerScheduler;
    private final ServiceBusReceiverInstrumentation instrumentation;
    private final AtomicInteger activeHandlerCount = new AtomicInteger(0);
    private final Object drainLock = new Object();
    private final ThreadLocal<Boolean> isHandlerThread = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private volatile boolean closing;
    // True when the receive mode is PEEK_LOCK, in which case it is safe to skip handler dispatch
    // for messages that arrive after closing=true (the broker still owns the lock and will
    // redeliver). False for RECEIVE_AND_DELETE, where the broker has already removed the message
    // before delivery - skipping the handler in that mode would lose the message permanently, so
    // we must always invoke processMessage even during the drain window.
    private final boolean skipDuringDrain;

    /**
     * Instantiate {@link MessagePump} that pumps messages emitted by the given {@code client}. The messages
     * are pumped to the {@code processMessage} concurrently with the parallelism equal to {@code concurrency}.
     *
     * @param client The underlying client to read messages from the broker.
     * @param processMessage The consumer that the pump should invoke for each message.
     * @param processError The consumer that the pump should report the errors.
     * @param concurrency The pumping concurrency, i.e., how many invocations of {@code processMessage} should happen in parallel.
     * @param enableAutoDisposition Indicate if auto-complete or abandon should be enabled.
     */
    MessagePump(ServiceBusReceiverAsyncClient client, Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError, int concurrency, boolean enableAutoDisposition) {
        this.pumpId = COUNTER.incrementAndGet();
        this.client = client;
        this.fullyQualifiedNamespace = this.client.getFullyQualifiedNamespace();
        this.entityPath = this.client.getEntityPath();

        final Map<String, Object> loggingContext = new HashMap<>(3);
        loggingContext.put(PUMP_ID_KEY, this.pumpId);
        loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, this.fullyQualifiedNamespace);
        loggingContext.put(ENTITY_PATH_KEY, this.entityPath);
        this.logger = new ClientLogger(MessagePump.class, loggingContext);

        this.processMessage = processMessage;
        this.processError = processError;
        this.concurrency = concurrency;
        this.enableAutoDisposition = enableAutoDisposition;
        this.enableAutoLockRenew = client.isAutoLockRenewRequested();
        // Cached at construction so the hot path (handleMessage) reads a primitive instead of
        // walking the receiver options each call. PEEK_LOCK is safe to skip during drain (broker
        // redelivers); RECEIVE_AND_DELETE is not (message would be lost). When the client cannot
        // report a receive mode (test mocks that didn't stub getReceiverOptions()) default to the
        // safer no-skip behavior so messages cannot be dropped silently.
        final ReceiverOptions options = client.getReceiverOptions();
        this.skipDuringDrain = options != null && options.getReceiveMode() == ServiceBusReceiveMode.PEEK_LOCK;
        if (concurrency > 1) {
            this.workerScheduler = Schedulers.boundedElastic();
        } else {
            // For the max-concurrent-calls == 1 (the default) case, the message handler can be invoked in the same
            // BoundedElastic thread that the ServiceBusReactorReceiver::receive() API (backing the MessageFlux)
            // publishes the message. In this case, we use 'Schedulers.immediate' to avoid the thread switch that
            // is only needed for higher parallelism (max-concurrent-calls > 1) case.
            this.workerScheduler = Schedulers.immediate();
        }
        this.instrumentation = this.client.getInstrumentation();
    }

    /**
     * Begin pumping messages in parallel, with the parallelism equal to the configured {@code concurrency}.
     *
     * @return a mono that emits {@link MessagePumpTerminatedException} when this {@link MessagePump} terminates.
     * The pumping terminates when the underlying client encounters a non-retriable error, the retries exhaust,
     * or rejection when scheduling concurrently.
     */
    Mono<Void> begin() {
        logCPUResourcesConcurrencyMismatch();
        final Mono<Void> terminatePumping = pollConnectionState();
        final Mono<Void> pumping = client.nonSessionProcessorReceiveV2()
            .flatMap(new RunOnWorker(this::handleMessage, workerScheduler), concurrency, 1)
            .then();

        final Mono<Void> pumpingMessages = Mono.firstWithSignal(pumping, terminatePumping);

        return pumpingMessages.onErrorMap(e -> {
            if (e instanceof MessagePumpTerminatedException) {
                // Source of 'e': pollConnectionState.
                return e;
            }
            // 'e' propagated from client.nonSessionProcessorReceiveV2.
            return new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "pumping#error-map",
                e);
        })
            .then(Mono.error(
                () -> MessagePumpTerminatedException.forCompletion(pumpId, fullyQualifiedNamespace, entityPath)));
    }

    private Mono<Void> pollConnectionState() {
        return Flux.interval(CONNECTION_STATE_POLL_INTERVAL).handle((ignored, sink) -> {
            if (client.isConnectionClosed()) {
                final RuntimeException e = logger.atInfo()
                    .log(new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath,
                        "non-session#connection-state-poll"));
                sink.error(e);
            }
        }).then();
    }

    private void handleMessage(ServiceBusReceivedMessage message) {
        // Fast-path early return: avoid counting skip-path invocations against the drain. Under
        // sustained throughput with concurrency > 1, the upstream subscription is still live
        // while drainHandlers() is waiting (the subscription is only disposed after drain returns),
        // so flatMap keeps dispatching messages. If we incremented the counter for every skip
        // we could keep activeHandlerCount > 0 long enough to push drain to its timeout. Reading
        // the volatile flag here ensures messages that arrive after closing=true is set are
        // dropped without touching the drain's exit condition.
        //
        // Skip is gated on PEEK_LOCK only: in that mode the broker still owns the lock and will
        // redeliver any message we drop. In RECEIVE_AND_DELETE, the broker has already removed
        // the message before delivery, so dropping it here would lose it permanently - those
        // messages must always reach processMessage even during the drain window.
        if (closing && skipDuringDrain) {
            logger.atVerbose().log("Skipping handler execution (early), pump is closing.");
            return;
        }
        activeHandlerCount.incrementAndGet();
        isHandlerThread.set(Boolean.TRUE);
        try {
            // closing may have flipped between the early check above and this point
            // (a check-then-act race). Re-check inside the counted region so the rare race-loser
            // still skips work; the increment will be balanced by the decrement in finally and
            // notifyAll the drain. Same RECEIVE_AND_DELETE exemption applies.
            if (closing && skipDuringDrain) {
                logger.atVerbose().log("Skipping handler execution, pump is closing.");
                return;
            }
            instrumentation.instrumentProcess(message, ReceiverKind.PROCESSOR, msg -> {
                final Disposable lockRenewDisposable;
                if (enableAutoLockRenew) {
                    lockRenewDisposable = client.beginLockRenewal(message);
                } else {
                    lockRenewDisposable = Disposables.disposed();
                }
                final Throwable error = notifyMessage(message);
                if (enableAutoDisposition) {
                    if (error == null) {
                        complete(message);
                    } else {
                        abandon(message);
                    }
                }
                lockRenewDisposable.dispose();
                return error;
            });
        } finally {
            isHandlerThread.remove();
            if (activeHandlerCount.decrementAndGet() <= 1) {
                synchronized (drainLock) {
                    drainLock.notifyAll();
                }
            }
        }
    }

    private Throwable notifyMessage(ServiceBusReceivedMessage message) {
        try {
            processMessage.accept(new ServiceBusReceivedMessageContext(client, new ServiceBusMessageContext(message)));
        } catch (Exception e) {
            notifyError(new ServiceBusException(e, ServiceBusErrorSource.USER_CALLBACK));
            return e;
        }

        return null;
    }

    private void notifyError(Throwable throwable) {
        try {
            processError.accept(new ServiceBusErrorContext(throwable, fullyQualifiedNamespace, entityPath));
        } catch (Exception e) {
            logger.atVerbose().log("Ignoring error from user processError handler.", e);
        }
    }

    private void complete(ServiceBusReceivedMessage message) {
        try {
            client.complete(message).block();
        } catch (Exception e) {
            logger.atVerbose().log("Failed to complete message", e);
        }
    }

    private void abandon(ServiceBusReceivedMessage message) {
        try {
            client.abandon(message).block();
        } catch (Exception e) {
            logger.atVerbose().log("Failed to abandon message", e);
        }
    }

    /**
     * Wait for in-flight message handlers to complete, up to the specified timeout.
     * This is called during processor close to ensure graceful shutdown — messages currently
     * being processed are allowed to complete (including settlement) before the underlying client
     * is disposed.
     *
     * <p><strong>Re-entrant semantics:</strong> when invoked from within a message handler
     * (i.e. the calling thread is the handler thread itself), this method waits only for
     * <em>other</em> concurrent handlers to complete and excludes the calling handler from the
     * wait condition - waiting for the calling handler to finish would self-deadlock. In that
     * case, this method may return {@code true} while the calling handler is still executing.</p>
     *
     * @param timeout the maximum time to wait for in-flight handlers to complete.
     * @return {@code true} if all in-flight handlers (excluding the calling handler on the
     *     re-entrant path) completed within the timeout, {@code false} otherwise.
     */
    boolean drainHandlers(Duration timeout) {
        closing = true;
        final int threshold;
        if (isHandlerThread.get()) {
            // Re-entrant call from within a message handler (e.g., user called close() inside processMessage).
            // Cannot wait for this thread's own handler to complete (would self-deadlock), but we can
            // wait for OTHER concurrent handlers to finish settlement before the underlying client is disposed.
            threshold = 1;
            if (activeHandlerCount.get() <= threshold) {
                return true;
            }
            logger.atInfo()
                .addKeyValue("otherActiveHandlers", activeHandlerCount.get() - 1)
                .log("drainHandlers called from within a message handler (re-entrant). "
                    + "Waiting for other active handlers to complete.");
        } else {
            threshold = 0;
        }
        final long deadline = System.nanoTime() + timeout.toNanos();
        synchronized (drainLock) {
            while (activeHandlerCount.get() > threshold) {
                final long remainingNanos = deadline - System.nanoTime();
                if (remainingNanos <= 0) {
                    logger.atWarning()
                        .addKeyValue("activeHandlers", activeHandlerCount.get())
                        .log("Drain timeout expired with active handlers still running.");
                    return false;
                }
                try {
                    final long millis = TimeUnit.NANOSECONDS.toMillis(remainingNanos);
                    final int nanos = (int) (remainingNanos % 1_000_000);
                    drainLock.wait(millis, nanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.atWarning().log("Drain interrupted while waiting for in-flight handlers.");
                    return false;
                }
            }
        }
        return true;
    }

    private void logCPUResourcesConcurrencyMismatch() {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int poolSize = DEFAULT_BOUNDED_ELASTIC_SIZE;
        if (concurrency > poolSize || concurrency > CONCURRENCY_PER_CORE * cores) {
            logger.atWarning().log(CORES_VS_CONCURRENCY_MESSAGE, poolSize, cores, concurrency);
        }
    }

    /**
     * A Function that when called, invokes {@link ServiceBusReceivedMessage} handler using a Worker.
     */
    private static final class RunOnWorker implements Function<ServiceBusReceivedMessage, Publisher<Void>> {
        private final Consumer<ServiceBusReceivedMessage> handleMessage;
        private final Scheduler workerScheduler;

        /**
         * Instantiate {@link RunOnWorker} to run the given {@code handleMessage} handler using a Worker
         * from the provided {@code workerScheduler}.
         *
         * @param handleMessage The message handler.
         * @param workerScheduler The Scheduler hosting the Worker to run the message handler.
         */
        RunOnWorker(Consumer<ServiceBusReceivedMessage> handleMessage, Scheduler workerScheduler) {
            this.handleMessage = handleMessage;
            this.workerScheduler = workerScheduler;
        }

        @Override
        public Mono<Void> apply(ServiceBusReceivedMessage message) {
            return Mono.<Void>fromRunnable(() -> {
                handleMessage.accept(message);
            }).subscribeOn(workerScheduler);
            // The subscribeOn offloads message handling to a Worker from the Scheduler.
        }
    }
}
