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
 *  Processor to stream messages from a session aware entity.
 */
public final class SessionProcessor {
    private final Object lock = new Object();
    private final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder;
    private final int concurrencyPerSession;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private boolean isRunning;
    private RollingSessionsMessagePump rollingSessionsMessagePump;

    SessionProcessor(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder,
        int concurrencyPerSession,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError) {
        this.builder = builder;
        this.concurrencyPerSession = concurrencyPerSession;
        this.processError = processError;
        this.processMessage = processMessage;

        synchronized (lock) {
            this.isRunning = false;
        }
    }

    void start() {
        final RollingSessionsMessagePump p;
        synchronized (lock) {
            if (isRunning) {
                return;
            }
            isRunning = true;
            rollingSessionsMessagePump = new RollingSessionsMessagePump(builder, concurrencyPerSession,
                processMessage, processError);
            p = rollingSessionsMessagePump;
        }
        p.begin();
    }

    boolean isRunning() {
        synchronized (lock) {
            return isRunning;
        }
    }

    void close() {
        final RollingSessionsMessagePump p;
        synchronized (lock) {
            if (!isRunning) {
                return;
            }
            isRunning = false;
            p = rollingSessionsMessagePump;
        }
        p.dispose();
    }

    void stop() {
        // Synonym for 'close' see https://github.com/Azure/azure-sdk-for-java/issues/34464
        close();
    }

    String getIdentifier() {
        final RollingSessionsMessagePump p;
        synchronized (lock) {
            p = rollingSessionsMessagePump;
        }
        return p == null ? null : p.getClientIdentifier();
    }

    /**
     * The abstraction to stream messages using {@link SessionsMessagePump}. {@link RollingSessionsMessagePump}
     * transparently switch (rolls) to the next {@link SessionsMessagePump} to continue streaming when the current pump
     * terminates.
     */
    static final class RollingSessionsMessagePump extends AtomicBoolean {
        private static final RuntimeException DISPOSED_ERROR = new RuntimeException("The Processor closure disposed the RollingSessionsMessagePump.");
        private static final Duration NEXT_PUMP_BACKOFF = Duration.ofSeconds(5);
        private final ClientLogger logger;
        private final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder;
        private final int concurrency;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final Disposable.Composite disposable = Disposables.composite();
        private final AtomicReference<String> clientIdentifier = new AtomicReference<>();

        /**
         * Instantiate {@link RollingSessionsMessagePump} that stream messages using {@link SessionsMessagePump}.
         * The {@link RollingSessionsMessagePump} rolls to the next {@link SessionsMessagePump} to continue streaming
         * when the current {@link SessionsMessagePump} terminates.
         *
         * @param builder The builder to build the client for pulling messages from the broker.
         * @param concurrencyPerSession The parallelism, i.e., how many invocations of {@code processMessage} should happen
         *      in parallel for each session.
         * @param processMessage The consumer to invoke for each message.
         * @param processError The consumer to report the errors.

         */
        RollingSessionsMessagePump(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder,
            int concurrencyPerSession,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError) {
            this.logger = new ClientLogger(RollingSessionsMessagePump.class);
            this.builder = builder;
            this.concurrency = concurrencyPerSession;
            this.processError = processError;
            this.processMessage = processMessage;
        }

        /**
         * Begin streaming messages. To terminate the streaming, dispose the pump by invoking
         * {@link RollingSessionsMessagePump#dispose()}.
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
        // to be called only from 'RollingSessionsMessagePump#begin'. The package internal scope is to support testing.
        Mono<Void> beginIntern() {
            final Mono<Void> pumping = Mono.using(
                () -> {
                    final SessionsMessagePump pump = builder.buildPumpForProcessor(logger, concurrency, processMessage, processError);
                    return pump;
                },
                pump -> {
                    clientIdentifier.set(pump.getIdentifier());
                    return pump.begin();
                },
                pump -> {
                    // NOP: The pump (SessionsMessagePump) does self clean up and there isn't anything that is owned by
                    // RollingSessionsMessagePump to clean up.
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
         * Retry spec to roll to the next {@link SessionsMessagePump} with a back-off. If the spec is asked for retry after
         * the {@link RollingSessionsMessagePump} is disposed of (due to {@link SessionProcessor} closure), then an exception
         * {@link RollingSessionsMessagePump#DISPOSED_ERROR} will be emitted.
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
                        return monoError(logger, new IllegalStateException("RetrySignal::failure() expected to be MessagePump.TerminatedException.", error));
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
}
