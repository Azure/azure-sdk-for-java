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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.messaging.servicebus.FluxTrace.PROCESS_ERROR_KEY;

/**
 *  Processor to pump messages from a session unaware entity.
 */
public final class NonSessionProcessor {
    private final Object lock = new Object();
    private final ServiceBusClientBuilder builder;
    private final int concurrency;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final boolean enableAutoLockRenew;
    private final boolean enableAutoDisposition;
    private boolean isRunning;
    private RecoverableMessagePump pump;

    NonSessionProcessor(ServiceBusClientBuilder builder,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
        int concurrency, boolean enableAutoLockRenew, boolean enableAutoDisposition) {
        this.builder = builder;
        this.concurrency = concurrency;
        this.processError = processError;
        this.processMessage = processMessage;
        this.enableAutoLockRenew = enableAutoLockRenew;
        this.enableAutoDisposition = enableAutoDisposition;

        synchronized (lock) {
            this.isRunning = false;
        }
    }

    void start() {
        final RecoverableMessagePump p;
        synchronized (lock) {
            if (isRunning) {
                return;
            }
            isRunning = true;
            pump = new RecoverableMessagePump(builder, processMessage, processError, concurrency, enableAutoLockRenew,
                enableAutoDisposition);
            p = pump;
        }
        p.begin();
    }

    boolean isRunning() {
        synchronized (lock) {
            return isRunning;
        }
    }

    void close() {
        final RecoverableMessagePump p;
        synchronized (lock) {
            if (!isRunning) {
                return;
            }
            isRunning = false;
            p = pump;
        }
        p.dispose();
    }

    /**
     * The abstraction that wraps a {@link ParallelPumping} and transparently moves to the next 'ParallelPumping' when
     * the current one terminates.
     */
    private static final class RecoverableMessagePump {
        static final RuntimeException PUMP_TERMINATED = new RuntimeException("Pump is Terminated (due to Processor close).");
        private final ServiceBusClientBuilder builder;
        private final int concurrency;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final Disposable.Composite disposable = Disposables.composite();
        private final boolean enableAutoLockRenew;
        private final boolean enableAutoDisposition;

        RecoverableMessagePump(ServiceBusClientBuilder builder,
                               Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
                               int concurrency, boolean enableAutoLockRenew, boolean enableAutoDisposition) {
            this.builder = builder;
            this.concurrency = concurrency;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoLockRenew = enableAutoLockRenew;
            this.enableAutoDisposition = enableAutoDisposition;
        }

        void begin() {
            final Disposable d = Mono.defer(() -> {
                final ServiceBusReceiverAsyncClient client = builder.receiver().buildAsyncClient();
                final ParallelPumping parallelPumping = new ParallelPumping(client, processMessage, processError,
                    concurrency, enableAutoLockRenew, enableAutoDisposition);
                return parallelPumping.begin();
            }).retryWhen(retryWhenSpec()).subscribe();

            disposable.add(d);
        }

        void dispose() {
            disposable.dispose();
        }

        /**
         * Spec to Retry forever with a back-off until this pump is disposed of, which happens when the Processor
         * is closed.
         *
         * @return the retry spec.
         */
        private Retry retryWhenSpec() {
            return Retry.from(retrySignals -> retrySignals
                .concatMap(retrySignal -> {
                    final Retry.RetrySignal signal = retrySignal.copy();
                    final Throwable error = signal.failure();
                    if (error == null) {
                        return Mono.error(new IllegalStateException("RetrySignal::failure() not expected to be null."));
                    }
                    // to_do: log-error
                    if (disposable.isDisposed()) {
                        return Mono.error(PUMP_TERMINATED);
                    }
                    return Mono.delay(Duration.ofSeconds(5), Schedulers.boundedElastic())
                        .handle((v, sink) -> {
                            if (disposable.isDisposed()) {
                                sink.error(PUMP_TERMINATED);
                            } else {
                                sink.next(v);
                            }
                        });
                }));
        }
    }

