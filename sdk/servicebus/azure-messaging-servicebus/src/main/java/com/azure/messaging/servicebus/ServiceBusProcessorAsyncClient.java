// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * The asynchronous processor client for processing Service Bus messages with reactive, non-blocking message handlers.
 * {@link ServiceBusProcessorAsyncClient} provides the same push-based, auto-recovering, concurrency-managed message
 * pump as {@link ServiceBusProcessorClient}, but the message and error handlers return a {@link Mono} instead of
 * running as blocking {@link java.util.function.Consumer Consumer} callbacks.
 *
 * <p>This is the async counterpart to {@link ServiceBusProcessorClient}, completing the family of asynchronous
 * clients ({@link ServiceBusSenderAsyncClient}, {@link ServiceBusReceiverAsyncClient}). It targets applications
 * that perform I/O-bound work in the handler (for example, reactive HTTP calls, non-blocking database writes, or
 * further messaging) and want to compose that work reactively without blocking a processing thread for the duration
 * of each message, and without re-implementing auto-recovery, concurrency management, and lifecycle handling on top
 * of {@link ServiceBusReceiverAsyncClient}.</p>
 *
 * <p>Messages are dispatched to the handler with up to {@code maxConcurrentCalls} concurrent invocations in flight;
 * a new message is requested from the broker only as an in-flight handler completes. By default a message whose
 * handler {@link Mono} completes successfully is {@link ServiceBusReceivedMessageContext#complete() completed}, and a
 * message whose handler {@link Mono} signals an error is {@link ServiceBusReceivedMessageContext#abandon() abandoned};
 * this auto-settlement can be disabled via
 * {@link ServiceBusClientBuilder.ServiceBusProcessorAsyncClientBuilder#disableAutoComplete()
 * disableAutoComplete()}.</p>
 *
 * <p>A {@link ServiceBusProcessorAsyncClient} can be created for a session-enabled or a non-session-enabled Service
 * Bus entity through {@link ServiceBusClientBuilder#processorAsync()} and
 * {@link ServiceBusClientBuilder#sessionProcessorAsync()} respectively.</p>
 *
 * <p><strong>Auto-settlement and manual settlement</strong></p>
 * <p>Auto-settlement (the default) is fully non-blocking - the completion or abandonment is issued reactively when the
 * handler {@link Mono} terminates. When auto-settlement is disabled, the handler is responsible for settling each
 * message through the {@link ServiceBusReceivedMessageContext} passed to it. Note that the settlement methods on
 * {@link ServiceBusReceivedMessageContext} ({@link ServiceBusReceivedMessageContext#complete() complete()},
 * {@link ServiceBusReceivedMessageContext#abandon() abandon()}, etc.) are <strong>blocking</strong>; a handler that
 * settles manually should perform that call on a scheduler that permits blocking (for example by wrapping it in
 * {@link Mono#fromRunnable(Runnable)} subscribed on {@code Schedulers.boundedElastic()}), or
 * rely on auto-settlement, which is non-blocking. A future revision may add reactive settlement methods.</p>
 *
 * <p><strong>Lifecycle</strong></p>
 * <p>The lifecycle operations {@link #start()}, {@link #stop()}, and {@link #close()} are not designed to be invoked
 * concurrently from multiple threads. Drive the processor's lifecycle from a single controlling thread; interleaving
 * these calls from different threads leads to undefined behavior.</p>
 *
 * @see ServiceBusProcessorClient
 * @see ServiceBusClientBuilder.ServiceBusProcessorAsyncClientBuilder
 * @see ServiceBusClientBuilder.ServiceBusSessionProcessorAsyncClientBuilder
 */
public final class ServiceBusProcessorAsyncClient implements AutoCloseable {

    private static final int SCHEDULER_INTERVAL_IN_SECONDS = 10;
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusProcessorAsyncClient.class);

    private final ServiceBusReceiverClientBuilder receiverBuilder;
    private final ServiceBusSessionReceiverClientBuilder sessionReceiverBuilder;
    private final Function<ServiceBusReceivedMessageContext, Mono<Void>> processMessage;
    private final Function<ServiceBusErrorContext, Mono<Void>> processError;
    private final ServiceBusProcessorClientOptions processorOptions;
    private final boolean autoComplete;
    private final int maxConcurrentCalls;

    private final String queueName;
    private final String topicName;
    private final String subscriptionName;

    private final AtomicReference<ServiceBusReceiverAsyncClient> asyncClient = new AtomicReference<>();
    private final AtomicReference<Disposable> receiveDisposable = new AtomicReference<>();
    private final AtomicBoolean isRunning = new AtomicBoolean();
    private final AtomicInteger activeHandlerCount = new AtomicInteger(0);
    private final Object drainLock = new Object();
    private volatile String cachedFullyQualifiedNamespace;
    private volatile String cachedEntityPath;
    // True while close() is draining. New PEEK_LOCK dispatches are skipped (without counting toward the drain) so the
    // in-flight count can reach zero; RECEIVE_AND_DELETE dispatches are never skipped because the broker has already
    // removed the message and dropping it here would lose it. Reset by start() so the processor can restart.
    private volatile boolean closing;
    // PEEK_LOCK is safe to skip during drain (the broker still owns the lock and will redeliver). Cached per receive
    // cycle; defaults to false (no-skip) when the receive mode cannot be determined, so messages are never dropped.
    private volatile boolean skipDuringDrain;
    // Auto-settlement (complete/abandon) is applied only for PEEK_LOCK: in RECEIVE_AND_DELETE the broker already
    // removed the message on delivery, so a settlement call is meaningless and would fail. Cached per receive cycle.
    private volatile boolean settleMessages;
    private Disposable monitorDisposable;

    /**
     * Constructor to create a non-session async processor.
     *
     * @param receiverBuilder The receiver builder used to create new receivers for the processor.
     * @param queueName The name of the queue this processor is associated with.
     * @param topicName The name of the topic this processor is associated with.
     * @param subscriptionName The name of the subscription this processor is associated with.
     * @param processMessage The async message processing callback.
     * @param processError The async error handler.
     * @param processorOptions Options to configure this instance of the processor.
     */
    ServiceBusProcessorAsyncClient(ServiceBusReceiverClientBuilder receiverBuilder, String queueName, String topicName,
        String subscriptionName, Function<ServiceBusReceivedMessageContext, Mono<Void>> processMessage,
        Function<ServiceBusErrorContext, Mono<Void>> processError, ServiceBusProcessorClientOptions processorOptions) {
        this.receiverBuilder = Objects.requireNonNull(receiverBuilder, "'receiverBuilder' cannot be null");
        this.sessionReceiverBuilder = null;
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");
        this.autoComplete = !processorOptions.isDisableAutoComplete();
        this.maxConcurrentCalls = validateMaxConcurrentCalls(processorOptions.getMaxConcurrentCalls());
        this.queueName = queueName;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
    }

    /**
     * Constructor to create a session-enabled async processor.
     *
     * @param sessionReceiverBuilder The session receiver builder used to create new receivers for the processor.
     * @param queueName The name of the queue this processor is associated with.
     * @param topicName The name of the topic this processor is associated with.
     * @param subscriptionName The name of the subscription this processor is associated with.
     * @param processMessage The async message processing callback.
     * @param processError The async error handler.
     * @param processorOptions Options to configure this instance of the processor.
     */
    ServiceBusProcessorAsyncClient(ServiceBusSessionReceiverClientBuilder sessionReceiverBuilder, String queueName,
        String topicName, String subscriptionName,
        Function<ServiceBusReceivedMessageContext, Mono<Void>> processMessage,
        Function<ServiceBusErrorContext, Mono<Void>> processError, ServiceBusProcessorClientOptions processorOptions) {
        this.sessionReceiverBuilder
            = Objects.requireNonNull(sessionReceiverBuilder, "'sessionReceiverBuilder' cannot be null");
        this.receiverBuilder = null;
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");
        this.autoComplete = !processorOptions.isDisableAutoComplete();
        this.maxConcurrentCalls = validateMaxConcurrentCalls(processorOptions.getMaxConcurrentCalls());
        this.queueName = queueName;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
    }

    /**
     * Starts the processor in the background. When the returned {@link Mono} completes, the processor has wired a
     * message receiver that invokes the async message handler as messages become available, and the async error
     * handler when an error occurs. Control returns immediately - the returned {@link Mono} does not wait for messages
     * to be processed.
     * <p><strong>The returned {@link Mono} is cold: the processor does not start until you subscribe to (or
     * {@code block()} on) it. Calling {@code start()} without subscribing is a no-op.</strong></p>
     * <p>
     * This method is idempotent - subscribing to the {@link Mono} returned when the processor is already running is a
     * no-op. Calling {@code start()} after {@link #stop() stop()} resumes processing using the same underlying
     * connection; calling {@code start()} after {@link #close() close()} starts the processor with a new connection.
     * </p>
     *
     * @return A {@link Mono} that completes when the processor has started.
     */
    public Mono<Void> start() {
        return Mono.fromRunnable(() -> {
            synchronized (this) {
                if (isRunning.getAndSet(true)) {
                    LOGGER.info("Processor is already running");
                    return;
                }
                closing = false;
                if (asyncClient.get() == null) {
                    asyncClient.set(createNewReceiver());
                }
                subscribeToReceiver(asyncClient.get());
                startMonitor();
            }
        });
    }

    /**
     * Stops message processing for this processor. The receiving links and sessions are kept active and processing can
     * be resumed by subscribing to {@link #start()} again. This does not wait for in-flight handlers to finish;
     * disposing the receive subscription cancels any handlers that are still running. Use {@link #close()} to give
     * in-flight handlers a best-effort chance to drain.
     * <p><strong>The returned {@link Mono} is cold: it takes effect only when subscribed to (or
     * blocked on).</strong></p>
     *
     * @return A {@link Mono} that completes when the processor has stopped requesting new messages.
     */
    public Mono<Void> stop() {
        return Mono.fromRunnable(() -> {
            synchronized (this) {
                isRunning.set(false);
                final Disposable disposable = receiveDisposable.getAndSet(null);
                if (disposable != null) {
                    disposable.dispose();
                }
            }
        });
    }

    /**
     * Stops message processing and closes the processor. The receiving links and sessions are closed; subscribing to
     * {@link #start()} afterwards creates a new processing cycle with a new connection.
     *
     * <p>This method blocks while waiting for in-flight message handlers to complete (up to the configured drain
     * timeout, default 30 seconds) before cancelling the subscription and closing the underlying client, so handlers
     * that are already running can finish settlement against a live receiver. Draining is <strong>best-effort</strong>:
     * a message the pump had already accepted at the instant {@code close()} begins may occasionally start after the
     * drain observes an empty in-flight set, in which case it may settle against a closing receiver - for
     * {@link ServiceBusReceiveMode#PEEK_LOCK PEEK_LOCK} the broker simply redelivers such a message, and for
     * {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE RECEIVE_AND_DELETE} no settlement is required. Callers should
     * avoid invoking {@code close()} on latency-sensitive threads.</p>
     *
     * <p><strong>RECEIVE_AND_DELETE and shutdown:</strong> the broker removes
     * {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE RECEIVE_AND_DELETE} messages on delivery, and the receiver keeps
     * delivering while the drain waits. Under sustained traffic the in-flight set may not reach zero before the drain
     * timeout; handlers still running when it elapses are cancelled and those messages are lost (they cannot be
     * redelivered). A future revision may decouple handler execution from the receiver subscription to bound this.</p>
     *
     * <p>Do not call {@code close()} from within a message or error handler: it blocks until in-flight handlers
     * drain, so invoking it from a handler that has not yet returned would deadlock. Calling {@link #stop()} from a
     * handler is also unsafe: it disposes the receive subscription and cancels the very handler that invoked it.
     * Schedule shutdown from a separate controlling thread.</p>
     */
    @Override
    public void close() {
        final Duration drainTimeout;
        synchronized (this) {
            if (!isRunning.getAndSet(false) && asyncClient.get() == null) {
                return;
            }
            // Signal the pump to stop accepting new PEEK_LOCK dispatches so the in-flight count can drain to zero.
            closing = true;
            drainTimeout = processorOptions.getDrainTimeout();
        }

        // Drain in-flight handlers BEFORE disposing the subscription and closing the receiver, so handlers that are
        // already running can settle against a live receiver. Best-effort: a dispatch accepted by the pump at the
        // instant close() begins may still slip past the drain (see the close() Javadoc).
        drainHandlers(drainTimeout);

        synchronized (this) {
            final Disposable disposable = receiveDisposable.getAndSet(null);
            if (disposable != null) {
                disposable.dispose();
            }
            if (monitorDisposable != null) {
                monitorDisposable.dispose();
                monitorDisposable = null;
            }
            final ServiceBusReceiverAsyncClient client = asyncClient.getAndSet(null);
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Returns {@code true} if the processor is running. If the processor is stopped or closed, this returns
     * {@code false}.
     *
     * @return {@code true} if the processor is running; {@code false} otherwise.
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Returns the queue name associated with this instance of {@link ServiceBusProcessorAsyncClient}.
     *
     * @return the queue name, or {@code null} if the processor instance is for a topic and subscription.
     */
    public String getQueueName() {
        return this.queueName;
    }

    /**
     * Returns the topic name associated with this instance of {@link ServiceBusProcessorAsyncClient}.
     *
     * @return the topic name, or {@code null} if the processor instance is for a queue.
     */
    public String getTopicName() {
        return this.topicName;
    }

    /**
     * Returns the subscription name associated with this instance of {@link ServiceBusProcessorAsyncClient}.
     *
     * @return the subscription name, or {@code null} if the processor instance is for a queue.
     */
    public String getSubscriptionName() {
        return this.subscriptionName;
    }

    /**
     * Gets the identifier of the instance of {@link ServiceBusProcessorAsyncClient}.
     *
     * @return The identifier that can identify the instance of {@link ServiceBusProcessorAsyncClient}, or {@code null}
     *     if no receiver has been created yet (before the first {@link #start() start()}).
     */
    public String getIdentifier() {
        final ServiceBusReceiverAsyncClient client = asyncClient.get();
        return client == null ? null : client.getIdentifier();
    }

    private void subscribeToReceiver(ServiceBusReceiverAsyncClient receiverClient) {
        cachedFullyQualifiedNamespace = receiverClient.getFullyQualifiedNamespace();
        cachedEntityPath = receiverClient.getEntityPath();
        // Only PEEK_LOCK is safe to skip during drain, and PEEK_LOCK is the only mode the processor settles. Default
        // to no-skip and no-settle when the mode is unavailable.
        final ReceiverOptions receiverOptions = receiverClient.getReceiverOptions();
        final boolean isPeekLock
            = receiverOptions != null && receiverOptions.getReceiveMode() == ServiceBusReceiveMode.PEEK_LOCK;
        this.skipDuringDrain = isPeekLock;
        this.settleMessages = autoComplete && isPeekLock;

        final Disposable disposable = receiverClient.receiveMessagesWithContext()
            .flatMap(messageContext -> dispatchMessage(messageContext, receiverClient), maxConcurrentCalls, 1)
            .subscribe(ignored -> {
            }, throwable -> {
                LOGGER.info("Error receiving messages.", throwable);
                handleError(throwable).subscribe();
                scheduleRestart();
            }, () -> {
                LOGGER.info("Completed receiving messages.");
                scheduleRestart();
            });
        receiveDisposable.set(disposable);
    }

    private Mono<Void> dispatchMessage(ServiceBusMessageContext messageContext,
        ServiceBusReceiverAsyncClient receiverClient) {
        if (messageContext.hasError()) {
            return handleError(messageContext.getThrowable());
        }

        return Mono.defer(() -> {
            // Fast path: a redeliverable PEEK_LOCK message that arrives while close() is draining is skipped WITHOUT
            // touching the in-flight counter, so churn from skipped messages under sustained load cannot keep the
            // drain above zero and push shutdown toward the drain timeout. (The broker redelivers a skipped message.)
            if (closing && skipDuringDrain) {
                LOGGER.verbose("Skipping dispatch, processor is closing.");
                return Mono.<Void>empty();
            }
            // Publish intent-to-run, then re-check: this handles the check-then-act race where closing flips between
            // the fast-path check and the increment, and narrows (best-effort, per the close() Javadoc) the window in
            // which a dispatch could start after the drain has observed an empty in-flight set.
            activeHandlerCount.incrementAndGet();
            if (closing && skipDuringDrain) {
                LOGGER.verbose("Skipping dispatch, processor is closing.");
                decrementAndNotifyDrain();
                return Mono.<Void>empty();
            }
            final ServiceBusReceivedMessageContext receivedMessageContext;
            try {
                receivedMessageContext = new ServiceBusReceivedMessageContext(receiverClient, messageContext);
            } catch (RuntimeException e) {
                // Constructor threw before the doFinally decrement could attach; release the count so a synchronous
                // failure here cannot leave the drain permanently above zero.
                decrementAndNotifyDrain();
                throw LOGGER.logExceptionAsError(e);
            }

            return Mono.defer(() -> processMessage.apply(receivedMessageContext)).then(Mono.defer(() -> {
                // The handler succeeded. Auto-settle only in PEEK_LOCK; a completion failure is reported to the error
                // handler with its OWN error source (e.g. COMPLETE) and must NOT trigger an abandon - the handler
                // did its job.
                if (!settleMessages) {
                    return Mono.<Void>empty();
                }
                return receiverClient.complete(messageContext.getMessage())
                    .onErrorResume(completeError -> handleError(completeError));
            }))
                // Only a handler error reaches here (completion errors were handled above). Report it as USER_CALLBACK
                // and abandon the message (when settling, i.e. PEEK_LOCK with auto-complete enabled).
                .onErrorResume(handlerError -> handleError(
                    new ServiceBusException(handlerError, ServiceBusErrorSource.USER_CALLBACK)).then(Mono.defer(() -> {
                        if (settleMessages) {
                            LOGGER.warning("Error when processing message. Abandoning message.", handlerError);
                            return receiverClient.abandon(messageContext.getMessage()).onErrorResume(abandonError -> {
                                LOGGER.verbose("Failed to abandon message", abandonError);
                                return Mono.<Void>empty();
                            });
                        }
                        return Mono.<Void>empty();
                    })))
                .then()
                .doFinally(signalType -> decrementAndNotifyDrain());
        });
    }

    /**
     * Decrements the in-flight handler count and, when it reaches zero, wakes any thread blocked in
     * {@link #drainHandlers(Duration)} waiting for the drain to complete.
     */
    private void decrementAndNotifyDrain() {
        if (activeHandlerCount.decrementAndGet() <= 0) {
            synchronized (drainLock) {
                drainLock.notifyAll();
            }
        }
    }

    private static int validateMaxConcurrentCalls(int maxConcurrentCalls) {
        if (maxConcurrentCalls < 1) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'maxConcurrentCalls' cannot be less than 1"));
        }
        return maxConcurrentCalls;
    }

    private Mono<Void> handleError(Throwable throwable) {
        return Mono.defer(() -> {
            final ServiceBusErrorContext errorContext
                = new ServiceBusErrorContext(throwable, cachedFullyQualifiedNamespace, cachedEntityPath);
            // Wrap apply() in its own defer so a synchronously-throwing error handler (one that throws instead of
            // returning Mono.error) is converted to an error signal and caught by onErrorResume - never propagated to
            // the receive subscription, where it would otherwise trigger a spurious restart.
            return Mono.defer(() -> processError.apply(errorContext)).onErrorResume(errorHandlerError -> {
                LOGGER.verbose("Error from error handler. Ignoring error.", errorHandlerError);
                return Mono.empty();
            });
        });
    }

    // Restart off the reactive receive thread: restartMessageReceiver() calls the blocking client close(), which must
    // not run on the subscription's onError/onComplete callback thread. restartMessageReceiver() guards on
    // isRunning/closing, so a stale schedule after stop()/close() is a no-op.
    private void scheduleRestart() {
        Mono.fromRunnable(this::restartMessageReceiver).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    // Auto-recovery is one restart per receive-stream terminal (error/completion). There is deliberately no backoff
    // or attempt cap here: the underlying ServiceBusReceiverAsyncClient applies the configured retry policy (with
    // backoff) before surfacing a terminal error, so transient failures are absorbed there. This matches the sync
    // ServiceBusProcessorClient - a fast persistent failure (entity deleted, credentials revoked) relies on that
    // receiver-level policy rather than a second backoff layer here. Unlike close(), a restart does NOT drain in-flight
    // handlers before closing the stale receiver: a handler still running settles against a closing receiver, so a
    // PEEK_LOCK message is redelivered (no loss) and RECEIVE_AND_DELETE needs no settlement.
    private synchronized void restartMessageReceiver() {
        if (!isRunning.get() || closing) {
            return;
        }
        final Disposable disposable = receiveDisposable.getAndSet(null);
        if (disposable != null) {
            disposable.dispose();
        }
        final ServiceBusReceiverAsyncClient staleClient = asyncClient.get();
        if (staleClient != null) {
            staleClient.close();
        }
        final ServiceBusReceiverAsyncClient freshClient = createNewReceiver();
        asyncClient.set(freshClient);
        subscribeToReceiver(freshClient);
    }

    private synchronized void startMonitor() {
        if (monitorDisposable == null) {
            monitorDisposable = Schedulers.boundedElastic().schedulePeriodically(() -> {
                final ServiceBusReceiverAsyncClient currentClient = this.asyncClient.get();
                if (currentClient == null) {
                    return;
                }
                if (currentClient.isConnectionClosed()) {
                    restartMessageReceiver();
                }
            }, SCHEDULER_INTERVAL_IN_SECONDS, SCHEDULER_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    private ServiceBusReceiverAsyncClient createNewReceiver() {
        return this.receiverBuilder == null
            ? this.sessionReceiverBuilder.buildAsyncClientForProcessor()
            : this.receiverBuilder.buildAsyncClientForProcessor();
    }

    private void drainHandlers(Duration timeout) {
        final long deadlineNanos = System.nanoTime() + timeout.toNanos();
        synchronized (drainLock) {
            while (activeHandlerCount.get() > 0) {
                final long remainingNanos = deadlineNanos - System.nanoTime();
                if (remainingNanos <= 0) {
                    LOGGER.info("Drain timeout elapsed with {} handler(s) still in flight; proceeding with shutdown.",
                        activeHandlerCount.get());
                    break;
                }
                try {
                    final long remainingMillis = Math.max(1, remainingNanos / 1_000_000);
                    drainLock.wait(remainingMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
