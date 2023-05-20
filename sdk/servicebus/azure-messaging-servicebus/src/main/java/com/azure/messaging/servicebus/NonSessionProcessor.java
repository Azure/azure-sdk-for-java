// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.util.FluxUtil.monoError;

/**
 *  Processor to stream messages from a session unaware entity.
 */
public final class NonSessionProcessor {
    private final Object lock = new Object();
    private final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder;
    private final int concurrency;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final boolean enableAutoDisposition;
    private boolean isRunning;
    private RollingMessagePump rollingMessagePump;

    NonSessionProcessor(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
        int concurrency, boolean enableAutoDisposition) {
        this.builder = builder;
        this.concurrency = concurrency;
        this.processError = processError;
        this.processMessage = processMessage;
        this.enableAutoDisposition = enableAutoDisposition;

        synchronized (lock) {
            this.isRunning = false;
        }
    }

    void start() {
        final RollingMessagePump p;
        synchronized (lock) {
            if (isRunning) {
                return;
            }
            isRunning = true;
            rollingMessagePump = new RollingMessagePump(builder, processMessage, processError, concurrency, enableAutoDisposition);
            p = rollingMessagePump;
        }
        p.begin();
    }

    boolean isRunning() {
        synchronized (lock) {
            return isRunning;
        }
    }

    void close() {
        final RollingMessagePump p;
        synchronized (lock) {
            if (!isRunning) {
                return;
            }
            isRunning = false;
            p = rollingMessagePump;
        }
        p.dispose();
    }

    void stop() {
        // Synonym for 'close' see https://github.com/Azure/azure-sdk-for-java/issues/34464
        close();
    }

    String getIdentifier() {
        final RollingMessagePump p;
        synchronized (lock) {
            p = rollingMessagePump;
        }
        return p == null ? null : p.getClientIdentifier();
    }

    /**
     * The abstraction to stream messages using {@link MessagePump}. {@link RollingMessagePump} transparently switch
     * (rolls) to the next {@link MessagePump} to continue streaming when the current pump terminates.
     */
    static final class RollingMessagePump extends AtomicBoolean {
        private static final RuntimeException DISPOSED_ERROR = new RuntimeException("The Processor closure disposed the RollingMessagePump.");
        private static final Duration NEXT_PUMP_BACKOFF = Duration.ofSeconds(5);
        private final ClientLogger logger;
        private final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder;
        private final int concurrency;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final Disposable.Composite disposable = Disposables.composite();
        private final boolean enableAutoDisposition;
        private final AtomicReference<String> clientIdentifier = new AtomicReference<>();

        /**
         * Instantiate {@link RollingMessagePump} that stream messages using {@link MessagePump}.
         * The {@link RollingMessagePump} rolls to the next {@link MessagePump} to continue streaming when the current
         * {@link MessagePump} terminates.
         *
         * @param builder The builder to build the client for pulling messages from the broker.
         * @param processMessage The consumer to invoke for each message.
         * @param processError The consumer to report the errors.
         * @param concurrency The parallelism, i.e., how many invocations of {@code processMessage} should happen in parallel.
         * @param enableAutoDisposition Indicate if auto-complete or abandon should be enabled.
         */
        RollingMessagePump(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
            int concurrency, boolean enableAutoDisposition) {
            this.logger = new ClientLogger(RollingMessagePump.class);
            this.builder = builder;
            this.concurrency = concurrency;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoDisposition = enableAutoDisposition;
        }

        /**
         * Begin streaming messages. To terminate the streaming, dispose the pump by invoking {@link MessagePump#dispose()}.
         *
         * @throws IllegalStateException If the API is called more than once or after the disposal.
         */
        void begin() {
            if (getAndSet(true)) {
                throw logger.atInfo().log(new IllegalStateException("The streaming cannot begin more than once."));
            }
            final Disposable d = beginIntern().subscribe();
            if (!disposable.add(d)) {
                throw logger.atInfo().log(new IllegalStateException("Cannot begin streaming after the disposal."));
            }
        }

        // Internal API that begin streaming messages once subscribed to the mono it returns, this method is supposed
        // to be called only from 'MessagePump#begin'. The package internal scope is to support testing.
        Mono<Void> beginIntern() {
            final Mono<Void> pumping = Mono.using(
                () -> {
                    return builder.buildAsyncClient();
                },
                client -> {
                    clientIdentifier.set(client.getIdentifier());
                    final MessagePump pump = new MessagePump(client, processMessage, processError, concurrency, enableAutoDisposition);
                    return pump.begin();
                },
                client -> {
                    client.close();
                },
                true);
            return pumping.retryWhen(retrySpecForNextPump());
        }

        String getClientIdentifier() {
            return clientIdentifier.get();
        }

        void dispose() {
            disposable.dispose();
        }

