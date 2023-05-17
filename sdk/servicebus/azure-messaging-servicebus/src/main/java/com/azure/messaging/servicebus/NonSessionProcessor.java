// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
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
import static com.azure.messaging.servicebus.FluxTrace.PROCESS_ERROR_KEY;

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
    private MessageStreaming streaming;

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
        final MessageStreaming s;
        synchronized (lock) {
            if (isRunning) {
                return;
            }
            isRunning = true;
            streaming = new MessageStreaming(builder, processMessage, processError, concurrency, enableAutoDisposition);
            s = streaming;
        }
        s.begin();
    }

    boolean isRunning() {
        synchronized (lock) {
            return isRunning;
        }
    }

    void close() {
        final MessageStreaming s;
        synchronized (lock) {
            if (!isRunning) {
                return;
            }
            isRunning = false;
            s = streaming;
        }
        s.dispose();
    }

    void stop() {
        // Synonym for 'close' see https://github.com/Azure/azure-sdk-for-java/issues/34464
        close();
    }

    String getIdentifier() {
        final MessageStreaming s;
        synchronized (lock) {
            s = streaming;
        }
        return s == null ? null : s.getIdentifier();
    }

    /**
     * The abstraction to stream messages using {@link ParallelPump}. {@link MessageStreaming} transparently switch
     * to a new pump to continue streaming when the current pump terminates.
     */
    private static final class MessageStreaming extends AtomicBoolean {
        private static final String PUMP_ID_KEY = "pump-id";
        private static final RuntimeException STREAMING_DISPOSED = new RuntimeException("The Processor closure disposed the streaming.");
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
         * Instantiate {@link MessageStreaming} to stream messages.
         *
         * @param builder The builder to build the client for pulling messages from the broker.
         * @param processMessage The consumer to invoke for each message.
         * @param processError The consumer to report the errors.
         * @param concurrency The parallelism, i.e., how many invocations of {@code processMessage} should happen in parallel.
         * @param enableAutoDisposition Indicate if auto-complete or abandon should be enabled.
         */
        MessageStreaming(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
            int concurrency, boolean enableAutoDisposition) {
            this.logger = new ClientLogger(MessageStreaming.class);
            this.builder = builder;
            this.concurrency = concurrency;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoDisposition = enableAutoDisposition;
        }

        /**
         * Begin streaming messages in parallel.
         *
         * @throws IllegalStateException If the API is called more than once or after the disposal.
         */
        void begin() {
            if (getAndSet(true)) {
                throw logger.atInfo().log(new IllegalStateException("The streaming cannot begin more than once."));
            }

            final Disposable d = Mono.defer(() -> {
                final ServiceBusReceiverAsyncClient client = builder.buildAsyncClient();
                clientIdentifier.set(client.getIdentifier());
                final ParallelPump pump = new ParallelPump(client, processMessage, processError, concurrency, enableAutoDisposition);
                return pump.begin();
            }).retryWhen(retrySpecForNextPump()).subscribe();

            if (!disposable.add(d)) {
                throw logger.atInfo().log(new IllegalStateException("Cannot begin streaming after the disposal."));
            }
        }

        String getIdentifier() {
            return clientIdentifier.get();
        }

        void dispose() {
            disposable.dispose();
        }

        /**
         * Spec to Retry for the next {@link ParallelPump} with a back-off. If the spec is asked for retry after
         * the streaming is disposed of (due to {@link NonSessionProcessor} closure), then a {@link RuntimeException}
         * indicating the disposal state will be emitted.
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
                    if (!(error instanceof PumpTerminatedException)) {
                        return monoError(logger, new IllegalStateException("RetrySignal::failure() expected to be PumpTerminatedException.", error));
                    }

                    final PumpTerminatedException e = (PumpTerminatedException) error;

                    if (disposable.isDisposed()) {
                        e.logWithCause(logger, "The Processor closure disposed the streaming, canceling retry for the next pump.");
                        return Mono.error(STREAMING_DISPOSED);
                    }

                    e.logWithCause(logger, "The current pump is terminated, scheduling retry for the next pump.");

                    return Mono.delay(NEXT_PUMP_BACKOFF, Schedulers.boundedElastic())
                        .handle((v, sink) -> {
                            if (disposable.isDisposed()) {
                                e.log(logger,
                                    "During backoff, The Processor closure disposed the streaming, canceling retry for the next pump.");
                                sink.error(STREAMING_DISPOSED);
                            } else {
                                e.log(logger, "Retrying for the next pump.");
                                sink.next(v);
                            }
                        });
                }));
        }

        /**
         * Abstraction to pump messages from a 'ServiceBusReceiverAsyncClient' as long as the client is healthy.
         */
        private static final class ParallelPump {
            private static final AtomicLong COUNTER = new AtomicLong();
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

            /**
             * Instantiate {@link ParallelPump} that pumps messages emitted by the given {@code client}. The messages
             * are pumped to the {@code processMessage} concurrently with the parallelism equal to {@code concurrency}.
             *
             * @param client The client capable of emitting messages.
             * @param processMessage The consumer that the pump should invoke for each message.
             * @param processError The consumer that the pump should report the errors.
             * @param concurrency The pumping concurrency, i.e., how many invocations of {@code processMessage} should happen in parallel.
             * @param enableAutoDisposition Indicate if auto-complete or abandon should be enabled.
             */
            ParallelPump(ServiceBusReceiverAsyncClient client, Consumer<ServiceBusReceivedMessageContext> processMessage,
                Consumer<ServiceBusErrorContext> processError, int concurrency, boolean enableAutoDisposition) {

                this.pumpId = COUNTER.incrementAndGet();
                this.client = client;
                this.fqdn = this.client.getFullyQualifiedNamespace();
                this.entityPath = this.client.getEntityPath();

                final Map<String, Object> loggingContext = new HashMap<>(3);
                loggingContext.put(PUMP_ID_KEY, this.pumpId);
                loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, this.fqdn);
                loggingContext.put(ENTITY_PATH_KEY, this.entityPath);
                this.logger = new ClientLogger(ParallelPump.class, loggingContext);

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
            }

            /**
             * Begin pumping messages in parallel, with the parallelism equal to the configured {@code concurrency}.
             *
             * @return a mono that emits {@link PumpTerminatedException} when the pumping terminates.
             * The pumping terminates when the underlying client encounters a non-retriable error, the retries exhaust,
             * or rejection when scheduling concurrently.
             */
            Mono<Void> begin() {
                final Mono<Void> terminatePumping = pollConnectionState();
                final Mono<Void> pumping = client.nonSessionProcessorReceiveV2()
                    .flatMap(new RunOnWorker(this::handleMessage, workerScheduler), concurrency, 1).then();

                return Mono.firstWithSignal(pumping, terminatePumping)
                    .onErrorMap(e -> new PumpTerminatedException(pumpId, fqdn, entityPath, e))
                    .then(Mono.error(PumpTerminatedException.forCompletion(pumpId, fqdn, entityPath)));
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
                Throwable error = null;
                try {
                    processMessage.accept(new ServiceBusReceivedMessageContext(client, messageContext));
                } catch (Exception e) {
                    error = e;
                }

                if (error != null) {
                    final Context contextWithError = messageContext.getMessage().getContext()
                        .addData(PROCESS_ERROR_KEY, error);
                    messageContext.getMessage().setContext(contextWithError);
                    notifyError(new ServiceBusException(error, ServiceBusErrorSource.USER_CALLBACK));
                    return false;
                }
                return true;
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
        }

        /**
         * Represents the exception emitted by the mono returned from the {@link ParallelPump#begin()}.
         * The {@link PumpTerminatedException#getCause()}} indicates the cause for the termination of message pumping.
         */
        private static final class PumpTerminatedException extends RuntimeException {
            static final RuntimeException TERMINAL_COMPLETION = new RuntimeException("The pump reached completion.");
            private final long pumpId;
            private final String fqdn;
            private final String entityPath;

            /**
             * Instantiate {@link PumpTerminatedException} representing termination of a {@link ParallelPump}.
             *
             * @param pumpId The unique identifier of the pump that terminated.
             * @param fqdn The FQDN of the host from which the message was pumping.
             * @param entityPath The path to the entity within the FQDN streaming message.
             * @param terminationCause The reason for the termination of the pump.
             */
            PumpTerminatedException(long pumpId, String fqdn, String entityPath, Throwable terminationCause) {
                super(terminationCause);
                this.pumpId = pumpId;
                this.fqdn = fqdn;
                this.entityPath = entityPath;
            }

            /**
             * Instantiate {@link PumpTerminatedException} that represents the case when the pump terminates by
             * running into the completion.
             *
             * @param pumpId The unique identifier of the pump that terminated.
             * @param fqdn The FQDN of the host from which the message was pumping.
             * @param entityPath The path to the entity within the FQDN streaming message.
             * @return the {@link PumpTerminatedException}.
             */
            static PumpTerminatedException forCompletion(long pumpId, String fqdn, String entityPath) {
                return new PumpTerminatedException(pumpId, fqdn, entityPath, TERMINAL_COMPLETION);
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
