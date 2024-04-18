// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * An abstraction that underneath uses either {@link MessagePump} or {@link SessionsMessagePump}
 * to pump messages from session unaware or session aware entity.
 */
final class ServiceBusProcessor {
    private final Object lock = new Object();
    private final Kind kind;
    private final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder nonSessionBuilder;
    private final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionBuilder;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final int concurrency;
    private final Boolean enableAutoDisposition;
    private boolean isRunning;
    private RollingMessagePump rollingMessagePump;

    /**
     * Instantiate {@link ServiceBusProcessor} to pump messages from a session unaware entity.
     *
     * @param builder The builder to build the client for pulling messages from a session unaware entity.
     * @param processMessage The consumer to invoke for each message.
     * @param processError The consumer to report the errors.
     * @param concurrency The parallelism, i.e., how many invocations of {@code processMessage} should happen in parallel.
     * @param enableAutoDisposition Indicate if auto-complete or abandon should be enabled.
     */
    ServiceBusProcessor(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
        int concurrency, boolean enableAutoDisposition) {
        this.kind = Kind.NON_SESSION;
        this.nonSessionBuilder = builder;
        this.sessionBuilder = null;
        this.processError = processError;
        this.processMessage = processMessage;
        this.concurrency = concurrency;
        this.enableAutoDisposition = enableAutoDisposition;

        synchronized (lock) {
            this.isRunning = false;
        }
    }