        /**
         * Retry spec to roll to the next {@link MessagePump} with a back-off. If the spec is asked for retry after
         * the {@link RollingMessagePump} is disposed of (due to {@link NonSessionProcessor} closure), then an exception
         * {@link RollingMessagePump#DISPOSED_ERROR} will be emitted.
         *
         * @return the retry spec.
         */
        private Retry retrySpecForNextPump() {
            return Retry.from(retrySignals -> retrySignals
                .concatMap(retrySignal -> {
                    final Retry.RetrySignal signal = retrySignal.copy();
                    final Throwable error = signal.failure();
                    if (error == null) {
                        return monoError(logger, new IllegalStateException("RetrySignal::failure() not expected to be null."));
                    }
                    if (!(error instanceof MessagePump.TerminatedException)) {
                        return monoError(logger, new IllegalStateException("RetrySignal::failure() expected to be MessagePump.TerminatedException.", error));
                    }

                    final MessagePump.TerminatedException e = (MessagePump.TerminatedException) error;

                    if (disposable.isDisposed()) {
                        e.logWithCause(logger, "The Processor closure disposed the streaming, canceling retry for the next MessagePump.");
                        return Mono.error(DISPOSED_ERROR);
                    }

                    e.logWithCause(logger, "The current MessagePump is terminated, scheduling retry for the next pump.");

                    return Mono.delay(NEXT_PUMP_BACKOFF, Schedulers.boundedElastic())
                        .handle((v, sink) -> {
                            if (disposable.isDisposed()) {
                                e.log(logger,
                                    "During backoff, The Processor closure disposed the streaming, canceling retry for the next MessagePump.");
                                sink.error(DISPOSED_ERROR);
                            } else {
                                e.log(logger, "Retrying for the next MessagePump.");
                                sink.next(v);
                            }
                        });
                }));
        }

        /**
         * Abstraction to pump messages using a {@link ServiceBusReceiverAsyncClient} as long as the client is healthy.
         */
        private static final class MessagePump {
            private static final AtomicLong COUNTER = new AtomicLong();
            private static final String PUMP_ID_KEY = "pump-id";
            private static final Duration CONNECTION_STATE_POLL_INTERVAL = Duration.ofSeconds(20);
            private final long  pumpId;
            private final ServiceBusReceiverAsyncClient client;
            private final String fqdn;
            private final String entityPath;
            private final ClientLogger logger;
            private final Consumer<ServiceBusReceivedMessageContext> processMessage;
            private final Consumer<ServiceBusErrorContext> processError;
            private final int concurrency;
            private final boolean enableAutoDisposition;
            private final boolean enableAutoLockRenew;
            private final Scheduler workerScheduler;
            private final ServiceBusReceiverInstrumentation instrumentation;
            private final ServiceBusTracer tracer;

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
                this.fqdn = this.client.getFullyQualifiedNamespace();
                this.entityPath = this.client.getEntityPath();

                final Map<String, Object> loggingContext = new HashMap<>(3);
                loggingContext.put(PUMP_ID_KEY, this.pumpId);
                loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, this.fqdn);
                loggingContext.put(ENTITY_PATH_KEY, this.entityPath);
                this.logger = new ClientLogger(MessagePump.class, loggingContext);

                this.processMessage = processMessage;
                this.processError = processError;
                this.concurrency = concurrency;
                this.enableAutoDisposition = enableAutoDisposition;
                this.enableAutoLockRenew = client.isAutoLockRenewRequested();
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
                this.tracer = this.instrumentation.getTracer();
            }

            /**
             * Begin pumping messages in parallel, with the parallelism equal to the configured {@code concurrency}.
             *
             * @return a mono that emits {@link TerminatedException} when this {@link MessagePump} terminates.
             * The pumping terminates when the underlying client encounters a non-retriable error, the retries exhaust,
             * or rejection when scheduling concurrently.
             */
            Mono<Void> begin() {
                final Mono<Void> terminatePumping = pollConnectionState();
                final Mono<Void> pumping = client.nonSessionProcessorReceiveV2()
                    .flatMap(new RunOnWorker(this::handleMessage, workerScheduler), concurrency, 1).then();

                return Mono.firstWithSignal(pumping, terminatePumping)
                    .onErrorMap(e -> new TerminatedException(pumpId, fqdn, entityPath, e))
                    .then(Mono.error(() -> TerminatedException.forCompletion(pumpId, fqdn, entityPath)));
            }

            private Mono<Void> pollConnectionState() {
                return Flux.interval(CONNECTION_STATE_POLL_INTERVAL)
                    .handle((ignored, sink) -> {
                        if (client.isConnectionClosed()) {
                            sink.error(new RuntimeException("Connection is terminated."));
                        } else {
                            sink.next(false);
                        }
                    })
                    .ignoreElements()
                    .then();
            }