    /**
     * Abstraction to pump messages as long as the associated 'ServiceBusReceiverAsyncClient' is healthy.
     */
    private static final class ParallelPumping {
        static final RuntimeException PUMP_COMPLETED = new RuntimeException("Pump Terminated with completion.");
        private static final AtomicLong COUNTER = new AtomicLong();
        private final ClientLogger logger;
        private final ServiceBusReceiverAsyncClient client;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final int concurrency;
        private final boolean enableAutoLockRenew;
        private final boolean enableAutoDisposition;
        private final String fqdn;
        private final String entityPath;

        ParallelPumping(ServiceBusReceiverAsyncClient client, Consumer<ServiceBusReceivedMessageContext> processMessage,
            Consumer<ServiceBusErrorContext> processError, int concurrency, boolean enableAutoLockRenew,
            boolean enableAutoDisposition) {
            final Map<String, Object> loggingContext = new HashMap<>(1);
            loggingContext.put("id", COUNTER.incrementAndGet());
            this.logger = new ClientLogger(ParallelPumping.class, loggingContext);

            this.client = client;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoLockRenew = enableAutoLockRenew;
            this.enableAutoDisposition = enableAutoDisposition;
            this.fqdn = client.getFullyQualifiedNamespace();
            this.entityPath = client.getEntityPath();
            this.concurrency = concurrency;
        }

        /**
         * Begin pumping in parallel.
         *
         * @return A mono that terminates when pumping encounters an error i.e. underlying 'ServiceBusReceiverAsyncClient's
         * retry exhausted or encountered a non-retriable error, or rejection when scheduling concurrently.
         */
        Mono<Void> begin() {
            final OnMessage onMessage = new OnMessage(this::onMessage, Schedulers.boundedElastic());
            final Mono<Void> terminatePumping = channelTerminated();
            final Mono<Void> pumpInParallel = client.nonSessionProcessorReceiveV2().flatMap(onMessage, concurrency, 1).then();
            return Mono.firstWithSignal(pumpInParallel, terminatePumping).then(Mono.error(PUMP_COMPLETED));
        }

        private Mono<Void> channelTerminated() {
            return Flux.interval(Duration.ofSeconds(20))
                .handle((ignored, sink) -> {
                    if (client.isConnectionClosed()) {
                        sink.error(new RuntimeException("Channel is terminated."));
                    } else {
                        sink.next(false);
                    }
                })
                .ignoreElements()
                .then();
        }

        private void onMessage(ServiceBusReceivedMessage message) {
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
                logger.verbose("Ignoring error from user processError handler.", e);
            }
        }

        private void complete(ServiceBusMessageContext messageContext) {
            try {
                client.complete(messageContext.getMessage()).block();
            } catch (Exception e) {
                logger.verbose("Failed to complete message", e);
            }
        }

        private void abandon(ServiceBusMessageContext messageContext) {
            try {
                client.abandon(messageContext.getMessage()).block();
            } catch (Exception e) {
                logger.verbose("Failed to abandon message", e);
            }
        }

        private static final class OnMessage implements Function<ServiceBusReceivedMessage, Publisher<Void>> {
            private final Consumer<ServiceBusReceivedMessage> messageConsumer;
            private final Scheduler consumingThreadScheduler;

            OnMessage(Consumer<ServiceBusReceivedMessage> messageConsumer, Scheduler consumingThreadScheduler) {
                this.messageConsumer = messageConsumer;
                this.consumingThreadScheduler = consumingThreadScheduler;
            }

            @Override
            public Publisher<Void> apply(ServiceBusReceivedMessage message) {
                return Mono.fromRunnable(() -> {
                    messageConsumer.accept(message);
                }).subscribeOn(consumingThreadScheduler).then();
                // The subscribeOn ^ enabling parallelism.
            }
        }
    }
}
