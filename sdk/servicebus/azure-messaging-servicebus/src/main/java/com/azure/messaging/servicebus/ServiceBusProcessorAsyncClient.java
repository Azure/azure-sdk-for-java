// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

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
 * receiver demand is replenished as in-flight handlers complete. Configured prefetch can buffer additional messages.
 * By default a message whose
 * handler {@link Mono} completes successfully is {@link ServiceBusReceivedMessageAsyncContext#complete() completed}, and a
 * message whose handler {@link Mono} signals an error is {@link ServiceBusReceivedMessageAsyncContext#abandon() abandoned};
 * this auto-settlement can be disabled via
 * {@link ServiceBusClientBuilder.ServiceBusProcessorAsyncClientBuilder#disableAutoComplete()
 * disableAutoComplete()}.</p>
 *
 * <p>A {@link ServiceBusProcessorAsyncClient} can be created for a session-enabled or a non-session-enabled Service
 * Bus entity through {@link ServiceBusClientBuilder#sessionProcessorAsync()} and
 * {@link ServiceBusClientBuilder#processorAsync()} respectively.</p>
 *
 * <p><strong>Auto-settlement and manual settlement</strong></p>
 * <p>Auto-settlement (the default) is fully non-blocking - the completion or abandonment is issued reactively when the
 * handler {@link Mono} terminates. When auto-settlement is disabled, the handler is responsible for settling each
 * message through the {@link ServiceBusReceivedMessageAsyncContext} passed to it. Compose settlement into the
 * returned handler {@link Mono}, after all downstream work has completed. Do not start detached subscriptions
 * inside a handler: its returned {@link Mono} must represent the entire processing and settlement lifetime.</p>
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
    private final Function<ServiceBusReceivedMessageAsyncContext, Mono<Void>> processMessage;
    private final Function<ServiceBusErrorContext, Mono<Void>> processError;
    private final ServiceBusProcessorClientOptions processorOptions;
    private final boolean autoComplete;
    private final int maxConcurrentCalls;

    private final String queueName;
    private final String topicName;
    private final String subscriptionName;

    private final AtomicReference<ServiceBusReceiverAsyncClient> asyncClient = new AtomicReference<>();
    private final AtomicReference<ServiceBusSessionReceiverAsyncClient> sessionClient = new AtomicReference<>();
    private final Set<SessionLease> sessionLeases = ConcurrentHashMap.newKeySet();
    private final AtomicReference<Disposable> receiveDisposable = new AtomicReference<>();
    private final AtomicBoolean isRunning = new AtomicBoolean();
    private volatile String cachedFullyQualifiedNamespace;
    private volatile String cachedEntityPath;
    private volatile boolean closing;
    private volatile AtomicBoolean receiveTerminated = new AtomicBoolean();
    private Sinks.Empty<Void> resumeProcessing = Sinks.empty();
    private Runnable resumeReceiving;
    private Sinks.Empty<Void> stopReceiving;
    private Sinks.Empty<Void> processingFinished;
    private Disposable monitorDisposable;
    private Disposable recoveryErrorDisposable;
    private Mono<Void> recoveryErrorFinished = Mono.empty();

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
        String subscriptionName, Function<ServiceBusReceivedMessageAsyncContext, Mono<Void>> processMessage,
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
        Function<ServiceBusReceivedMessageAsyncContext, Mono<Void>> processMessage,
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
        return Mono.<Void>fromRunnable(() -> {
            synchronized (this) {
                if (isRunning.get()) {
                    LOGGER.info("Processor is already running");
                    return;
                }
                closing = false;
                isRunning.set(true);
                try {
                    if (receiveDisposable.get() == null || receiveTerminated.get()) {
                        if (receiveTerminated.get()) {
                            disposeReceiver();
                        }
                        subscribeToReceiver();
                    } else {
                        resumeProcessing.tryEmitEmpty();
                        resumeReceiving.run();
                    }
                    startMonitor();
                } catch (RuntimeException error) {
                    isRunning.set(false);
                    cleanupAfterFailure(error);
                    disposeMonitor();
                    throw LOGGER.logExceptionAsError(error);
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Stops message processing for this processor. The receiving links and sessions are kept active and processing can
     * be resumed by subscribing to {@link #start()} again. This does not wait for or cancel in-flight handlers;
     * they can finish processing and settlement while the processor is stopped. Use {@link #close()} to drain
     * accepted handlers and release the receivers.
     * <p><strong>The returned {@link Mono} is cold: it takes effect only when subscribed to (or
     * blocked on).</strong></p>
     *
     * @return A {@link Mono} that completes when the processor has stopped requesting new messages.
     */
    public Mono<Void> stop() {
        return Mono.<Void>fromRunnable(() -> {
            synchronized (this) {
                if (isRunning.getAndSet(false)) {
                    resumeProcessing = Sinks.empty();
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Stops message processing and closes the processor. The receiving links and sessions are closed; subscribing to
     * {@link #start()} afterwards creates a new processing cycle with a new connection.
     *
     * <p>This method blocks while waiting for in-flight message handlers to complete (up to the configured drain
     * timeout, default 30 seconds) before cancelling the subscription and closing the underlying client, so handlers
     * that are already running can finish settlement against a live receiver. Dispatch and further receive demand stop
     * before waiting; receiving links remain open until draining ends. Callers should avoid invoking {@code close()}
     * on latency-sensitive threads.</p>
     *
     * <p><strong>RECEIVE_AND_DELETE and shutdown:</strong> the broker removes
     * {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE RECEIVE_AND_DELETE} messages on delivery. Handlers still running
     * when the drain timeout elapses are cancelled and their messages cannot be redelivered. Prefetched messages
     * that have not reached a handler can also be lost in this mode.</p>
     *
     * <p>Do not call {@code close()} from within a message or error handler: it blocks until in-flight handlers
     * drain, so invoking it from a handler that has not yet returned would deadlock. Calling {@link #stop()} from a
     * handler does not wait for that handler to finish. Schedule shutdown from a separate controlling thread.</p>
     */
    @Override
    public void close() {
        final Mono<Void> finished;
        synchronized (this) {
            if (!isRunning.getAndSet(false)
                && asyncClient.get() == null
                && sessionClient.get() == null
                && monitorDisposable == null
                && recoveryErrorDisposable == null) {
                return;
            }
            closing = true;
            resumeProcessing.tryEmitEmpty();
            finished = Mono.when(processingFinished == null ? Mono.empty() : processingFinished.asMono(),
                recoveryErrorFinished);
            if (stopReceiving != null) {
                stopReceiving.tryEmitEmpty();
            }
        }

        try {
            finished.timeout(processorOptions.getDrainTimeout(), Mono
                .fromRunnable(() -> LOGGER.warning("Drain timeout elapsed; cancelling remaining message handlers.")))
                .block();
        } finally {
            synchronized (this) {
                try {
                    disposeReceiver();
                } finally {
                    disposeMonitor();
                    if (recoveryErrorDisposable != null) {
                        recoveryErrorDisposable.dispose();
                        recoveryErrorDisposable = null;
                    }
                    recoveryErrorFinished = Mono.empty();
                }
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
     *     before the first {@link #start() start()} or after {@link #close()}.
     */
    public String getIdentifier() {
        final ServiceBusReceiverAsyncClient client = asyncClient.get();
        final ServiceBusSessionReceiverAsyncClient sessions = sessionClient.get();
        return client != null ? client.getIdentifier() : sessions == null ? null : sessions.getIdentifier();
    }

    private void subscribeToReceiver() {
        final Flux<ReceivedMessage> receivedMessages;
        if (receiverBuilder != null) {
            final ServiceBusReceiverAsyncClient receiver = receiverBuilder.buildAsyncClientForProcessor();
            asyncClient.set(receiver);
            cachedFullyQualifiedNamespace = receiver.getFullyQualifiedNamespace();
            cachedEntityPath = receiver.getEntityPath();
            receivedMessages = receiver.receiveMessagesWithContextForAsyncProcessor()
                .map(context -> new ReceivedMessage(receiver, context,
                    () -> receiver.endLockRenewal(context.getMessage())));
        } else {
            final ServiceBusSessionReceiverAsyncClient sessions
                = sessionReceiverBuilder.buildAsyncClientForAsyncProcessor();
            sessionClient.set(sessions);
            cachedFullyQualifiedNamespace = sessions.getFullyQualifiedNamespace();
            cachedEntityPath = sessions.getEntityPath();
            final int concurrency = sessions.getReceiverOptions().getMaxConcurrentSessions();
            receivedMessages
                = Flux.range(0, concurrency).flatMap(ignored -> receiveSession(sessions).repeat(), concurrency, 1);
        }
        // A delayed terminal callback must only mark its own receive cycle.
        final AtomicBoolean terminated = new AtomicBoolean();
        receiveTerminated = terminated;
        final Sinks.Empty<Void> stop = Sinks.empty();
        final Sinks.Empty<Void> finished = Sinks.empty();
        stopReceiving = stop;
        processingFinished = finished;

        final Disposable.Composite subscriptions = Disposables.composite();
        receiveDisposable.set(subscriptions);
        // Completing intake must not cancel the underlying receiver: cancellation can close its AMQP links,
        // which accepted handlers still need for settlement. The composite cancels it after drain finishes.
        final Flux<ReceivedMessage> messages = Flux.create(sink -> {
            final BaseSubscriber<ReceivedMessage> subscriber = new BaseSubscriber<ReceivedMessage>() {
                private long pending;

                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    sink.onRequest(count -> {
                        synchronized (ServiceBusProcessorAsyncClient.this) {
                            if (!closing) {
                                pending += count;
                                resume();
                            }
                        }
                    });
                    resumeReceiving = this::resume;
                }

                private void resume() {
                    if (isRunning.get() && !closing) {
                        final long count = pending;
                        pending = 0;
                        if (count > 0) {
                            request(count);
                        }
                    }
                }

                @Override
                protected void hookOnNext(ReceivedMessage message) {
                    sink.next(message);
                }

                @Override
                protected void hookOnError(Throwable error) {
                    sink.error(error);
                }

                @Override
                protected void hookOnComplete() {
                    sink.complete();
                }
            };
            subscriptions.add(subscriber);
            sink.onCancel(subscriber);
            final Disposable stopSubscription = stop.asMono().subscribe(ignored -> {
            }, sink::error, sink::complete);
            subscriptions.add(stopSubscription);
            sink.onDispose(stopSubscription);
            receivedMessages.subscribe(subscriber);
        }, FluxSink.OverflowStrategy.ERROR);

        final Disposable disposable = messages
            .flatMap(message -> awaitRunning().then(Mono.defer(() -> dispatchMessage(message)))
                .doFinally(signal -> message.finished.run()), maxConcurrentCalls, 1)
            .onErrorResume(error -> handleError(error).then(Mono.error(error)))
            .doFinally(signal -> finished.tryEmitEmpty())
            .subscribe(ignored -> {
            }, throwable -> {
                LOGGER.info("Error receiving messages.", throwable);
                markReceiveTerminated(subscriptions, terminated);
            }, () -> {
                LOGGER.info("Completed receiving messages.");
                markReceiveTerminated(subscriptions, terminated);
            });
        subscriptions.add(disposable);
    }

    private synchronized Mono<Void> awaitRunning() {
        return isRunning.get() || closing ? Mono.empty() : resumeProcessing.asMono();
    }

    private Mono<Void> dispatchMessage(ReceivedMessage received) {
        final ServiceBusMessageContext messageContext = received.context;
        final ServiceBusReceiverAsyncClient receiverClient = received.receiver;
        if (messageContext.hasError()) {
            return handleError(messageContext.getThrowable());
        }
        if (closing) {
            return Mono.empty();
        }

        return Mono.defer(() -> {
            final boolean settleMessages = autoComplete
                && receiverClient.getReceiverOptions().getReceiveMode() == ServiceBusReceiveMode.PEEK_LOCK;
            final AtomicBoolean handlerSucceeded = new AtomicBoolean();
            final ServiceBusReceivedMessageAsyncContext receivedMessageContext
                = new ServiceBusReceivedMessageAsyncContext(receiverClient, messageContext);

            final Mono<Void> processing = Mono.defer(() -> processMessage.apply(receivedMessageContext))
                .doOnSuccess(ignored -> handlerSucceeded.set(true))
                .then(Mono.defer(() -> {
                    if (!settleMessages) {
                        return Mono.<Void>empty();
                    }
                    return receiverClient.complete(messageContext.getMessage());
                }))
                // Preserve service error sources from reactive manual settlement.
                .onErrorResume(
                    handlerError -> handleError(handlerSucceeded.get() || handlerError instanceof ServiceBusException
                        ? handlerError
                        : new ServiceBusException(handlerError, ServiceBusErrorSource.USER_CALLBACK))
                            .then(Mono.defer(() -> {
                                if (settleMessages && !handlerSucceeded.get()) {
                                    LOGGER.warning("Error when processing message. Abandoning message.", handlerError);
                                    return receiverClient.abandon(messageContext.getMessage());
                                }
                                return Mono.<Void>empty();
                            }).onErrorResume(this::handleError))
                            .then(Mono.error(handlerError)));
            return receiverClient.getInstrumentation()
                .instrumentProcessAsync(messageContext.getMessage(), processing)
                // Recovery has reported the error; expose it to tracing without terminating intake.
                .onErrorComplete();
        });
    }

    private void disposeReceiver() {
        final Disposable disposable = receiveDisposable.getAndSet(null);
        final ServiceBusReceiverAsyncClient client = asyncClient.getAndSet(null);
        final ServiceBusSessionReceiverAsyncClient sessions = sessionClient.getAndSet(null);
        try {
            if (disposable != null) {
                disposable.dispose();
            }
        } finally {
            try {
                // Cleanup must not wait for work queued on the controlling thread's own scheduler.
                Mono.whenDelayError(sessionLeases.stream()
                    .map(lease -> Mono.<Void>fromRunnable(lease::closeReceiver))
                    .collect(Collectors.toList())).block();
                if (client != null) {
                    client.close();
                }
            } finally {
                if (sessions != null) {
                    sessions.close();
                }
            }
        }
    }

    private void cleanupAfterFailure(RuntimeException error) {
        try {
            disposeReceiver();
        } catch (RuntimeException cleanupError) {
            if (cleanupError != error) {
                error.addSuppressed(cleanupError);
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

    // Let the monitor rebuild terminal streams so persistent non-transient errors cannot cause a hot restart loop.
    private void markReceiveTerminated(Disposable cycle, AtomicBoolean terminated) {
        if (receiveDisposable.get() == cycle) {
            terminated.set(true);
        }
    }

    // A terminal receive error cancels accepted handlers; unlike close(), recovery cannot retain their links
    // for settlement.
    private synchronized void restartMessageReceiver(Disposable cycle) {
        if (!isRunning.get()
            || closing
            || receiveDisposable.get() != cycle
            || (recoveryErrorDisposable != null && !recoveryErrorDisposable.isDisposed())) {
            return;
        }
        try {
            disposeReceiver();
            subscribeToReceiver();
        } catch (RuntimeException error) {
            cleanupAfterFailure(error);
            LOGGER.warning("Failed to restart the receiver; the processor monitor will retry.", error);
            final Sinks.Empty<Void> reported = Sinks.empty();
            recoveryErrorFinished = reported.asMono();
            recoveryErrorDisposable = handleError(new ServiceBusException(error, ServiceBusErrorSource.RECEIVE))
                .doFinally(signal -> reported.tryEmitEmpty())
                .subscribe();
        }
    }

    private synchronized void startMonitor() {
        if (monitorDisposable == null) {
            monitorDisposable = Schedulers.boundedElastic().schedulePeriodically(() -> {
                synchronized (this) {
                    final ServiceBusReceiverAsyncClient currentClient = asyncClient.get();
                    final ServiceBusSessionReceiverAsyncClient sessions = sessionClient.get();
                    if (receiveTerminated.get()
                        || (currentClient == null && sessions == null)
                        || (currentClient != null && currentClient.isConnectionClosed())
                        || (sessions != null && sessions.isConnectionClosed())) {
                        restartMessageReceiver(receiveDisposable.get());
                    }
                }
            }, SCHEDULER_INTERVAL_IN_SECONDS, SCHEDULER_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void disposeMonitor() {
        if (monitorDisposable != null) {
            monitorDisposable.dispose();
            monitorDisposable = null;
        }
    }

    private Flux<ReceivedMessage> receiveSession(ServiceBusSessionReceiverAsyncClient sessions) {
        return Flux.usingWhen(Mono.defer(sessions::acceptNextSession).map(receiver -> {
            final SessionLease lease = new SessionLease(receiver);
            sessionLeases.add(lease);
            return lease;
        }), lease -> {
            final Duration idleTimeout = sessions.getSessionIdleTimeout();
            return lease.receiver.receiveMessagesWithContextForAsyncProcessor().map(context -> {
                lease.accepted();
                return new ReceivedMessage(lease.receiver, context, lease::finished);
            })
                .takeUntilOther(lease.activity.asFlux()
                    .switchMap(active -> active ? Mono.never() : Mono.delay(idleTimeout))
                    .next());
        }, this::closeSession, (lease, error) -> closeSession(lease), this::closeSession);
    }

    private Mono<Void> closeSession(SessionLease lease) {
        return lease.close.doFinally(signal -> sessionLeases.remove(lease));
    }

    private static final class ReceivedMessage {
        private final ServiceBusReceiverAsyncClient receiver;
        private final ServiceBusMessageContext context;
        private final Runnable finished;

        private ReceivedMessage(ServiceBusReceiverAsyncClient receiver, ServiceBusMessageContext context,
            Runnable finished) {
            this.receiver = receiver;
            this.context = context;
            this.finished = finished;
        }
    }

    private static final class SessionLease {
        private final ServiceBusReceiverAsyncClient receiver;
        private final Mono<Void> close;
        private final AtomicBoolean closeStarted = new AtomicBoolean();
        private final Sinks.Empty<Void> closeFinished = Sinks.empty();
        private final Sinks.Many<Boolean> activity = Sinks.many().replay().latestOrDefault(false);
        private int pending;

        private SessionLease(ServiceBusReceiverAsyncClient receiver) {
            this.receiver = receiver;
            this.close = Mono.<Void>fromRunnable(this::closeReceiver).subscribeOn(Schedulers.boundedElastic()).cache();
        }

        private void closeReceiver() {
            if (closeStarted.compareAndSet(false, true)) {
                try {
                    receiver.close();
                    closeFinished.tryEmitEmpty();
                } catch (RuntimeException error) {
                    closeFinished.tryEmitError(error);
                    throw error;
                }
            } else {
                closeFinished.asMono().block();
            }
        }

        private synchronized void accepted() {
            if (pending++ == 0) {
                activity.emitNext(true, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }

        private synchronized void finished() {
            if (--pending == 0) {
                activity.emitNext(false, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }
    }
}