            private void handleMessage(ServiceBusReceivedMessage message) {
                final ServiceBusMessageContext messageContext = new ServiceBusMessageContext(message);
                final Throwable error = messageContext.getThrowable();
                if (error != null) {
                    notifyError(error);
                    return;
                }
                final Disposable lockRenewDisposable;
                if (enableAutoLockRenew) {
                    lockRenewDisposable = client.beginLockRenewal(messageContext);
                } else {
                    lockRenewDisposable = Disposables.disposed();
                }
                final boolean success = notifyMessage(messageContext);
                if (enableAutoDisposition) {
                    if (success) {
                        complete(messageContext);
                    } else {
                        abandon(messageContext);
                    }
                }
                lockRenewDisposable.dispose();
            }

            private boolean notifyMessage(ServiceBusMessageContext messageContext) {
                final ServiceBusReceivedMessage message = messageContext.getMessage();
                final Context span = instrumentation.instrumentProcess("ServiceBus.process", message, Context.NONE);
                final AutoCloseable scope  = tracer.makeSpanCurrent(span);

                Throwable error = null;
                try {
                    processMessage.accept(new ServiceBusReceivedMessageContext(client, messageContext));
                } catch (Exception e) {
                    error = e;
                }

                if (error != null) {
                    notifyError(new ServiceBusException(error, ServiceBusErrorSource.USER_CALLBACK));
                    tracer.endSpan(error, message.getContext(), scope);
                    return false;
                } else {
                    tracer.endSpan(null, message.getContext(), scope);
                    return true;
                }
            }

            private void notifyError(Throwable throwable) {
                try {
                    processError.accept(new ServiceBusErrorContext(throwable, fqdn, entityPath));
                } catch (Exception e) {
                    logger.atVerbose().log("Ignoring error from user processError handler.", e);
                }
            }

            private void complete(ServiceBusMessageContext messageContext) {
                try {
                    client.complete(messageContext.getMessage()).block();
                } catch (Exception e) {
                    logger.atVerbose().log("Failed to complete message", e);
                }
            }

            private void abandon(ServiceBusMessageContext messageContext) {
                try {
                    client.abandon(messageContext.getMessage()).block();
                } catch (Exception e) {
                    logger.atVerbose().log("Failed to abandon message", e);
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
                    return Mono.fromRunnable(() -> {
                        handleMessage.accept(message);
                    }).subscribeOn(workerScheduler).then();
                    // The subscribeOn offloads message handling to a Worker from the Scheduler.
                }
            }

            /**
             * The exception emitted by the mono returned from the {@link MessagePump#begin()}.
             * The {@link TerminatedException#getCause()}} indicates the cause for the termination of message pumping.
             */
            private static final class TerminatedException extends RuntimeException {
                static final RuntimeException COMPLETION_ERROR = new RuntimeException("The MessagePump reached completion.");
                private final long pumpId;
                private final String fqdn;
                private final String entityPath;

                /**
                 * Instantiate {@link TerminatedException} representing termination of a {@link MessagePump}.
                 *
                 * @param pumpId The unique identifier of the {@link MessagePump} that terminated.
                 * @param fqdn The FQDN of the host from which the message was pumping.
                 * @param entityPath The path to the entity within the FQDN streaming message.
                 * @param terminationCause The reason for the termination of the pump.
                 */
                TerminatedException(long pumpId, String fqdn, String entityPath, Throwable terminationCause) {
                    super(terminationCause);
                    this.pumpId = pumpId;
                    this.fqdn = fqdn;
                    this.entityPath = entityPath;
                }

                /**
                 * Instantiate {@link TerminatedException} that represents the case when the {@link MessagePump}
                 * terminates by running into the completion.
                 *
                 * @param pumpId The unique identifier of the pump that terminated.
                 * @param fqdn The FQDN of the host from which the message was pumping.
                 * @param entityPath The path to the entity within the FQDN streaming message.
                 * @return the {@link TerminatedException}.
                 */
                static TerminatedException forCompletion(long pumpId, String fqdn, String entityPath) {
                    return new TerminatedException(pumpId, fqdn, entityPath, COMPLETION_ERROR);
                }

                /**
                 * Logs the cause for termination and the given {@code message} along with pump identifier, FQDN and entity path.
                 *
                 * @param logger The logger.
                 * @param message The message to log.
                 */
                void logWithCause(ClientLogger logger, String message) {
                    logger.atInfo()
                        .addKeyValue(PUMP_ID_KEY, pumpId)
                        .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, fqdn)
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .log(message, getCause());
                }

                /**
                 * Logs the given {@code message} along with pump identifier, FQDN and entity path.
                 *
                 * @param logger The logger.
                 * @param message The message to log.
                 */
                void log(ClientLogger logger, String message) {
                    logger.atInfo()
                        .addKeyValue(PUMP_ID_KEY, pumpId)
                        .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, fqdn)
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .log(message);
                }
            }
        }
    }
}