    /**
     * Instantiate {@link ServiceBusProcessor} to pump messages from a session aware entity.
     *
     * @param builder The builder to build the client for pulling messages from a session aware entity.
     * @param processMessage The consumer to invoke for each message.
     * @param processError The consumer to report the errors.
     * @param concurrency The parallelism, i.e., how many invocations of {@code processMessage} should happen in parallel per session.
     */
    ServiceBusProcessor(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
        int concurrency) {
        this.kind = Kind.SESSION;
        this.sessionBuilder = builder;
        this.nonSessionBuilder = null;
        this.processError = processError;
        this.processMessage = processMessage;
        this.concurrency = concurrency;
        this.enableAutoDisposition = null;

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
            if (kind == Kind.NON_SESSION) {
                rollingMessagePump = new RollingMessagePump(nonSessionBuilder,
                    processMessage, processError, concurrency, enableAutoDisposition);
            } else {
                rollingMessagePump = new RollingMessagePump(sessionBuilder,
                    processMessage, processError, concurrency);
            }
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
     * The abstraction to stream messages using either {@link MessagePump} or {@link SessionsMessagePump} pump.
     * {@link RollingMessagePump} transparently switch (rolls) to the next pump to continue streaming when the current pump
     * terminates.
     */
    static final class RollingMessagePump extends AtomicBoolean {
        private static final RuntimeException DISPOSED_ERROR = new RuntimeException("The Processor closure disposed the RollingMessagePump.");
        private static final Duration NEXT_PUMP_BACKOFF = Duration.ofSeconds(5);
        private final ClientLogger logger;
        private final Kind kind;
        private final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder nonSessionBuilder;
        private final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionBuilder;
        private final int concurrency;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final Boolean enableAutoDisposition;
        private final Disposable.Composite disposable = Disposables.composite();
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
            this.kind = Kind.NON_SESSION;
            this.nonSessionBuilder = builder;
            this.sessionBuilder = null;
            this.concurrency = concurrency;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoDisposition = enableAutoDisposition;
        }

        /**
         * Instantiate {@link RollingMessagePump} that stream messages using {@link SessionsMessagePump}.
         * The {@link RollingMessagePump} rolls to the next {@link SessionsMessagePump} to continue streaming
         * when the current {@link SessionsMessagePump} terminates.
         *
         * @param builder The builder to build the client for pulling messages from the broker.
         * @param concurrencyPerSession The parallelism, i.e., how many invocations of {@code processMessage} should happen
         *      in parallel for each session.
         * @param processMessage The consumer to invoke for each message.
         * @param processError The consumer to report the errors.

         */
        RollingMessagePump(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
            int concurrencyPerSession) {
            this.logger = new ClientLogger(RollingMessagePump.class);
            this.kind = Kind.SESSION;
            this.sessionBuilder = builder;
            this.nonSessionBuilder = null;
            this.processError = processError;
            this.processMessage = processMessage;
            this.concurrency = concurrencyPerSession;
            this.enableAutoDisposition = null;
        }

        /**
         * Begin streaming messages. To terminate the streaming, dispose the pump by invoking
         * {@link RollingMessagePump#dispose()}.
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
        // to be called only from 'RollingMessagePump#begin'. The package internal scope is to support testing.
        Mono<Void> beginIntern() {
            final Mono<Void> pumping;
            if (kind == Kind.NON_SESSION) {
                pumping = Mono.using(
                    () -> {
                        // TODO: anu, there seems an opportunity to simplify, if builder directly returns MessagePump,
                        //  similar to SessionsMessagePump.
                        return nonSessionBuilder.buildAsyncClientForProcessor();
                    },
                    client -> {
                        clientIdentifier.set(client.getIdentifier());
                        final MessagePump pump = new MessagePump(client,
                            processMessage, processError, concurrency, enableAutoDisposition);
                        return pump.begin();
                    },
                    client -> {
                        client.close();
                    },
                    true);
            } else {
                pumping = Mono.using(
                    () -> {
                        final SessionsMessagePump pump = sessionBuilder.buildPumpForProcessor(logger,
                            processMessage, processError, concurrency);
                        return pump;
                    },
                    pump -> {
                        clientIdentifier.set(pump.getIdentifier());
                        return pump.begin();
                    },
                    pump -> {
                        // NOP: The pump (SessionsMessagePump) does self clean up and there isn't anything that is owned
                        // by RollingSessionsMessagePump to clean up.
                    },
                    true);
            }
            final Mono<Void> rollingPump = pumping
                .onErrorResume(MessagePumpTerminatedException.class, t -> notifyError(t).then(Mono.error(t)))
                .retryWhen(retrySpecForNextPump());
            return rollingPump;
        }

        String getClientIdentifier() {
            return clientIdentifier.get();
        }

        void dispose() {
            disposable.dispose();
        }

        /**
         * Notify the current pump termination cause to the processor handler.
         * <p>
         * The processor handler will be called on a worker thread so that application may do blocking calls in
         * the handler.
         * </p>
         * @param t the input error with cause as the reason for pump termination.
         * @return a Mono that when subscribed invokes the processor handler.
         */
        private Mono<Void> notifyError(MessagePumpTerminatedException t) {
            final ServiceBusErrorContext errorContext = t.getErrorContext();
            if (errorContext == null) {
                return Mono.empty();
            }
            return Mono.<Void>fromRunnable(() -> {
                try {
                    processError.accept(errorContext);
                } catch (Exception e) {
                    logger.atVerbose().log("Ignoring error from user processError handler.", e);
                }
            }).subscribeOn(Schedulers.boundedElastic());
        }

        /**
         * Retry spec to roll to the next {@link SessionsMessagePump} with a back-off. If the spec is asked for retry after
         * the {@link RollingMessagePump} is disposed of (due to {@link ServiceBusProcessor} closure),
         * then an exception {@link RollingMessagePump#DISPOSED_ERROR} will be emitted.
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
                    if (!(error instanceof MessagePumpTerminatedException)) {
                        return monoError(logger, new IllegalStateException("RetrySignal::failure() expected to be MessagePumpTerminatedException.", error));
                    }

                    final MessagePumpTerminatedException e = (MessagePumpTerminatedException) error;

                    if (disposable.isDisposed()) {
                        e.log(logger, "The Processor closure disposed the streaming, canceling retry for the next MessagePump.", true);
                        return Mono.error(DISPOSED_ERROR);
                    }

                    e.log(logger, "The current MessagePump is terminated, scheduling retry for the next pump.", true);

                    return Mono.delay(NEXT_PUMP_BACKOFF, Schedulers.boundedElastic())
                        .handle((v, sink) -> {
                            if (disposable.isDisposed()) {
                                e.log(logger,
                                    "During backoff, The Processor closure disposed the streaming, canceling retry for the next MessagePump.", false);
                                sink.error(DISPOSED_ERROR);
                            } else {
                                e.log(logger, "Retrying for the next MessagePump.", false);
                                sink.next(v);
                            }
                        });
                }));
        }
    }

    /**
     * The {@link ServiceBusProcessor} kind.
     */
    enum Kind {
        NON_SESSION,
        SESSION
    }
}
